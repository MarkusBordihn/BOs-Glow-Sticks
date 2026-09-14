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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

  public static final DeferredRegister<Block> BLOCKS =
    DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
  public static final RegistryObject<Block> GLOW_STICK_LIGHT =
    BLOCKS.register(
      "glow_stick_light",
      () ->
        new GlowStickLightBlock(
          GlowStickBlockProperties.createLightBlockProperties("glow_stick_light")));
  public static final RegistryObject<Block> GLOW_STICK_LIGHT_WATER =
    BLOCKS.register(
      "glow_stick_light_water",
      () ->
        new GlowStickLightWaterBlock(
          GlowStickBlockProperties.createLightBlockProperties("glow_stick_light_water")));
  protected static final Map<GlowStickColor, RegistryObject<Block>> GLOW_STICK_BLOCKS =
    new EnumMap<>(GlowStickColor.class);
  protected static final Map<GlowStickColor, RegistryObject<Block>> CREATIVE_GLOW_STICK_BLOCKS =
    new EnumMap<>(GlowStickColor.class);

  static {
    for (GlowStickColor glowStickColor : GlowStickColor.values()) {
      String colorName = glowStickColor.getName();

      String blockName = "glow_stick_" + colorName;
      GLOW_STICK_BLOCKS.put(
        glowStickColor,
        BLOCKS.register(
          blockName,
          () ->
            new GlowStickBlock(
              GlowStickBlockProperties.createGlowStickBlockProperties(blockName), glowStickColor)));

      String creativeBlockName = "creative_glow_stick_" + colorName;
      CREATIVE_GLOW_STICK_BLOCKS.put(
        glowStickColor,
        BLOCKS.register(
          creativeBlockName,
          () ->
            new CreativeGlowStickBlock(
              GlowStickBlockProperties.createCreativeGlowStickBlockProperties(creativeBlockName),
              glowStickColor)));
    }
  }

  public static RegistryObject<Block> getGlowStickBlock(final GlowStickColor glowStickColor) {
    return GLOW_STICK_BLOCKS.get(glowStickColor);
  }

  public static RegistryObject<Block> getCreativeGlowStickBlock(
    final GlowStickColor glowStickColor) {
    return CREATIVE_GLOW_STICK_BLOCKS.get(glowStickColor);
  }
}
