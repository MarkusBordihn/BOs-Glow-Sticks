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

package de.markusbordihn.glowsticks.tabs;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModTabs {

  public static final CreativeModeTab GLOW_STICKS_TAB =
      FabricItemGroup.builder()
          .icon(() -> new ItemStack(ModItems.getGlowStickItem(DyeColor.WHITE)))
          .title(Component.translatable("itemGroup." + Constants.MOD_ID + ".tab"))
          .displayItems(
              (displayContext, entries) -> {
                for (DyeColor dyeColor : DyeColor.values()) {
                  entries.accept(ModItems.getGlowStickItem(dyeColor));
                }
              })
          .build();
  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected ModTabs() {}

  public static void registerModTabs() {
    log.info("{} Glow Sticks Tabs ...", Constants.LOG_SUB_REGISTER_PREFIX);
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "tab"),
        GLOW_STICKS_TAB);
  }
}
