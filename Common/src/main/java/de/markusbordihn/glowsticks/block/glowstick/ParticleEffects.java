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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ParticleEffects {

  public static void handleParticleAnimation(
      BlockState blockState,
      Level level,
      BlockPos blockPos,
      RandomSource random,
      DyeColor dyeColor) {
    if (!(level instanceof ClientLevel clientLevel) || !shouldSpawnParticles(blockState, random)) {
      return;
    }

    int currentAge = blockState.getValue(GlowStickBlock.AGE);
    float ageFactor = Math.max(0.2f, 1.0f - (currentAge / 15.0f));
    float size = Math.max(0.8f, ageFactor * 1.2f);

    int adjustedColor = calculateAdjustedColor(dyeColor, ageFactor);
    spawnMainParticle(clientLevel, blockPos, random, adjustedColor, size);

    if (currentAge <= 5 && random.nextInt(3) == 0) {
      spawnExtraParticle(clientLevel, blockPos, random, adjustedColor, size);
    }
  }

  private static boolean shouldSpawnParticles(BlockState blockState, RandomSource random) {
    return GlowSticksConfig.spawnRandomParticles
        && random.nextInt(GlowSticksConfig.randomParticleSpawnRate) == 0
        && blockState.getValue(GlowStickBlock.AGE) < 15;
  }

  private static int calculateAdjustedColor(DyeColor dyeColor, float brightness) {
    int originalColor = dyeColor.getTextureDiffuseColor();
    int red = (originalColor >> 16) & 0xFF;
    int green = (originalColor >> 8) & 0xFF;
    int blue = originalColor & 0xFF;

    int adjustedRed = Math.min(255, (int) (red * brightness));
    int adjustedGreen = Math.min(255, (int) (green * brightness));
    int adjustedBlue = Math.min(255, (int) (blue * brightness));

    return (adjustedRed << 16) | (adjustedGreen << 8) | adjustedBlue;
  }

  private static void spawnMainParticle(
      ClientLevel clientLevel, BlockPos blockPos, RandomSource random, int color, float size) {
    clientLevel.addParticle(
        new DustParticleOptions(color, size),
        blockPos.getX() + 0.5 + (random.nextFloat() - 0.5) * 0.3,
        blockPos.getY() + 0.1,
        blockPos.getZ() + 0.5 + (random.nextFloat() - 0.5) * 0.3,
        0,
        0.01,
        0);
  }

  private static void spawnExtraParticle(
      ClientLevel clientLevel, BlockPos blockPos, RandomSource random, int color, float size) {
    clientLevel.addParticle(
        new DustParticleOptions(color, size * 0.7f),
        blockPos.getX() + 0.5 + (random.nextFloat() - 0.5) * 0.4,
        blockPos.getY() + 0.15,
        blockPos.getZ() + 0.5 + (random.nextFloat() - 0.5) * 0.4,
        0,
        0.005,
        0);
  }
}
