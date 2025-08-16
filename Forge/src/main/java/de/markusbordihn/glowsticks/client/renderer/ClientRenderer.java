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
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.entity.projectile.GlowStickProjectile;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import de.markusbordihn.glowsticks.item.ModItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
          ResourceLocation animationStep =
              new ResourceLocation(Constants.MOD_ID, GlowStickItem.TAG_STEP);

          // Register animation steps for all glow stick colors
          for (DyeColor dyeColor : DyeColor.values()) {
            RegistryObject<Item> glowStickItem = ModItems.getGlowStickItem(dyeColor);
            if (glowStickItem != null) {
              ItemProperties.register(
                  glowStickItem.get(), animationStep, ClientRenderer::getStepFromTag);
            }
          }

          // Register render layers for all glow stick blocks
          for (DyeColor dyeColor : DyeColor.values()) {
            RegistryObject<Block> glowStickBlock = ModBlocks.getGlowStickBlock(dyeColor);
            if (glowStickBlock != null) {
              ItemBlockRenderTypes.setRenderLayer(glowStickBlock.get(), RenderType.translucent());
            }
          }

          // Glow Stick Light Blocks (cutout mip)
          ItemBlockRenderTypes.setRenderLayer(
              ModBlocks.GLOW_STICK_LIGHT.get(), RenderType.cutoutMipped());
          ItemBlockRenderTypes.setRenderLayer(
              ModBlocks.GLOW_STICK_LIGHT_WATER.get(), RenderType.cutoutMipped());
        });
  }

  public static float getStepFromTag(
      final ItemStack itemStack, final ClientLevel level, final LivingEntity living, final int id) {
    if (itemStack.getItem() instanceof GlowStickItem && living != null) {
      CompoundTag compoundTag = itemStack.getTag();
      if (compoundTag != null) {
        return compoundTag.getInt(GlowStickItem.TAG_STEP);
      }
    }
    return 0.0F;
  }

  @SubscribeEvent
  public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
    log.info("{} Client Renderer ...", Constants.LOG_REGISTER_PREFIX);

    // Register renderer for all glow stick entity colors
    for (DyeColor dyeColor : DyeColor.values()) {
      RegistryObject<EntityType<GlowStickProjectile>> glowStickEntity =
          ModEntity.getGlowStickEntity(dyeColor);
      if (glowStickEntity != null) {
        event.registerEntityRenderer(glowStickEntity.get(), ThrownItemRenderer::new);
      }
    }
  }
}
