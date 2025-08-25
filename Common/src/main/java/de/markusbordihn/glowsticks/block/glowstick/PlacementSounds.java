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

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PlacementSounds {

  private static final float PLACEMENT_SOUND_VOLUME = 0.3F;
  private static final float PITCH_VARIATION_FACTOR = 0.2F;

  public static void playPlacementSound(
      Level level, BlockPos position, BlockState surfaceState, Random random) {
    Block surfaceBlock = surfaceState.getBlock();
    float pitch = 1.0F + (random.nextFloat() - 0.5F) * PITCH_VARIATION_FACTOR;

    SoundEvent soundEvent = determinePlacementSound(surfaceBlock, surfaceState);
    level.playSound(null, position, soundEvent, SoundSource.BLOCKS, PLACEMENT_SOUND_VOLUME, pitch);
  }

  private static SoundEvent determinePlacementSound(Block surfaceBlock, BlockState surfaceState) {
    if (isStoneLikeSurface(surfaceBlock, surfaceState)) {
      return SoundEvents.STONE_PLACE;
    } else if (isDirtLikeSurface(surfaceBlock, surfaceState)) {
      return SoundEvents.GRAVEL_PLACE;
    } else if (surfaceState.is(BlockTags.LOGS) || surfaceState.is(BlockTags.PLANKS)) {
      return SoundEvents.WOOD_PLACE;
    } else if (isSandLikeSurface(surfaceBlock)) {
      return SoundEvents.SAND_PLACE;
    } else if (surfaceState.is(BlockTags.LEAVES)) {
      return SoundEvents.GRASS_PLACE;
    } else {
      return SoundEvents.STONE_PLACE;
    }
  }

  private static boolean isStoneLikeSurface(Block surfaceBlock, BlockState surfaceState) {
    return surfaceBlock == Blocks.STONE
        || surfaceBlock == Blocks.COBBLESTONE
        || surfaceBlock == Blocks.DEEPSLATE
        || surfaceState.is(BlockTags.STONE_BRICKS);
  }

  private static boolean isDirtLikeSurface(Block surfaceBlock, BlockState surfaceState) {
    return surfaceBlock == Blocks.GRASS_BLOCK
        || surfaceBlock == Blocks.DIRT
        || surfaceState.is(BlockTags.DIRT);
  }

  private static boolean isSandLikeSurface(Block surfaceBlock) {
    return surfaceBlock == Blocks.SAND
        || surfaceBlock == Blocks.RED_SAND
        || surfaceBlock == Blocks.GRAVEL;
  }
}
