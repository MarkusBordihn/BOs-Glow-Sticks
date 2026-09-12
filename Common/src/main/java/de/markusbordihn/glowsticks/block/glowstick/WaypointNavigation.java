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

import de.markusbordihn.glowsticks.block.CreativeGlowStickBlock;
import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.item.CreativeGlowStickItem;
import de.markusbordihn.glowsticks.item.GlowStickColors;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WaypointNavigation {

  private static final int SEARCH_INTERVAL_TICKS = 10;
  private static final double MIN_WAYPOINT_DISTANCE_SQUARED = 6.0;
  private static long lastSearchTick = 0;
  private static DyeColor cachedSearchColor = null;
  private static BlockPos cachedNearestGlowStick = null;
  private static ResourceKey<Level> cachedSearchDimension = null;

  public static void handleWaypointParticles(
    ClientLevel clientLevel, BlockPos blockPos, RandomSource random, DyeColor glowStickColor) {
    if (shouldSpawnWaypointParticles(clientLevel, blockPos, glowStickColor)) {
      spawnWaypointParticles(clientLevel, blockPos, random, glowStickColor);
    }
  }

  private static boolean shouldSpawnWaypointParticles(
    ClientLevel clientLevel, BlockPos blockPos, DyeColor glowStickColor) {
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
    ResourceKey<Level> currentDimension = clientLevel.dimension();
    if (cachedSearchColor != heldColor
      || cachedSearchDimension != currentDimension
      || currentTick < lastSearchTick
      || currentTick - lastSearchTick >= SEARCH_INTERVAL_TICKS) {
      lastSearchTick = currentTick;
      cachedSearchColor = heldColor;
      cachedSearchDimension = currentDimension;
      cachedNearestGlowStick = findNearestGlowStick(clientLevel, player, heldColor);
    }

    return blockPos.equals(cachedNearestGlowStick);
  }

  private static BlockPos findNearestGlowStick(
    ClientLevel clientLevel, LocalPlayer player, DyeColor targetColor) {
    BlockPos playerPos = player.blockPosition();
    int horizontalRadius = GlowSticksConfig.waypointSearchRadius;
    int verticalRadius = GlowSticksConfig.waypointVerticalSearchRadius;
    BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

    for (int shellRadius = 1;
      shellRadius <= Math.max(horizontalRadius, verticalRadius);
      shellRadius++) {
      BlockPos nearestOfShell =
        findInShell(
          clientLevel,
          playerPos,
          checkPos,
          targetColor,
          shellRadius,
          horizontalRadius,
          verticalRadius);
      if (nearestOfShell != null) {
        return nearestOfShell;
      }
    }

    return null;
  }

  private static BlockPos findInShell(
    ClientLevel clientLevel,
    BlockPos playerPos,
    BlockPos.MutableBlockPos checkPos,
    DyeColor targetColor,
    int shellRadius,
    int horizontalRadius,
    int verticalRadius) {
    int horizontalLimit = Math.min(shellRadius, horizontalRadius);
    int verticalLimit = Math.min(shellRadius, verticalRadius);

    BlockPos nearest = null;
    double nearestDistanceSquared = Double.MAX_VALUE;

    for (int offsetY = -verticalLimit; offsetY <= verticalLimit; offsetY++) {
      for (int offsetX = -horizontalLimit; offsetX <= horizontalLimit; offsetX++) {
        boolean fullRow = Math.abs(offsetX) == shellRadius || Math.abs(offsetY) == shellRadius;
        for (int offsetZ = -horizontalLimit; offsetZ <= horizontalLimit; offsetZ++) {
          if (!fullRow && Math.abs(offsetZ) != shellRadius) {
            continue;
          }

          checkPos.set(
            playerPos.getX() + offsetX, playerPos.getY() + offsetY, playerPos.getZ() + offsetZ);
          double distanceSquared = playerPos.distSqr(checkPos);
          if (distanceSquared < MIN_WAYPOINT_DISTANCE_SQUARED
            || distanceSquared >= nearestDistanceSquared
            || !clientLevel.isLoaded(checkPos)
            || !matchesGlowStickColor(clientLevel.getBlockState(checkPos), targetColor)) {
            continue;
          }

          nearestDistanceSquared = distanceSquared;
          nearest = checkPos.immutable();
        }
      }
    }

    return nearest;
  }

  private static boolean matchesGlowStickColor(BlockState blockState, DyeColor targetColor) {
    Block block = blockState.getBlock();
    if (block instanceof GlowStickBlock glowStickBlock) {
      return glowStickBlock.getGlowStickColor() == targetColor;
    }
    if (block instanceof CreativeGlowStickBlock creativeGlowStickBlock) {
      return creativeGlowStickBlock.getGlowStickColor() == targetColor;
    }

    return false;
  }

  private static DyeColor getHeldGlowStickColor(LocalPlayer player) {
    DyeColor mainHandColor = getGlowStickColor(player.getMainHandItem());
    if (mainHandColor != null) {
      return mainHandColor;
    }

    return getGlowStickColor(player.getOffhandItem());
  }

  private static DyeColor getGlowStickColor(ItemStack itemStack) {
    if (itemStack.getItem() instanceof GlowStickItem glowStickItem) {
      return glowStickItem.getDyeColor();
    }
    if (itemStack.getItem() instanceof CreativeGlowStickItem creativeGlowStickItem) {
      return creativeGlowStickItem.getDyeColor();
    }

    return null;
  }

  private static void spawnWaypointParticles(
    ClientLevel clientLevel, BlockPos blockPos, RandomSource random, DyeColor glowStickColor) {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) {
      return;
    }

    TrailParameters trail = calculateTrailParameters(player, blockPos);
    if (trail.distance < 2.0) {
      return;
    }

    spawnWaypointTrail(clientLevel, random, calculateWaypointColor(glowStickColor), trail);
  }

  private static int calculateWaypointColor(DyeColor glowStickColor) {
    float[] baseColors = GlowStickColors.getRgbComponents(glowStickColor);
    float colorMultiplier =
      Math.max(baseColors[0], Math.max(baseColors[1], baseColors[2])) > 0.3f ? 1.8f : 2.5f;

    return ARGB.colorFromFloat(
      1.0f,
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
    ClientLevel clientLevel, RandomSource random, int particleColor, TrailParameters trail) {
    int baseWaypoints = Math.max(8, Math.min(40, (int) (trail.distance * 2.2)));
    double minSpawnDistance = 1.0;
    spawnMainTrail(
      clientLevel,
      random,
      particleColor,
      trail,
      baseWaypoints,
      calculateParticlesPerWaypoint(trail.distance),
      1.0 / (baseWaypoints + 1),
      minSpawnDistance);
    spawnTargetParticles(clientLevel, random, particleColor, trail);
    spawnHelperParticles(clientLevel, random, particleColor, trail, baseWaypoints,
      minSpawnDistance);
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
    int particleColor,
    TrailParameters trail,
    int baseWaypoints,
    int particlesPerWaypoint,
    double waypointStep,
    double minSpawnDistance) {
    for (int i = 1; i <= baseWaypoints; i++) {
      double progress = i * waypointStep;
      if (progress * trail.distance < minSpawnDistance) {
        continue;
      }

      double waypointX =
        trail.startX + (trail.deltaX * progress) + (random.nextGaussian() - 0.5) * 0.06;
      double waypointY =
        trail.startY + (trail.deltaY * progress) + (random.nextGaussian() - 0.5) * 0.06;
      double waypointZ =
        trail.startZ + (trail.deltaZ * progress) + (random.nextGaussian() - 0.5) * 0.06;
      for (int j = 0; j < particlesPerWaypoint; j++) {
        float particleSize =
          (float) Math.min(2.0f, 0.9f + (trail.distance * 0.08f) - (progress * 0.2f));

        clientLevel.addParticle(
          new DustParticleOptions(particleColor, particleSize),
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
    ClientLevel clientLevel, RandomSource random, int particleColor, TrailParameters trail) {
    int targetParticleCount = Math.max(3, Math.min(8, (int) (trail.distance * 0.5)));
    for (int i = 0; i < targetParticleCount; i++) {
      clientLevel.addParticle(
        new DustParticleOptions(particleColor, 1.5f),
        trail.targetX + (random.nextGaussian() - 0.5) * 0.2,
        trail.targetY - 0.35 + random.nextFloat() * 0.3,
        trail.targetZ + (random.nextGaussian() - 0.5) * 0.2,
        (random.nextGaussian() - 0.5) * 0.004,
        0.004 + random.nextGaussian() * 0.002,
        (random.nextGaussian() - 0.5) * 0.004);
    }
  }

  private static void spawnHelperParticles(
    ClientLevel clientLevel,
    RandomSource random,
    int particleColor,
    TrailParameters trail,
    int baseWaypoints,
    double minSpawnDistance) {
    if (trail.distance <= 12.0) {
      return;
    }

    int helperParticleCount = Math.min(5, baseWaypoints / 8);
    for (int i = 0; i < helperParticleCount; i++) {
      double randomProgress = 0.3 + (random.nextDouble() * 0.4);
      if (randomProgress * trail.distance < minSpawnDistance) {
        continue;
      }

      clientLevel.addParticle(
        new DustParticleOptions(particleColor, 0.8f),
        trail.startX + (trail.deltaX * randomProgress) + (random.nextGaussian() - 0.5) * 0.12,
        trail.startY + (trail.deltaY * randomProgress) + (random.nextGaussian() - 0.5) * 0.12,
        trail.startZ + (trail.deltaZ * randomProgress) + (random.nextGaussian() - 0.5) * 0.12,
        (random.nextGaussian() - 0.5) * 0.002,
        0.002,
        (random.nextGaussian() - 0.5) * 0.002);
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
