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
import de.markusbordihn.glowsticks.component.DataComponents;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.data.GlowStickData;
import de.markusbordihn.glowsticks.entity.projectile.GlowStickProjectile;
import de.markusbordihn.glowsticks.utils.ToolTips;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class GlowStickItem extends Item {

  public static final String NAME = "glow_stick";
  public static final int ANIMATION_STEPS = 6;
  public static final int DURATION_TICKS = ANIMATION_STEPS * 2;
  public static final String TOOLTIP_PREFIX = Constants.TEXT_PREFIX + NAME;

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
    GlowStickData glowStickData =
        stack.getOrDefault(DataComponents.GLOW_STICK_DATA, GlowStickData.EMPTY);
    return glowStickData.activated();
  }

  public static void setActivated(final ItemStack stack, final boolean activated) {
    GlowStickData currentData =
        stack.getOrDefault(DataComponents.GLOW_STICK_DATA, GlowStickData.EMPTY);
    stack.set(DataComponents.GLOW_STICK_DATA, currentData.withActivated(activated));
  }

  public static int getStep(final ItemStack itemStack) {
    GlowStickData glowStickData =
        itemStack.getOrDefault(DataComponents.GLOW_STICK_DATA, GlowStickData.EMPTY);
    return glowStickData.step();
  }

  public static int setStep(final ItemStack itemStack, final int step) {
    GlowStickData currentData =
        itemStack.getOrDefault(DataComponents.GLOW_STICK_DATA, GlowStickData.EMPTY);
    itemStack.set(DataComponents.GLOW_STICK_DATA, currentData.withStep(step));
    return step;
  }

  public static int increaseStep(final ItemStack itemStack) {
    GlowStickData currentData =
        itemStack.getOrDefault(DataComponents.GLOW_STICK_DATA, GlowStickData.EMPTY);
    GlowStickData newData = currentData.withIncrementedStep();
    itemStack.set(DataComponents.GLOW_STICK_DATA, newData);
    return newData.step();
  }

  public static float getStepFromDataComponent(
      final ItemStack itemStack, final Level level, final LivingEntity livingEntity, final int id) {
    if (itemStack.getItem() instanceof GlowStickItem && livingEntity != null) {
      return getStep(itemStack);
    }
    return 0.0F;
  }

  public static float getActivatedFromDataComponent(
      final ItemStack itemStack, final Level level, final LivingEntity livingEntity, final int id) {
    if (itemStack.getItem() instanceof GlowStickItem && livingEntity != null) {
      return isActivated(itemStack) ? 1.0F : 0.0F;
    }
    return 0.0F;
  }

  public GlowStickProjectile getGlowStickEntity(final Level level, final LivingEntity entity) {
    throw new UnsupportedOperationException("GlowStickItem.getGlowStickEntity() not implemented!");
  }

  public DyeColor getDyeColor() {
    return dyeColor;
  }

  @Override
  public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
    return DURATION_TICKS;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
    return ItemUseAnimation.NONE;
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    ItemStack itemStack = player.getItemInHand(hand);
    if (isActivated(itemStack)) {
      if (!level.isClientSide()) {
        GlowStickProjectile entity = getGlowStickEntity(level, player);
        entity.setItem(itemStack);
        entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(entity);
        player.getCooldowns().addCooldown(itemStack, DURATION_TICKS);
        if (!player.getAbilities().instabuild) {
          itemStack.shrink(1);
        }
      }
      setActivated(itemStack, false);
      setStep(itemStack, 0);
      player.awardStat(Stats.ITEM_USED.get(this));
      return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    } else {
      player.startUsingItem(hand);
      return InteractionResult.CONSUME;
    }
  }

  @Override
  public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int count) {
    if (!level.isClientSide() && count % 2 == 0) {
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
  public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int timeLeft) {
    if (!isActivated(itemStack)) {
      return true;
    }
    return false;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
    if (!level.isClientSide() && !isActivated(itemStack)) {
      setActivated(itemStack, true);
    }
    return itemStack;
  }

  @Override
  public void appendHoverText(
      ItemStack itemStack,
      TooltipContext tooltipContext,
      TooltipDisplay tooltipDisplay,
      Consumer<Component> tooltipConsumer,
      TooltipFlag tooltipFlag) {
    ToolTips.addTooltip(
        tooltipConsumer,
        Component.translatable(TOOLTIP_PREFIX + "_" + dyeColor + ".description")
            .withStyle(ChatFormatting.GRAY));
    ToolTips.addTooltip(
        tooltipConsumer,
        Component.translatable(TOOLTIP_PREFIX + ".usage").withStyle(ChatFormatting.YELLOW));
    ToolTips.addTooltip(
        tooltipConsumer,
        Component.translatable(TOOLTIP_PREFIX + ".use", despawnTickRate)
            .withStyle(ChatFormatting.GREEN));
  }
}
