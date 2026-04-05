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
import de.markusbordihn.glowsticks.item.GlowStickItem;
import de.markusbordihn.glowsticks.item.ModItems;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientRenderer {
  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected ClientRenderer() {}

  public static void registerItemRenderer() {
    log.info("{} Client Setup ...", Constants.LOG_REGISTER_PREFIX);

    ResourceLocation animationStep = new ResourceLocation(Constants.MOD_ID, GlowStickItem.TAG_STEP);

    // Register animation steps for all glow stick colors
    for (DyeColor dyeColor : DyeColor.values()) {
      ItemProperties.register(
          ModItems.getGlowStickItem(dyeColor), animationStep, ClientRenderer::getStepFromTag);
    }

    // Register render layers for all glow stick blocks
    for (DyeColor dyeColor : DyeColor.values()) {
      BlockRenderLayerMap.INSTANCE.putBlock(
          ModBlocks.getGlowStickBlock(dyeColor), RenderType.translucent());
      BlockRenderLayerMap.INSTANCE.putBlock(
          ModBlocks.getCreativeGlowStickBlock(dyeColor), RenderType.translucent());
    }

    // Glow Stick Light Blocks (cutout mip)
    BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GLOW_STICK_LIGHT, RenderType.cutoutMipped());
    BlockRenderLayerMap.INSTANCE.putBlock(
        ModBlocks.GLOW_STICK_LIGHT_WATER, RenderType.cutoutMipped());
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

  public static void registerRenderers() {
    log.info("{} Client Renderer ...", Constants.LOG_REGISTER_PREFIX);

    // Register renderer for all glow stick entity colors
    for (DyeColor dyeColor : DyeColor.values()) {
      EntityRendererRegistry.register(
          ModEntity.getGlowStickEntity(dyeColor), ThrownItemRenderer::new);
    }
  }
}
