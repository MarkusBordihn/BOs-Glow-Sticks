/*
 * Copyright 2021 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.glowsticks.block.glowstick;

import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class WaypointNavigation {

  private static final int SEARCH_INTERVAL_TICKS = 10;
  private static long lastSearchTick = 0;
  private static DyeColor cachedSearchColor = null;
  private static BlockPos cachedNearestGlowStick = null;

  public static void handleWaypointParticles(
    ClientLevel clientLevel, BlockPos blockPos, RandomSource random, DyeColor glowStickColor) {
    if (shouldSpawnWaypointParticles(clientLevel, blockPos, random, glowStickColor)) {
      spawnWaypointParticles(clientLevel, blockPos, random, glowStickColor);
    }
  }

  private static boolean shouldSpawnWaypointParticles(
    ClientLevel clientLevel, BlockPos blockPos, RandomSource random, DyeColor glowStickColor) {
    if (!GlowSticksConfig.spawnWaypointParticles) {
      return false;
    }

    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null || !player.isShiftKeyDown()) {
      return false;
    }

    DyeColor heldColor = getHeldGlowStickColor(player);
    if (heldColor == null || heldColor != glowStickColor) {
      return false;
    }

    long currentTick = clientLevel.getGameTime();
    if (cachedSearchColor != heldColor || currentTick - lastSearchTick >= SEARCH_INTERVAL_TICKS) {
      lastSearchTick = currentTick;
      cachedSearchColor = heldColor;
      cachedNearestGlowStick = findNearestGlowStick(clientLevel, player, heldColor);
    }

    return blockPos.equals(cachedNearestGlowStick);
  }

  private static BlockPos findNearestGlowStick(
    ClientLevel clientLevel, LocalPlayer player, DyeColor targetColor) {
    BlockPos playerPos = player.blockPosition();
    int horizontalRadius = GlowSticksConfig.waypointSearchRadius;
    int verticalRadius = GlowSticksConfig.waypointVerticalSearchRadius;

    BlockPos nearest = null;
    double nearestDistSqr = Double.MAX_VALUE;

    for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
      for (int y = -verticalRadius; y <= verticalRadius; y++) {
        for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
          BlockPos checkPos = playerPos.offset(x, y, z);
          BlockState checkState = clientLevel.getBlockState(checkPos);

          if (checkState.getBlock() instanceof GlowStickBlock glowStick
            && glowStick.getGlowStickColor() == targetColor) {
            double distSqr = playerPos.distSqr(checkPos);

            if (distSqr >= 6.0 && distSqr < nearestDistSqr) {
              nearestDistSqr = distSqr;
              nearest = checkPos;
            }
          }
        }
      }
    }

    return nearest;
  }

  private static DyeColor getHeldGlowStickColor(LocalPlayer player) {
    if (player.getMainHandItem().getItem() instanceof GlowStickItem mainHandGlowStick) {
      return mainHandGlowStick.getDyeColor();
    }
    if (player.getOffhandItem().getItem() instanceof GlowStickItem offHandGlowStick) {
      return offHandGlowStick.getDyeColor();
    }
    return DyeColor.WHITE;
  }

  private static void spawnWaypointParticles(
    ClientLevel clientLevel, BlockPos blockPos, RandomSource random, DyeColor glowStickColor) {

    int particleColor = calculateWaypointColor(glowStickColor);
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) {
      return;
    }

    TrailParameters trailParams = calculateTrailParameters(player, blockPos);
    if (trailParams.distance < 2.0) {
      return;
    }

    spawnWaypointTrail(clientLevel, random, particleColor, trailParams);
  }

  private static int calculateWaypointColor(DyeColor glowStickColor) {
    int colorValue = glowStickColor.getTextureDiffuseColor();
    int red = (colorValue >> 16) & 0xFF;
    int green = (colorValue >> 8) & 0xFF;
    int blue = colorValue & 0xFF;

    float maxColorComponent = Math.max(red, Math.max(green, blue)) / 255.0f;
    float brightnessMultiplier = maxColorComponent > 0.3f ? 1.8f : 2.5f;

    int enhancedRed = Math.clamp((int) (red * brightnessMultiplier), 0, 255);
    int enhancedGreen = Math.clamp((int) (green * brightnessMultiplier), 0, 255);
    int enhancedBlue = Math.clamp((int) (blue * brightnessMultiplier), 0, 255);

    return (enhancedRed << 16) | (enhancedGreen << 8) | enhancedBlue;
  }

  private static TrailParameters calculateTrailParameters(LocalPlayer player, BlockPos blockPos) {
    float playerYaw = player.getYRot();
    double behindPlayerOffsetX = -Math.sin(Math.toRadians(playerYaw)) * 1.5;
    double behindPlayerOffsetZ = Math.cos(Math.toRadians(playerYaw)) * 1.5;
    double startX = player.getX() + behindPlayerOffsetX;
    double startY = player.getY() + player.getEyeHeight() - 0.2;
    double startZ = player.getZ() + behindPlayerOffsetZ;

    double targetX = blockPos.getX() + 0.5;
    double targetY = blockPos.getY() + 0.5;
    double targetZ = blockPos.getZ() + 0.5;
    double deltaX = targetX - startX;
    double deltaY = targetY - startY;
    double deltaZ = targetZ - startZ;
    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

    return new TrailParameters(
      startX, startY, startZ, targetX, targetY, targetZ, deltaX, deltaY, deltaZ, distance);
  }

  private static void spawnWaypointTrail(
    ClientLevel clientLevel, RandomSource random, int particleColor, TrailParameters params) {

    int baseWaypoints = Math.max(8, Math.min(40, (int) (params.distance * 2.2)));
    int particlesPerWaypoint = calculateParticlesPerWaypoint(params.distance);
    double waypointStep = 1.0 / (baseWaypoints + 1);
    double minSpawnDistance = 1.0;

    spawnMainTrail(
      clientLevel,
      random,
      particleColor,
      params,
      baseWaypoints,
      particlesPerWaypoint,
      waypointStep,
      minSpawnDistance);
    spawnTargetParticles(clientLevel, random, particleColor, params);
    spawnHelperParticles(
      clientLevel, random, particleColor, params, baseWaypoints, minSpawnDistance);
  }

  private static int calculateParticlesPerWaypoint(double distance) {
    if (distance < 5.0) {
      return 1;
    }
    if (distance < 15.0) {
      return 2;
    }
    return 3;
  }

  private static void spawnMainTrail(
    ClientLevel clientLevel,
    RandomSource random,
    int color,
    TrailParameters params,
    int baseWaypoints,
    int particlesPerWaypoint,
    double waypointStep,
    double minSpawnDistance) {

    for (int i = 1; i <= baseWaypoints; i++) {
      double progress = i * waypointStep;
      if (progress * params.distance < minSpawnDistance) {
        continue;
      }

      double waypointX =
        params.startX + (params.deltaX * progress) + (random.nextGaussian() - 0.5) * 0.06;
      double waypointY =
        params.startY + (params.deltaY * progress) + (random.nextGaussian() - 0.5) * 0.06;
      double waypointZ =
        params.startZ + (params.deltaZ * progress) + (random.nextGaussian() - 0.5) * 0.06;

      for (int j = 0; j < particlesPerWaypoint; j++) {
        float particleSize =
          (float) Math.min(2.0f, 0.9f + (params.distance * 0.08f) - (progress * 0.2f));

        clientLevel.addParticle(
          new DustParticleOptions(color, particleSize),
          waypointX + (random.nextGaussian() - 0.5) * 0.04,
          waypointY + (random.nextGaussian() - 0.5) * 0.04,
          waypointZ + (random.nextGaussian() - 0.5) * 0.04,
          (random.nextGaussian() - 0.5) * 0.003,
          0.003 + random.nextGaussian() * 0.001,
          (random.nextGaussian() - 0.5) * 0.003);
      }
    }
  }

  private static void spawnTargetParticles(
    ClientLevel clientLevel, RandomSource random, int particleColor, TrailParameters params) {

    int targetParticleCount = Math.clamp((int) (params.distance * 0.5), 3, 8);
    for (int i = 0; i < targetParticleCount; i++) {
      clientLevel.addParticle(
        new DustParticleOptions(particleColor, 1.5f),
        params.targetX + (random.nextGaussian() - 0.5) * 0.2,
        params.targetY - 0.35 + random.nextFloat() * 0.3,
        params.targetZ + (random.nextGaussian() - 0.5) * 0.2,
        (random.nextGaussian() - 0.5) * 0.004,
        0.004 + random.nextGaussian() * 0.002,
        (random.nextGaussian() - 0.5) * 0.004);
    }
  }

  private static void spawnHelperParticles(
    ClientLevel clientLevel,
    RandomSource random,
    int particleColor,
    TrailParameters params,
    int baseWaypoints,
    double minSpawnDistance) {

    if (params.distance > 12.0) {
      int helperParticleCount = Math.min(5, baseWaypoints / 8);
      for (int i = 0; i < helperParticleCount; i++) {
        double randomProgress = 0.3 + (random.nextDouble() * 0.4);
        if (randomProgress * params.distance < minSpawnDistance) {
          continue;
        }

        clientLevel.addParticle(
          new DustParticleOptions(particleColor, 0.8f),
          params.startX + (params.deltaX * randomProgress) + (random.nextGaussian() - 0.5) * 0.12,
          params.startY + (params.deltaY * randomProgress) + (random.nextGaussian() - 0.5) * 0.12,
          params.startZ + (params.deltaZ * randomProgress) + (random.nextGaussian() - 0.5) * 0.12,
          (random.nextGaussian() - 0.5) * 0.002,
          0.002,
          (random.nextGaussian() - 0.5) * 0.002);
      }
    }
  }

  private record TrailParameters(
    double startX,
    double startY,
    double startZ,
    double targetX,
    double targetY,
    double targetZ,
    double deltaX,
    double deltaY,
    double deltaZ,
    double distance) {

  }
}
