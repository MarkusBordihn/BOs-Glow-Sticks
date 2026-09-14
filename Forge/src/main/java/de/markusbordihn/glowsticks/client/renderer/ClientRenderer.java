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
import de.markusbordihn.glowsticks.data.GlowStickData;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.item.GlowStickColor;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import de.markusbordihn.glowsticks.item.ModItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRenderer {
  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected ClientRenderer() {}

  public static void registerItemRenderer(final FMLClientSetupEvent event) {
    log.info("{} Client Setup ...", Constants.LOG_REGISTER_PREFIX);

    event.enqueueWork(
        () -> {
          for (GlowStickColor glowStickColor : GlowStickColor.values()) {
            Item glowStickItem = ModItems.getGlowStickItem(glowStickColor).get();
            ItemProperties.register(
                glowStickItem,
                GlowStickData.STEP_PREDICATE,
                GlowStickItem::getStepFromDataComponent);
            ItemProperties.register(
                glowStickItem,
                GlowStickData.ACTIVATED_PREDICATE,
                GlowStickItem::getActivatedFromDataComponent);
          }

          for (GlowStickColor glowStickColor : GlowStickColor.values()) {
            ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.getGlowStickBlock(glowStickColor).get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.getCreativeGlowStickBlock(glowStickColor).get(),
                RenderType.translucent());
          }

          ItemBlockRenderTypes.setRenderLayer(
              ModBlocks.GLOW_STICK_LIGHT.get(), RenderType.cutoutMipped());
          ItemBlockRenderTypes.setRenderLayer(
              ModBlocks.GLOW_STICK_LIGHT_WATER.get(), RenderType.cutoutMipped());
        });
  }

  @SubscribeEvent
  public static void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
    for (GlowStickColor glowStickColor : GlowStickColor.values()) {
      int rgb = glowStickColor.getRgb();
      event.register(
          (blockState, blockAndTintGetter, blockPos, tintIndex) -> rgb,
          ModBlocks.getGlowStickBlock(glowStickColor).get(),
          ModBlocks.getCreativeGlowStickBlock(glowStickColor).get());
    }
  }

  @SubscribeEvent
  public static void registerItemColors(final RegisterColorHandlersEvent.Item event) {
    for (GlowStickColor glowStickColor : GlowStickColor.values()) {
      int opaqueArgb = glowStickColor.getOpaqueArgb();
      event.register(
          (itemStack, tintIndex) -> opaqueArgb,
          ModItems.getGlowStickItem(glowStickColor).get(),
          ModItems.getCreativeGlowStickItem(glowStickColor).get());
    }
  }

  @SubscribeEvent
  public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
    log.info("{} Client Renderer ...", Constants.LOG_REGISTER_PREFIX);

    for (GlowStickColor glowStickColor : GlowStickColor.values()) {
      event.registerEntityRenderer(
          ModEntity.getGlowStickEntity(glowStickColor).get(), GlowStickProjectileRenderer::new);
    }
  }
}
