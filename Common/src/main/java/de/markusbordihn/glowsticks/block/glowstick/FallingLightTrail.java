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

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.block.GlowStickLightBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FallingLightTrail {

  private static final int MIN_FALL_DISTANCE = 2;
  private static final int MAX_FALL_DISTANCE = 64;
  private static final int MAX_TRAIL_LIGHTS = 3;
  private static final int MIN_LIGHT_SPACING = 3;
  private static final double FALLING_BLOCK_GRAVITY = 0.04;
  private static final double FALLING_BLOCK_DRAG = 0.98;

  private static GlowStickLightBlock lightBlock;
  private static GlowStickLightBlock lightWaterBlock;

  public static void placeTrail(ServerLevel serverLevel, BlockPos fallStartPos) {
    int fallDistance = measureFallDistance(serverLevel, fallStartPos);
    if (fallDistance < MIN_FALL_DISTANCE) {
      return;
    }

    int lightSpacing = Math.max(MIN_LIGHT_SPACING, fallDistance / MAX_TRAIL_LIGHTS);
    for (int fallDepth = 0; fallDepth < fallDistance; fallDepth += lightSpacing) {
      placeTrailLight(serverLevel, fallStartPos.below(fallDepth), estimateFallTicks(fallDepth));
    }
  }

  private static int measureFallDistance(ServerLevel serverLevel, BlockPos fallStartPos) {
    BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();
    for (int fallDepth = 1; fallDepth <= MAX_FALL_DISTANCE; fallDepth++) {
      scanPos.set(fallStartPos.getX(), fallStartPos.getY() - fallDepth, fallStartPos.getZ());
      if (scanPos.getY() < serverLevel.getMinBuildHeight()
          || !FallingBlock.isFree(serverLevel.getBlockState(scanPos))) {
        return fallDepth - 1;
      }
    }

    return MAX_FALL_DISTANCE;
  }

  private static int estimateFallTicks(int fallDepth) {
    double fallSpeed = 0.0;
    double fallenDistance = 0.0;
    int fallTicks = 0;
    while (fallenDistance < fallDepth) {
      fallSpeed = (fallSpeed + FALLING_BLOCK_GRAVITY) * FALLING_BLOCK_DRAG;
      fallenDistance += fallSpeed;
      fallTicks++;
    }

    return fallTicks;
  }

  private static void placeTrailLight(
      ServerLevel serverLevel, BlockPos lightPos, int additionalLifetimeTicks) {
    BlockState currentBlockState = serverLevel.getBlockState(lightPos);
    boolean isWaterPosition = currentBlockState.is(Blocks.WATER);
    if (!currentBlockState.isAir() && !isWaterPosition) {
      return;
    }

    GlowStickLightBlock glowStickLightBlock =
        isWaterPosition ? getLightWaterBlock() : getLightBlock();
    if (glowStickLightBlock == null) {
      return;
    }

    serverLevel.setBlockAndUpdate(lightPos, glowStickLightBlock.defaultBlockState());
    glowStickLightBlock.scheduleTick(serverLevel, lightPos, additionalLifetimeTicks);
  }

  private static GlowStickLightBlock getLightBlock() {
    if (lightBlock == null) {
      lightBlock = resolveLightBlock("glow_stick_light");
    }

    return lightBlock;
  }

  private static GlowStickLightBlock getLightWaterBlock() {
    if (lightWaterBlock == null) {
      lightWaterBlock = resolveLightBlock("glow_stick_light_water");
    }

    return lightWaterBlock;
  }

  private static GlowStickLightBlock resolveLightBlock(String registryName) {
    Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(Constants.MOD_ID, registryName));
    return block instanceof GlowStickLightBlock glowStickLightBlock ? glowStickLightBlock : null;
  }
}
