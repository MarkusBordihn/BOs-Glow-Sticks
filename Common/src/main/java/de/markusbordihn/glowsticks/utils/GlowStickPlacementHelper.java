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

package de.markusbordihn.glowsticks.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public final class GlowStickPlacementHelper {

  private static final float PLACEMENT_SOUND_VOLUME = 0.3F;
  private static final float PITCH_VARIATION_FACTOR = 0.2F;
  private static final float LAVA_EXPLOSION_STRENGTH = 0.5F;

  private GlowStickPlacementHelper() {}

  public static boolean isLavaBlock(final BlockState blockState) {
    return blockState.is(Blocks.LAVA) || blockState.getFluidState().is(Fluids.LAVA);
  }

  public static void createLavaExplosionEffect(final Level level, final BlockPos position) {
    level.explode(
        null,
        position.getX() + 0.5,
        position.getY() + 0.5,
        position.getZ() + 0.5,
        LAVA_EXPLOSION_STRENGTH,
        false,
        Level.ExplosionInteraction.NONE);
  }

  public static void playLavaInteractionSounds(final Level level, final BlockPos position) {
    level.playSound(null, position, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
    level.playSound(null, position, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.18F, 0.8F);
  }

  public static void handleLavaDestruction(final Level level, final BlockPos lavaPosition) {
    createLavaExplosionEffect(level, lavaPosition);
    playLavaInteractionSounds(level, lavaPosition);
  }

  private static float calculatePitchWithVariation(final RandomSource random) {
    return 1.0F + (random.nextFloat() - 0.5F) * PITCH_VARIATION_FACTOR;
  }

  private static void playSound(
      final Level level,
      final BlockPos position,
      final SoundEvent soundEvent,
      final float pitch,
      final float volume) {
    level.playSound(null, position, soundEvent, SoundSource.BLOCKS, volume, pitch);
  }

  private static boolean isStoneLikeSurface(
      final Block surfaceBlock, final BlockState surfaceState) {
    return surfaceBlock == Blocks.STONE
        || surfaceBlock == Blocks.COBBLESTONE
        || surfaceBlock == Blocks.DEEPSLATE
        || surfaceState.is(BlockTags.STONE_BRICKS);
  }

  private static boolean isDirtLikeSurface(
      final Block surfaceBlock, final BlockState surfaceState) {
    return surfaceBlock == Blocks.GRASS_BLOCK
        || surfaceBlock == Blocks.DIRT
        || surfaceState.is(BlockTags.DIRT);
  }

  private static boolean isWoodLikeSurface(final BlockState surfaceState) {
    return surfaceState.is(BlockTags.LOGS) || surfaceState.is(BlockTags.PLANKS);
  }

  private static boolean isSandLikeSurface(final Block surfaceBlock) {
    return surfaceBlock == Blocks.SAND
        || surfaceBlock == Blocks.RED_SAND
        || surfaceBlock == Blocks.GRAVEL;
  }

  private static boolean isLeafSurface(final BlockState surfaceState) {
    return surfaceState.is(BlockTags.LEAVES);
  }

  private static boolean isWaterSurface(final Block surfaceBlock, final BlockState surfaceState) {
    return surfaceBlock == Blocks.WATER || surfaceState.getFluidState().is(Fluids.WATER);
  }

  public static void playPlacementSound(
      final Level level,
      final BlockPos position,
      final BlockState surfaceState,
      final RandomSource random) {
    if (level.isClientSide) {
      return;
    }

    Block surfaceBlock = surfaceState.getBlock();
    float basePitch = calculatePitchWithVariation(random);

    if (isStoneLikeSurface(surfaceBlock, surfaceState)) {
      playSound(level, position, SoundEvents.METAL_PLACE, basePitch, PLACEMENT_SOUND_VOLUME);
    } else if (isDirtLikeSurface(surfaceBlock, surfaceState)) {
      playSound(level, position, SoundEvents.MOSS_PLACE, basePitch * 0.8F, PLACEMENT_SOUND_VOLUME);
    } else if (isWoodLikeSurface(surfaceState)) {
      playSound(level, position, SoundEvents.WOOD_PLACE, basePitch, PLACEMENT_SOUND_VOLUME);
    } else if (isSandLikeSurface(surfaceBlock)) {
      playSound(level, position, SoundEvents.SAND_PLACE, basePitch * 0.7F, PLACEMENT_SOUND_VOLUME);
    } else if (isLeafSurface(surfaceState)) {
      playSound(
          level,
          position,
          SoundEvents.GRASS_PLACE,
          basePitch * 1.2F,
          PLACEMENT_SOUND_VOLUME * 0.5F);
    } else if (isWaterSurface(surfaceBlock, surfaceState)) {
      playSound(
          level,
          position,
          SoundEvents.PLAYER_SPLASH_HIGH_SPEED,
          basePitch * 1.5F,
          PLACEMENT_SOUND_VOLUME * 0.4F);
    } else if (isLavaBlock(surfaceState)) {
      playSound(
          level, position, SoundEvents.LAVA_POP, basePitch * 0.8F, PLACEMENT_SOUND_VOLUME * 0.6F);
    } else {
      playSound(level, position, SoundEvents.SCAFFOLDING_HIT, basePitch, PLACEMENT_SOUND_VOLUME);
    }
  }
}
