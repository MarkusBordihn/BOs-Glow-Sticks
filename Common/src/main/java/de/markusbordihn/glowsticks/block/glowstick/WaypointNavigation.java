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

import com.mojang.math.Vector3f;
import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class WaypointNavigation {

  public static void handleWaypointParticles(
      ClientLevel clientLevel, BlockPos blockPos, Random random, DyeColor glowStickColor) {
    if (shouldSpawnWaypointParticles(clientLevel, blockPos, random, glowStickColor)) {
      spawnWaypointParticles(clientLevel, blockPos, random, glowStickColor);
    }
  }

  private static boolean shouldSpawnWaypointParticles(
      ClientLevel clientLevel, BlockPos blockPos, Random random, DyeColor glowStickColor) {
    if (!GlowSticksConfig.spawnWaypointParticles) {
      return false;
    }

    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null || !player.isShiftKeyDown() || random.nextInt(2) != 0) {
      return false;
    }

    DyeColor heldColor = getHeldGlowStickColor(player);
    if (heldColor == null || heldColor != glowStickColor) {
      return false;
    }

    return isNearestMatchingGlowStick(clientLevel, blockPos, player, heldColor);
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

  private static boolean isNearestMatchingGlowStick(
      ClientLevel clientLevel, BlockPos currentBlockPos, LocalPlayer player, DyeColor targetColor) {
    BlockPos playerPos = player.blockPosition();
    double currentDistance = playerPos.distSqr(currentBlockPos);

    if (currentDistance < 6.0) {
      return false;
    }

    return findNearestGlowStick(
        clientLevel, playerPos, currentBlockPos, targetColor, currentDistance);
  }

  private static boolean findNearestGlowStick(
      ClientLevel clientLevel,
      BlockPos playerPos,
      BlockPos currentBlockPos,
      DyeColor targetColor,
      double currentDistance) {

    int horizontalRadius = GlowSticksConfig.waypointSearchRadius;
    int verticalRadius = GlowSticksConfig.waypointVerticalSearchRadius;

    for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
      for (int y = -verticalRadius; y <= verticalRadius; y++) {
        for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
          BlockPos checkPos = playerPos.offset(x, y, z);
          if (checkPos.equals(currentBlockPos)) {
            continue;
          }

          if (isMatchingGlowStickCloser(
              clientLevel, checkPos, targetColor, playerPos, currentDistance)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static boolean isMatchingGlowStickCloser(
      ClientLevel clientLevel,
      BlockPos checkPos,
      DyeColor targetColor,
      BlockPos playerPos,
      double currentDistance) {

    BlockState checkState = clientLevel.getBlockState(checkPos);
    if (checkState.getBlock() instanceof GlowStickBlock otherGlowStick
        && otherGlowStick.getGlowStickColor() == targetColor) {
      double otherDistance = playerPos.distSqr(checkPos);
      return otherDistance >= 6.0 && otherDistance < currentDistance;
    }
    return false;
  }

  private static void spawnWaypointParticles(
      ClientLevel clientLevel, BlockPos blockPos, Random random, DyeColor glowStickColor) {

    Vector3f particleColors = calculateWaypointColors(glowStickColor);
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) {
      return;
    }

    TrailParameters trailParams = calculateTrailParameters(player, blockPos);
    if (trailParams.distance < 2.0) {
      return;
    }

    spawnWaypointTrail(clientLevel, random, particleColors, trailParams);
  }

  private static Vector3f calculateWaypointColors(DyeColor glowStickColor) {
    float[] baseColors = glowStickColor.getTextureDiffuseColors();
    float maxColorValue = Math.max(baseColors[0], Math.max(baseColors[1], baseColors[2]));
    float colorMultiplier = maxColorValue > 0.3f ? 1.8f : 2.5f;

    return new Vector3f(
        Math.min(1.0f, baseColors[0] * colorMultiplier),
        Math.min(1.0f, baseColors[1] * colorMultiplier),
        Math.min(1.0f, baseColors[2] * colorMultiplier));
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
      ClientLevel clientLevel, Random random, Vector3f particleColors, TrailParameters params) {

    int baseWaypoints = Math.max(8, Math.min(40, (int) (params.distance * 2.2)));
    int particlesPerWaypoint = calculateParticlesPerWaypoint(params.distance);
    double waypointStep = 1.0 / (baseWaypoints + 1);
    double minSpawnDistance = 1.0;

    spawnMainTrail(
        clientLevel,
        random,
        particleColors,
        params,
        baseWaypoints,
        particlesPerWaypoint,
        waypointStep,
        minSpawnDistance);
    spawnTargetParticles(clientLevel, random, particleColors, params);
    spawnHelperParticles(
        clientLevel, random, particleColors, params, baseWaypoints, minSpawnDistance);
  }

  private static int calculateParticlesPerWaypoint(double distance) {
    if (distance < 5.0) return 1;
    if (distance < 15.0) return 2;
    return 3;
  }

  private static void spawnMainTrail(
      ClientLevel clientLevel,
      Random random,
      Vector3f particleColors,
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
            new DustParticleOptions(particleColors, particleSize),
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
      ClientLevel clientLevel, Random random, Vector3f particleColors, TrailParameters params) {

    int targetParticleCount = Math.max(3, Math.min(8, (int) (params.distance * 0.5)));
    for (int i = 0; i < targetParticleCount; i++) {
      clientLevel.addParticle(
          new DustParticleOptions(particleColors, 1.5f),
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
      Random random,
      Vector3f particleColors,
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
            new DustParticleOptions(particleColors, 0.8f),
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
      double distance) {}
}
