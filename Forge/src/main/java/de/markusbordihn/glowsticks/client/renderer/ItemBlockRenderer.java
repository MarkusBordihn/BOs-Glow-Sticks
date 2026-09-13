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

package de.markusbordihn.glowsticks.client.renderer;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.item.GlowStickColors;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ItemBlockRenderer {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected ItemBlockRenderer() {
  }

  @SubscribeEvent
  public static void registerItemRenderer(final FMLClientSetupEvent event) {
    log.info("{} Item Block Renderer Setup ...", Constants.LOG_REGISTER_PREFIX);
  }

  @SubscribeEvent
  public static void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
    for (DyeColor dyeColor : DyeColor.values()) {
      RegistryObject<Block> glowStickBlock = ModBlocks.getGlowStickBlock(dyeColor);
      RegistryObject<Block> creativeGlowStickBlock = ModBlocks.getCreativeGlowStickBlock(dyeColor);
      if (glowStickBlock != null && creativeGlowStickBlock != null) {
        event.register(
          List.of(BlockTintSources.constant(GlowStickColors.getOpaqueArgb(dyeColor))),
          glowStickBlock.get(),
          creativeGlowStickBlock.get());
      }
    }
  }
}
