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
import de.markusbordihn.glowsticks.entity.projectile.GlowStickProjectile;
import de.markusbordihn.glowsticks.item.GlowStickColors;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import de.markusbordihn.glowsticks.item.ModItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
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
          for (DyeColor dyeColor : DyeColor.values()) {
            RegistryObject<Item> glowStickItem = ModItems.getGlowStickItem(dyeColor);
            if (glowStickItem != null) {
              ItemProperties.register(
                  glowStickItem.get(),
                  GlowStickData.STEP_PREDICATE,
                  GlowStickItem::getStepFromDataComponent);
              ItemProperties.register(
                  glowStickItem.get(),
                  GlowStickData.ACTIVATED_PREDICATE,
                  GlowStickItem::getActivatedFromDataComponent);
            }
          }

          for (DyeColor dyeColor : DyeColor.values()) {
            RegistryObject<Block> glowStickBlock = ModBlocks.getGlowStickBlock(dyeColor);
            if (glowStickBlock != null) {
              ItemBlockRenderTypes.setRenderLayer(glowStickBlock.get(), RenderType.translucent());
            }
            RegistryObject<Block> creativeGlowStickBlock =
                ModBlocks.getCreativeGlowStickBlock(dyeColor);
            if (creativeGlowStickBlock != null) {
              ItemBlockRenderTypes.setRenderLayer(
                  creativeGlowStickBlock.get(), RenderType.translucent());
            }
          }

          ItemBlockRenderTypes.setRenderLayer(
              ModBlocks.GLOW_STICK_LIGHT.get(), RenderType.cutoutMipped());
          ItemBlockRenderTypes.setRenderLayer(
              ModBlocks.GLOW_STICK_LIGHT_WATER.get(), RenderType.cutoutMipped());
        });
  }

  @SubscribeEvent
  public static void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
    for (DyeColor dyeColor : DyeColor.values()) {
      int rgb = GlowStickColors.getRgb(dyeColor);
      RegistryObject<Block> glowStickBlock = ModBlocks.getGlowStickBlock(dyeColor);
      RegistryObject<Block> creativeGlowStickBlock = ModBlocks.getCreativeGlowStickBlock(dyeColor);
      if (glowStickBlock != null && creativeGlowStickBlock != null) {
        event.register(
            (blockState, blockAndTintGetter, blockPos, tintIndex) -> rgb,
            glowStickBlock.get(),
            creativeGlowStickBlock.get());
      }
    }
  }

  @SubscribeEvent
  public static void registerItemColors(final RegisterColorHandlersEvent.Item event) {
    for (DyeColor dyeColor : DyeColor.values()) {
      int opaqueArgb = GlowStickColors.getOpaqueArgb(dyeColor);
      RegistryObject<Item> glowStickItem = ModItems.getGlowStickItem(dyeColor);
      RegistryObject<Item> creativeGlowStickItem = ModItems.getCreativeGlowStickItem(dyeColor);
      if (glowStickItem != null && creativeGlowStickItem != null) {
        event.register(
            (itemStack, tintIndex) -> opaqueArgb, glowStickItem.get(), creativeGlowStickItem.get());
      }
    }
  }

  @SubscribeEvent
  public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
    log.info("{} Client Renderer ...", Constants.LOG_REGISTER_PREFIX);

    for (DyeColor dyeColor : DyeColor.values()) {
      RegistryObject<EntityType<GlowStickProjectile>> glowStickEntity =
          ModEntity.getGlowStickEntity(dyeColor);
      if (glowStickEntity != null) {
        event.registerEntityRenderer(glowStickEntity.get(), GlowStickProjectileRenderer::new);
      }
    }
  }
}
