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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public class ModItems {

  protected static final Map<DyeColor, Item> GLOW_STICK_ITEMS = new EnumMap<>(DyeColor.class);

  public static void registerItems() {
    // Register all glow stick items for each dye color
    for (DyeColor dyeColor : DyeColor.values()) {
      String colorName = dyeColor.getName();
      String itemId = "glow_stick_" + colorName;
      Item glowStickItem =
          new GlowStickItemWrapper(
              GlowStickItemProperties.createGlowStickItemProperties(itemId), dyeColor);

      GLOW_STICK_ITEMS.put(dyeColor, glowStickItem);
      Registry.register(
          BuiltInRegistries.ITEM,
          ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, itemId),
          glowStickItem);
    }
  }

  public static Item getGlowStickItem(final DyeColor dyeColor) {
    return GLOW_STICK_ITEMS.get(dyeColor);
  }
}
