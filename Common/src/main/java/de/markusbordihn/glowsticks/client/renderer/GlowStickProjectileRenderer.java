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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.markusbordihn.glowsticks.entity.projectile.GlowStickProjectile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;

public class GlowStickProjectileRenderer
  extends EntityRenderer<GlowStickProjectile, ThrownItemRenderState> {

  private static final float SPIN_DEGREES_PER_TICK = 20.0F;
  private static final int SELF_LIT_BLOCK_LIGHT = 15;

  private final ItemModelResolver itemModelResolver;

  public GlowStickProjectileRenderer(EntityRendererProvider.Context context) {
    super(context);
    this.itemModelResolver = context.getItemModelResolver();
  }

  @Override
  protected int getBlockLightLevel(GlowStickProjectile entity, BlockPos blockPos) {
    return SELF_LIT_BLOCK_LIGHT;
  }

  @Override
  public ThrownItemRenderState createRenderState() {
    return new ThrownItemRenderState();
  }

  @Override
  public void extractRenderState(
    GlowStickProjectile entity, ThrownItemRenderState renderState, float partialTick) {
    super.extractRenderState(entity, renderState, partialTick);
    this.itemModelResolver.updateForNonLiving(
      renderState.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
  }

  @Override
  public void submit(
    ThrownItemRenderState renderState,
    PoseStack poseStack,
    SubmitNodeCollector submitNodeCollector,
    CameraRenderState cameraRenderState) {
    poseStack.pushPose();
    poseStack.mulPose(cameraRenderState.orientation);
    poseStack.mulPose(Axis.ZP.rotationDegrees(renderState.ageInTicks * SPIN_DEGREES_PER_TICK));
    renderState.item.submit(
      poseStack,
      submitNodeCollector,
      renderState.lightCoords,
      OverlayTexture.NO_OVERLAY,
      renderState.outlineColor);
    poseStack.popPose();
    super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);
  }
}
