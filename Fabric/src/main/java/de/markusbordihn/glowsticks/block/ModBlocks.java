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
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public class ModBlocks {

  // Light blocks
  public static final Block GLOW_STICK_LIGHT =
      new GlowStickLightBlock(GlowStickBlockProperties.createLightBlockProperties());
  public static final Block GLOW_STICK_LIGHT_WATER =
      new GlowStickLightWaterBlock(GlowStickBlockProperties.createLightBlockProperties());
  protected static final Map<DyeColor, Block> GLOW_STICK_BLOCKS = new EnumMap<>(DyeColor.class);
  protected static final Map<DyeColor, Block> CREATIVE_GLOW_STICK_BLOCKS =
      new EnumMap<>(DyeColor.class);

  public static void registerBlocks() {
    // Register light blocks
    Registry.register(
        BuiltInRegistries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "glow_stick_light"),
        GLOW_STICK_LIGHT);
    Registry.register(
        BuiltInRegistries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "glow_stick_light_water"),
        GLOW_STICK_LIGHT_WATER);

    // Register all glow stick blocks for each dye color
    for (DyeColor dyeColor : DyeColor.values()) {
      String colorName = dyeColor.getName();

      // Normal glow stick blocks
      GLOW_STICK_BLOCKS.put(
          dyeColor,
          Registry.register(
              BuiltInRegistries.BLOCK,
              ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "glow_stick_" + colorName),
              new GlowStickBlock(
                  GlowStickBlockProperties.createGlowStickBlockProperties(),
                  dyeColor,
                  GlowSticksConfig.despawnTicks)));

      // Creative glow stick blocks (despawnTickRate = 0 means no despawn/no aging)
      CREATIVE_GLOW_STICK_BLOCKS.put(
          dyeColor,
          Registry.register(
              BuiltInRegistries.BLOCK,
              ResourceLocation.fromNamespaceAndPath(
                  Constants.MOD_ID, "creative_glow_stick_" + colorName),
              new GlowStickBlock(
                  GlowStickBlockProperties.createGlowStickBlockProperties(), dyeColor, 0)));
    }
  }

  public static Block getGlowStickBlock(final DyeColor dyeColor) {
    return GLOW_STICK_BLOCKS.get(dyeColor);
  }

  public static Block getCreativeGlowStickBlock(final DyeColor dyeColor) {
    return CREATIVE_GLOW_STICK_BLOCKS.get(dyeColor);
  }
}
