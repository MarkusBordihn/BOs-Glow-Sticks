/**
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

import net.minecraft.world.item.Item;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.tabs.GlowStickTab;

public class ModItems {

  protected ModItems() {}

  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

  // Glow Sticks
  public static final RegistryObject<Item> GLOW_STICK_WHITE = ITEMS.register("glow_stick_white",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_WHITE));
  public static final RegistryObject<Item> GLOW_STICK_ORANGE = ITEMS.register("glow_stick_orange",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_ORANGE));
  public static final RegistryObject<Item> GLOW_STICK_MAGENTA = ITEMS.register("glow_stick_magenta",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_MAGENTA));
  public static final RegistryObject<Item> GLOW_STICK_LIGHT_BLUE =
      ITEMS.register("glow_stick_light_blue",
          () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
              ModBlocks.GLOW_STICK_LIGHT_BLUE));
  public static final RegistryObject<Item> GLOW_STICK_YELLOW = ITEMS.register("glow_stick_yellow",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_YELLOW));
  public static final RegistryObject<Item> GLOW_STICK_LIME = ITEMS.register("glow_stick_lime",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_LIME));
  public static final RegistryObject<Item> GLOW_STICK_PINK = ITEMS.register("glow_stick_pink",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_PINK));
  public static final RegistryObject<Item> GLOW_STICK_GRAY = ITEMS.register("glow_stick_gray",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_GRAY));
  public static final RegistryObject<Item> GLOW_STICK_LIGHT_GRAY =
      ITEMS.register("glow_stick_light_gray",
          () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
              ModBlocks.GLOW_STICK_LIGHT_GRAY));
  public static final RegistryObject<Item> GLOW_STICK_CYAN = ITEMS.register("glow_stick_cyan",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_CYAN));
  public static final RegistryObject<Item> GLOW_STICK_PURPLE = ITEMS.register("glow_stick_purple",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_PURPLE));
  public static final RegistryObject<Item> GLOW_STICK_BLUE = ITEMS.register("glow_stick_blue",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_BLUE));
  public static final RegistryObject<Item> GLOW_STICK_BROWN = ITEMS.register("glow_stick_brown",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_BROWN));
  public static final RegistryObject<Item> GLOW_STICK_GREEN = ITEMS.register("glow_stick_green",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_GREEN));
  public static final RegistryObject<Item> GLOW_STICK_RED = ITEMS.register("glow_stick_red",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_RED));
  public static final RegistryObject<Item> GLOW_STICK_BLACK = ITEMS.register("glow_stick_black",
      () -> new GlowStickItem(new Item.Properties().tab(GlowStickTab.GLOW_STICKS),
          ModBlocks.GLOW_STICK_BLACK));
}
