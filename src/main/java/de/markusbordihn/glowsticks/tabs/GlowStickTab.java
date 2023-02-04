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

package de.markusbordihn.glowsticks.tabs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.Output;

import net.minecraftforge.event.CreativeModeTabEvent;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.item.ModItems;

public class GlowStickTab {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected GlowStickTab() {}

  protected static CreativeModeTab GLOW_STICKS;

  public static void handleCreativeModeTabRegister(CreativeModeTabEvent.Register event) {

    log.info("{} creative mod tabs ...", Constants.LOG_REGISTER_PREFIX);

    GLOW_STICKS = event
        .registerCreativeModeTab(new ResourceLocation(Constants.MOD_ID, "glow_sticks"), builder -> {
          builder.icon(() -> new ItemStack(ModItems.GLOW_STICK_GREEN.get()))
              .displayItems(GlowStickTab::addGlowSticksTabItems)
              .title(Component.translatable("itemGroup.material_elements.glow_sticks")).build();
        });
  }

  private static void addGlowSticksTabItems(FeatureFlagSet featureFlagSet, Output outputTab,
      boolean hasPermissions) {
    // Glow Sticks
    outputTab.accept(ModItems.GLOW_STICK_WHITE.get());
    outputTab.accept(ModItems.GLOW_STICK_ORANGE.get());
    outputTab.accept(ModItems.GLOW_STICK_MAGENTA.get());
    outputTab.accept(ModItems.GLOW_STICK_LIGHT_BLUE.get());
    outputTab.accept(ModItems.GLOW_STICK_YELLOW.get());
    outputTab.accept(ModItems.GLOW_STICK_LIME.get());
    outputTab.accept(ModItems.GLOW_STICK_PINK.get());
    outputTab.accept(ModItems.GLOW_STICK_GRAY.get());
    outputTab.accept(ModItems.GLOW_STICK_LIGHT_GRAY.get());
    outputTab.accept(ModItems.GLOW_STICK_CYAN.get());
    outputTab.accept(ModItems.GLOW_STICK_PURPLE.get());
    outputTab.accept(ModItems.GLOW_STICK_BLUE.get());
    outputTab.accept(ModItems.GLOW_STICK_BROWN.get());
    outputTab.accept(ModItems.GLOW_STICK_GREEN.get());
    outputTab.accept(ModItems.GLOW_STICK_RED.get());
    outputTab.accept(ModItems.GLOW_STICK_BLACK.get());
  }
}
