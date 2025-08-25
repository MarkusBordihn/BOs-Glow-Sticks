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
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientRenderer {
  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected ClientRenderer() {}

  public static void registerItemRenderer(final FMLClientSetupEvent event) {
    log.info("{} Client Setup ...", Constants.LOG_REGISTER_PREFIX);

    event.enqueueWork(
        () -> {
          // Register render layers for all glow stick blocks
          for (DyeColor dyeColor : DyeColor.values()) {
            DeferredBlock<Block> glowStickBlock = ModBlocks.getGlowStickBlock(dyeColor);
            if (glowStickBlock != null) {
              ItemBlockRenderTypes.setRenderLayer(
                  glowStickBlock.get(), ChunkSectionLayer.TRANSLUCENT);
            }
            DeferredBlock<Block> creativeGlowStickBlock =
                ModBlocks.getCreativeGlowStickBlock(dyeColor);
            if (creativeGlowStickBlock != null) {
              ItemBlockRenderTypes.setRenderLayer(
                  creativeGlowStickBlock.get(), ChunkSectionLayer.TRANSLUCENT);
            }
          }

          // Glow Stick Light Blocks (cutout mip)
          ItemBlockRenderTypes.setRenderLayer(
              ModBlocks.GLOW_STICK_LIGHT.get(), ChunkSectionLayer.CUTOUT_MIPPED);
          ItemBlockRenderTypes.setRenderLayer(
              ModBlocks.GLOW_STICK_LIGHT_WATER.get(), ChunkSectionLayer.CUTOUT_MIPPED);
        });
  }

  public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
    log.info("{} Client Renderer ...", Constants.LOG_REGISTER_PREFIX);

    // Register renderer for all glow stick entity colors
    for (DyeColor dyeColor : DyeColor.values()) {
      DeferredHolder<EntityType<?>, EntityType<GlowStickProjectile>> glowStickEntity =
          ModEntity.getGlowStickEntity(dyeColor);
      if (glowStickEntity != null) {
        event.registerEntityRenderer(glowStickEntity.get(), ThrownItemRenderer::new);
      }
    }
  }
}
