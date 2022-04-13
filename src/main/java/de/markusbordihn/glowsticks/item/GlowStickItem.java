/**
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import net.minecraftforge.fmllegacy.RegistryObject;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.entity.projectile.GlowStick;

public class GlowStickItem extends Item {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  // Using CompoundTag for item status and animation steps
  public static final String TAG_ACTIVATED = "activated";
  public static final String TAG_STEP = "step";
  public static final int ANIMATION_STEPS = 6;
  public static final int DURATION_TICKS = ANIMATION_STEPS * 2;

  private RegistryObject<Block> defaultBlock = null;

  public GlowStickItem(Item.Properties properties) {
    super(properties);
  }

  public GlowStickItem(Item.Properties properties, RegistryObject<Block> block) {
    super(properties);
    defaultBlock = block;
  }

  public static boolean isActivated(ItemStack itemStack) {
    CompoundTag compoundTag = itemStack.getTag();
    return compoundTag != null && compoundTag.getBoolean(TAG_ACTIVATED);
  }

  public static void setActivated(ItemStack itemStack, boolean activated) {
    CompoundTag compoundTag = itemStack.getOrCreateTag();
    compoundTag.putBoolean(TAG_ACTIVATED, activated);
  }

  public static int getStep(ItemStack itemStack) {
    CompoundTag compoundTag = itemStack.getTag();
    return compoundTag != null ? compoundTag.getInt(TAG_STEP) : 0;
  }

  public static int setStep(ItemStack itemStack, int step) {
    CompoundTag compoundTag = itemStack.getOrCreateTag();
    if (compoundTag.getInt(TAG_STEP) != step) {
      compoundTag.putInt(TAG_STEP, step);
    }
    return step;
  }

  public static int increaseStep(ItemStack itemStack) {
    return setStep(itemStack, getStep(itemStack) + 1);
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
  public boolean shouldCauseReequipAnimation(ItemStack oldItemStack, ItemStack newItemStack,
      boolean slotChanged) {
    return slotChanged || (oldItemStack.getItem() != newItemStack.getItem());
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack itemStack = player.getItemInHand(hand);

    // Only throw item if it is already activated, otherwise start activation animation.
    if (isActivated(itemStack)) {
      if (!level.isClientSide) {
        player.getCooldowns().addCooldown(this, DURATION_TICKS);
        player.stopUsingItem();

        // Create new glowStick Entity with block reference and facing direction.
        GlowStick glowStick = new GlowStick(level, player, defaultBlock);
        glowStick.setItem(itemStack);
        glowStick.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(glowStick);
      }

      player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));

      if (!player.getAbilities().instabuild) {
        itemStack.shrink(1);
      }
      setActivated(itemStack, false);
      setStep(itemStack, 0);
      return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    } else {
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(itemStack);
    }
  }

  @Override
  public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int tick) {
    // Add animation steps to the use animation
    if (!level.isClientSide && tick % 2 == 0) {
      int currentStep = increaseStep(itemStack);
      // Play sound after breaking the glow stick
      if (currentStep == 4) {
        level.playSound((Player) null, livingEntity.getX(), livingEntity.getY(),
            livingEntity.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.NEUTRAL, 1.0F, 1.0F);
      }
      if (currentStep > ANIMATION_STEPS) {
        setStep(itemStack, 0);
      }
    }
  }

  @Override
  public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity,
      int duration) {
    // Reset steps if item is release before.
    if (!isActivated(itemStack)) {
      setStep(itemStack, 0);
    }
  }

  @Override
  public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
    // Active item after use animation is done.
    if (livingEntity instanceof Player && !level.isClientSide && !isActivated(itemStack)) {
      setActivated(itemStack, true);
    }
    return itemStack;
  }

}
