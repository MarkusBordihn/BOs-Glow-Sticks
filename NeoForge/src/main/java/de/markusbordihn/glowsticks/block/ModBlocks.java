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
import de.markusbordihn.glowsticks.item.GlowStickColor;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

  public static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(Constants.MOD_ID);
  public static final DeferredBlock<Block> GLOW_STICK_LIGHT =
      BLOCKS.register(
          "glow_stick_light",
          () -> new GlowStickLightBlock(GlowStickBlockProperties.createLightBlockProperties()));
  public static final DeferredBlock<Block> GLOW_STICK_LIGHT_WATER =
      BLOCKS.register(
          "glow_stick_light_water",
          () ->
              new GlowStickLightWaterBlock(GlowStickBlockProperties.createLightBlockProperties()));
  protected static final Map<GlowStickColor, DeferredBlock<Block>> GLOW_STICK_BLOCKS =
      new EnumMap<>(GlowStickColor.class);
  protected static final Map<GlowStickColor, DeferredBlock<Block>> CREATIVE_GLOW_STICK_BLOCKS =
      new EnumMap<>(GlowStickColor.class);

  static {
    for (GlowStickColor glowStickColor : GlowStickColor.values()) {
      String colorName = glowStickColor.getName();
      GLOW_STICK_BLOCKS.put(
          glowStickColor,
          BLOCKS.register(
              "glow_stick_" + colorName,
              () ->
                  new GlowStickBlock(
                      GlowStickBlockProperties.createGlowStickBlockProperties(), glowStickColor)));

      CREATIVE_GLOW_STICK_BLOCKS.put(
          glowStickColor,
          BLOCKS.register(
              "creative_glow_stick_" + colorName,
              () ->
                  new CreativeGlowStickBlock(
                      GlowStickBlockProperties.createCreativeGlowStickBlockProperties(),
                      glowStickColor)));
    }
  }

  public static DeferredBlock<Block> getGlowStickBlock(final GlowStickColor glowStickColor) {
    return GLOW_STICK_BLOCKS.get(glowStickColor);
  }

  public static DeferredBlock<Block> getCreativeGlowStickBlock(
      final GlowStickColor glowStickColor) {
    return CREATIVE_GLOW_STICK_BLOCKS.get(glowStickColor);
  }
}
