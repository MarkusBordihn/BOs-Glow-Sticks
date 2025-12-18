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

package de.markusbordihn.glowsticks.block;

import de.markusbordihn.glowsticks.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class GlowStickBlockProperties {

  public static BlockBehaviour.Properties createGlowStickBlockProperties(String blockId) {
    return BlockBehaviour.Properties.of()
      .mapColor(MapColor.NONE)
      .sound(SoundType.SCAFFOLDING)
      .noOcclusion()
      .lightLevel(GlowStickBlock::getLightLevel)
      .randomTicks()
      .setId(
        ResourceKey.create(
          Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, blockId)));
  }

  public static BlockBehaviour.Properties createLightBlockProperties(String blockId) {
    return BlockBehaviour.Properties.of()
      .mapColor(MapColor.NONE)
      .noCollision()
      .lightLevel(blockState -> 15)
      .randomTicks()
      .setId(
        ResourceKey.create(
          Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, blockId)));
  }
}
