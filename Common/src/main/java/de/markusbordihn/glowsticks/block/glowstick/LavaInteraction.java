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

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class LavaInteraction {

  private static final float LAVA_EXPLOSION_STRENGTH = 0.5F;

  public static boolean isLavaBlock(BlockState blockState) {
    return blockState.is(Blocks.LAVA) || blockState.getFluidState().is(Fluids.LAVA);
  }

  public static void handleLavaDestruction(Level level, BlockPos lavaPosition, Entity source) {
    level.explode(
      source,
      lavaPosition.getX() + 0.5,
      lavaPosition.getY() + 0.5,
      lavaPosition.getZ() + 0.5,
      LAVA_EXPLOSION_STRENGTH,
      false,
      Level.ExplosionInteraction.NONE);

    level.playSound(
      null, lavaPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
    level.playSound(null, lavaPosition, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.18F, 0.8F);
  }
}
