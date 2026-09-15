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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class ModItems {

  protected static final Map<GlowStickColor, Item> GLOW_STICK_ITEMS =
    new EnumMap<>(GlowStickColor.class);

  protected static final Map<GlowStickColor, Item> CREATIVE_GLOW_STICK_ITEMS =
    new EnumMap<>(GlowStickColor.class);

  public static void registerItems() {
    for (GlowStickColor glowStickColor : GlowStickColor.values()) {
      String colorName = glowStickColor.getName();

      String itemName = "glow_stick_" + colorName;
      Item glowStickItem =
        new GlowStickItemWrapper(
          GlowStickItemProperties.createGlowStickItemProperties(itemName), glowStickColor);
      GLOW_STICK_ITEMS.put(glowStickColor, glowStickItem);
      Registry.register(
        BuiltInRegistries.ITEM,
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, itemName),
        glowStickItem);

      String creativeItemName = "creative_glow_stick_" + colorName;
      Item creativeGlowStickItem =
        new CreativeGlowStickItemWrapper(
          GlowStickItemProperties.createGlowStickItemProperties(creativeItemName),
          glowStickColor);
      CREATIVE_GLOW_STICK_ITEMS.put(glowStickColor, creativeGlowStickItem);
      Registry.register(
        BuiltInRegistries.ITEM,
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, creativeItemName),
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
