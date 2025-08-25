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

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.entity.projectile.GlowStickProjectile;
import de.markusbordihn.glowsticks.utils.ToolTips;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class GlowStickItem extends Item {

  public static final String NAME = "glow_stick";
  public static final String TAG_ACTIVATED = "activated";
  public static final String TAG_STEP = "step";
  public static final int ANIMATION_STEPS = 6;
  public static final int DURATION_TICKS = ANIMATION_STEPS * 2;
  public static final String TOOLTIP_PREFIX = Constants.TEXT_PREFIX + NAME + "_";

  protected final Supplier<Block> blockSupplier;
  protected final int despawnTickRate;
  private final DyeColor dyeColor;

  public GlowStickItem(Properties properties, Supplier<Block> blockSupplier, DyeColor dyeColor) {
    this(properties, blockSupplier, dyeColor, GlowSticksConfig.despawnTicks);
  }

  public GlowStickItem(
      Properties properties,
      Supplier<Block> blockSupplier,
      DyeColor dyeColor,
      int despawnTickRate) {
    super(properties);
    this.blockSupplier = blockSupplier;
    this.despawnTickRate = despawnTickRate;
    this.dyeColor = dyeColor;
  }

  public static boolean isActivated(final ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.getBoolean(TAG_ACTIVATED);
  }

  public static void setActivated(final ItemStack stack, final boolean activated) {
    stack.getOrCreateTag().putBoolean(TAG_ACTIVATED, activated);
  }

  public static int getStep(final ItemStack itemStack) {
    CompoundTag tag = itemStack.getTag();
    return tag != null ? tag.getInt(TAG_STEP) : 0;
  }

  public static int setStep(final ItemStack itemStack, final int step) {
    itemStack.getOrCreateTag().putInt(TAG_STEP, step);
    return step;
  }

  public static int increaseStep(final ItemStack itemStack) {
    return setStep(itemStack, getStep(itemStack) + 1);
  }

  public GlowStickProjectile getGlowStickEntity(final Level level, final LivingEntity entity) {
    throw new UnsupportedOperationException("GlowStickItem.getGlowStickEntity() not implemented!");
  }

  public DyeColor getDyeColor() {
    return dyeColor;
  }

  @Override
  public int getUseDuration(ItemStack itemStack) {
    return DURATION_TICKS;
  }

  @Override
  public UseAnim getUseAnimation(ItemStack itemStack) {
    return UseAnim.NONE;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack itemStack = player.getItemInHand(hand);
    if (isActivated(itemStack)) {
      if (!level.isClientSide) {
        GlowStickProjectile entity = getGlowStickEntity(level, player);
        entity.setItem(itemStack);
        entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(entity);
        player.getCooldowns().addCooldown(this, DURATION_TICKS);
        itemStack.shrink(1);
      }
      setActivated(itemStack, false);
      setStep(itemStack, 0);
      player.awardStat(Stats.ITEM_USED.get(this));
      return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    } else {
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(itemStack);
    }
  }

  @Override
  public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int count) {
    if (!level.isClientSide && count % 2 == 0) {
      int step = increaseStep(itemStack);
      if (step == 4) {
        level.playSound(
            null,
            livingEntity.getX(),
            livingEntity.getY(),
            livingEntity.getZ(),
            SoundEvents.TRIDENT_HIT,
            SoundSource.NEUTRAL,
            1.0F,
            1.0F);
      }
      if (step > ANIMATION_STEPS) {
        setStep(itemStack, 0);
      }
    }
  }

  @Override
  public void releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int timeLeft) {
    if (!isActivated(itemStack)) {
      setStep(itemStack, 0);
    }
  }

  @Override
  public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
    if (!level.isClientSide && !isActivated(itemStack)) {
      setActivated(itemStack, true);
    }
    return itemStack;
  }

  @Override
  public void appendHoverText(
      ItemStack itemStack, Level level, List<Component> tooltipList, TooltipFlag tooltipFlag) {
    ToolTips.addTooltip(
        tooltipList,
        new TranslatableComponent(TOOLTIP_PREFIX + dyeColor + "_description")
            .withStyle(ChatFormatting.GRAY));
    ToolTips.addTooltip(
        tooltipList,
        new TranslatableComponent(TOOLTIP_PREFIX + "usage").withStyle(ChatFormatting.YELLOW));
    ToolTips.addTooltip(
        tooltipList,
        new TranslatableComponent(TOOLTIP_PREFIX + "use", despawnTickRate)
            .withStyle(ChatFormatting.GREEN));
  }
}
