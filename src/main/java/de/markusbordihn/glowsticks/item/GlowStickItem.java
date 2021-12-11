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

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.config.CommonConfig;
import de.markusbordihn.glowsticks.entity.projectile.GlowStick;

@Mod.EventBusSubscriber
public class GlowStickItem extends Item {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  // Using CompoundTag for item status and animation steps
  public static final String TAG_ACTIVATED = "activated";
  public static final String TAG_STEP = "step";
  public static final int ANIMATION_STEPS = 6;
  public static final int DURATION_TICKS = ANIMATION_STEPS * 2;

  // Config
  public static final CommonConfig.Config COMMON = CommonConfig.COMMON;
  private static boolean glowStickDespawning = COMMON.glowStickDespawning.get();
  private static int glowStickDespawningTick = COMMON.glowStickDespawningTicks.get();

  private RegistryObject<Block> defaultBlock = null;

  @SubscribeEvent
  public static void onServerAboutToStartEvent(FMLServerAboutToStartEvent event) {
    glowStickDespawning = COMMON.glowStickDespawning.get();
    glowStickDespawningTick = COMMON.glowStickDespawningTicks.get();

    if (glowStickDespawning) {
      log.info("🧪 \u25BA Glow Sticks age will be decreased every {} random ticks ...",
          glowStickDespawningTick);
    } else {
      log.info("🧪 \u25A0 Glow Sticks will not despawn !");
    }
  }

  public GlowStickItem(Item.Properties properties) {
    super(properties);
  }

  public GlowStickItem(Item.Properties properties, RegistryObject<Block> block) {
    super(properties);
    defaultBlock = block;
  }

  public static boolean isActivated(ItemStack itemStack) {
    CompoundNBT compoundTag = itemStack.getTag();
    return compoundTag != null && compoundTag.getBoolean(TAG_ACTIVATED);
  }

  public static void setActivated(ItemStack itemStack, boolean activated) {
    CompoundNBT compoundTag = itemStack.getOrCreateTag();
    compoundTag.putBoolean(TAG_ACTIVATED, activated);
  }

  public static int getStep(ItemStack itemStack) {
    CompoundNBT compoundTag = itemStack.getTag();
    return compoundTag != null ? compoundTag.getInt(TAG_STEP) : 0;
  }

  public static int setStep(ItemStack itemStack, int step) {
    CompoundNBT compoundTag = itemStack.getOrCreateTag();
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
  public UseAction getUseAnimation(ItemStack itemStack) {
    return UseAction.NONE;
  }

  @Override
  public boolean shouldCauseReequipAnimation(ItemStack oldItemStack, ItemStack newItemStack,
      boolean slotChanged) {
    return slotChanged || (oldItemStack.getItem() != newItemStack.getItem());
  }

  @Override
  public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
    ItemStack itemStack = player.getItemInHand(hand);

    // Only throw item if it is already activated, otherwise start activation animation.
    if (isActivated(itemStack)) {
      if (!world.isClientSide) {
        player.getCooldowns().addCooldown(this, DURATION_TICKS);
        player.stopUsingItem();

        // Create new glowStick Entity with block reference and facing direction.
        GlowStick glowStick = new GlowStick(world, player, defaultBlock);
        glowStick.setItem(itemStack);
        glowStick.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 1.0F);
        world.addFreshEntity(glowStick);
      }

      player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));

      if (!player.abilities.instabuild) {
        itemStack.shrink(1);
      }
      setActivated(itemStack, false);
      setStep(itemStack, 0);
      return ActionResult.sidedSuccess(itemStack, world.isClientSide());
    } else {
      player.startUsingItem(hand);
      return ActionResult.consume(itemStack);
    }
  }

  @Override
  public void onUseTick(World world, LivingEntity livingEntity, ItemStack itemStack, int tick) {
    // Add animation steps to the use animation
    if (!world.isClientSide && glowStickDespawning && tick % glowStickDespawningTick == 0) {
      int currentStep = increaseStep(itemStack);
      // Play sound after breaking the glow stick
      if (currentStep == 4) {
        world.playSound((PlayerEntity) null, livingEntity.getX(), livingEntity.getY(),
            livingEntity.getZ(), SoundEvents.TRIDENT_HIT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
      }
      if (currentStep > ANIMATION_STEPS) {
        setStep(itemStack, 0);
      }
    }
  }

  @Override
  public void releaseUsing(ItemStack itemStack, World world, LivingEntity livingEntity,
      int duration) {
    // Reset steps if item is release before.
    if (!isActivated(itemStack)) {
      setStep(itemStack, 0);
    }
  }

  @Override
  public ItemStack finishUsingItem(ItemStack itemStack, World world, LivingEntity livingEntity) {
    // Active item after use animation is done.
    if (livingEntity instanceof PlayerEntity && !world.isClientSide && !isActivated(itemStack)) {
      setActivated(itemStack, true);
    }
    return itemStack;
  }

}
