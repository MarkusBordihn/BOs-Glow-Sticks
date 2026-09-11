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

import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class EmissiveBakedModel extends ForwardingBakedModel {

  private static final RenderContext.QuadTransform EMISSIVE_TRANSFORM =
      new RenderContext.QuadTransform() {
        private RenderMaterial material;

        @Override
        public boolean transform(MutableQuadView quad) {
          if (this.material == null) {
            this.material =
                RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(true).find();
          }
          quad.material(this.material);
          return true;
        }
      };

  public EmissiveBakedModel(BakedModel wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public boolean isVanillaAdapter() {
    return false;
  }

  @Override
  public void emitBlockQuads(
      BlockAndTintGetter blockView,
      BlockState state,
      BlockPos pos,
      Supplier<RandomSource> randomSupplier,
      RenderContext context) {
    context.pushTransform(EMISSIVE_TRANSFORM);
    super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
    context.popTransform();
  }

  @Override
  public void emitItemQuads(
      ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
    context.pushTransform(EMISSIVE_TRANSFORM);
    super.emitItemQuads(stack, randomSupplier, context);
    context.popTransform();
  }
}
