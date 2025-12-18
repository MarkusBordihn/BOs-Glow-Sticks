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

package de.markusbordihn.glowsticks.item;

import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.entity.projectile.GlowStickProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GlowStickItemWrapper extends GlowStickItem {

  public GlowStickItemWrapper(Properties properties, DyeColor dyeColor) {
    super(properties, () -> ModBlocks.getGlowStickBlock(dyeColor).get(), dyeColor);
  }

  @Override
  public GlowStickProjectile getGlowStickEntity(Level level, LivingEntity entity) {
    return new GlowStickProjectile(
      ModEntity.getGlowStickEntity(getDyeColor()).get(),
      level,
      entity,
      getDyeColor(),
      ModBlocks.getGlowStickBlock(getDyeColor()),
      ModItems.getGlowStickItem(getDyeColor()),
      ModBlocks.GLOW_STICK_LIGHT,
      ModBlocks.GLOW_STICK_LIGHT_WATER);
  }

  @Override
  public boolean shouldCauseReequipAnimation(
    ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    return slotChanged || oldStack.getItem() != newStack.getItem();
  }
}
