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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

  public static final DeferredRegister<Item> ITEMS =
    DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

  protected static final Map<DyeColor, RegistryObject<Item>> GLOW_STICK_ITEMS =
    new EnumMap<>(DyeColor.class);

  protected static final Map<DyeColor, RegistryObject<Item>> CREATIVE_GLOW_STICK_ITEMS =
    new EnumMap<>(DyeColor.class);

  static {
    for (DyeColor dyeColor : DyeColor.values()) {
      String colorName = dyeColor.getName();

      // Normal glow stick items
      String itemName = "glow_stick_" + colorName;
      GLOW_STICK_ITEMS.put(
        dyeColor,
        ITEMS.register(
          itemName,
          () ->
            new GlowStickItemWrapper(
              GlowStickItemProperties.createGlowStickItemProperties(itemName), dyeColor)));

      // Creative glow stick items
      String creativeItemName = "creative_glow_stick_" + colorName;
      CREATIVE_GLOW_STICK_ITEMS.put(
        dyeColor,
        ITEMS.register(
          creativeItemName,
          () ->
            new CreativeGlowStickItemWrapper(
              GlowStickItemProperties.createGlowStickItemProperties(creativeItemName),
              dyeColor)));
    }
  }

  public static RegistryObject<Item> getGlowStickItem(final DyeColor dyeColor) {
    return GLOW_STICK_ITEMS.get(dyeColor);
  }

  public static RegistryObject<Item> getCreativeGlowStickItem(final DyeColor dyeColor) {
    return CREATIVE_GLOW_STICK_ITEMS.get(dyeColor);
  }
}
