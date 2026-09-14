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

package de.markusbordihn.glowsticks.item;

import de.markusbordihn.glowsticks.Constants;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {

  protected static final Map<GlowStickColor, Item> GLOW_STICK_ITEMS =
      new EnumMap<>(GlowStickColor.class);

  protected static final Map<GlowStickColor, Item> CREATIVE_GLOW_STICK_ITEMS =
      new EnumMap<>(GlowStickColor.class);

  public static void registerItems() {
    for (GlowStickColor glowStickColor : GlowStickColor.values()) {
      String colorName = glowStickColor.getName();
      Item glowStickItem = new GlowStickItemWrapper(new Item.Properties(), glowStickColor);
      Item creativeGlowStickItem =
          new CreativeGlowStickItemWrapper(new Item.Properties(), glowStickColor);

      GLOW_STICK_ITEMS.put(glowStickColor, glowStickItem);
      CREATIVE_GLOW_STICK_ITEMS.put(glowStickColor, creativeGlowStickItem);

      Registry.register(
          BuiltInRegistries.ITEM,
          ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "glow_stick_" + colorName),
          glowStickItem);

      Registry.register(
          BuiltInRegistries.ITEM,
          ResourceLocation.fromNamespaceAndPath(
              Constants.MOD_ID, "creative_glow_stick_" + colorName),
          creativeGlowStickItem);
    }
  }

  public static Item getGlowStickItem(final GlowStickColor glowStickColor) {
    return GLOW_STICK_ITEMS.get(glowStickColor);
  }

  public static Item getCreativeGlowStickItem(final GlowStickColor glowStickColor) {
    return CREATIVE_GLOW_STICK_ITEMS.get(glowStickColor);
  }
}
