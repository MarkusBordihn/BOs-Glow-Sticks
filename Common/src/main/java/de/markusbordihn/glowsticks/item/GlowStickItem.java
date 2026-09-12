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
import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.block.glowstick.BlockStateManager;
import de.markusbordihn.glowsticks.block.glowstick.PlacementSounds;
import de.markusbordihn.glowsticks.block.glowstick.RedstoneCapable;
import de.markusbordihn.glowsticks.component.DataComponents;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.data.GlowStickData;
import de.markusbordihn.glowsticks.entity.projectile.GlowStickProjectile;
import de.markusbordihn.glowsticks.utils.ToolTips;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GlowStickItem extends Item {

  public static final String NAME = "glow_stick";
  public static final int ANIMATION_STEPS = 6;
  public static final int DURATION_TICKS = ANIMATION_STEPS * 2;
  public static final String TOOLTIP_PREFIX = Constants.TEXT_PREFIX + NAME;

  private static final int PLACEMENT_COOLDOWN_TICKS = 4;

  protected final Supplier<Block> blockSupplier;
  private final DyeColor dyeColor;

  public GlowStickItem(Properties properties, Supplier<Block> blockSupplier, DyeColor dyeColor) {
    super(properties);
    this.blockSupplier = blockSupplier;
    this.dyeColor = dyeColor;
  }

  private static GlowStickData getData(final ItemStack itemStack) {
    return itemStack.getOrDefault(DataComponents.GLOW_STICK_DATA, GlowStickData.EMPTY);
  }

  public static boolean isActivated(final ItemStack stack) {
    return getData(stack).activated();
  }

  public static void setActivated(final ItemStack stack, final boolean activated) {
    stack.set(DataComponents.GLOW_STICK_DATA, getData(stack).withActivated(activated));
  }

  public static int getStep(final ItemStack itemStack) {
    return getData(itemStack).step();
  }

  public static int setStep(final ItemStack itemStack, final int step) {
    itemStack.set(DataComponents.GLOW_STICK_DATA, getData(itemStack).withStep(step));
    return step;
  }

  public static int increaseStep(final ItemStack itemStack) {
    GlowStickData increasedData = getData(itemStack).withIncrementedStep();
    itemStack.set(DataComponents.GLOW_STICK_DATA, increasedData);
    return increasedData.step();
  }

  public static int getAge(final ItemStack itemStack) {
    return Mth.clamp(getData(itemStack).age(), 0, BlockStateManager.MAX_AGE);
  }

  public static void setAge(final ItemStack itemStack, final int age) {
    if (age > 0) {
      itemStack.set(
        DataComponents.GLOW_STICK_DATA,
        getData(itemStack).withAge(Mth.clamp(age, 0, BlockStateManager.MAX_AGE)));
    }
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
    return this.dyeColor;
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
  public InteractionResult useOn(UseOnContext context) {
    ItemStack itemStack = context.getItemInHand();
    Player player = context.getPlayer();
    if (player == null || isActivated(itemStack)) {
      return InteractionResult.PASS;
    }

    Level level = context.getLevel();
    BlockPos clickedPos = context.getClickedPos();
    BlockState clickedState = level.getBlockState(clickedPos);
    if (player.isShiftKeyDown() && clickedState.getBlock() instanceof GlowStickBlock) {
      return GlowSticksConfig.allowGlowStickPickup
        ? this.pickUpGlowStick(level, clickedPos, clickedState, player)
        : InteractionResult.PASS;
    }

    return GlowSticksConfig.allowGlowStickBlockPlacement
      ? this.placeGlowStick(context, level, player, itemStack)
      : InteractionResult.PASS;
  }

  private InteractionResult pickUpGlowStick(
    Level level, BlockPos blockPos, BlockState blockState, Player player) {
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }

    ItemStack pickedUpStack = new ItemStack(blockState.getBlock().asItem());
    setAge(pickedUpStack, blockState.getValue(GlowStickBlock.AGE));
    level.removeBlock(blockPos, false);

    if (!player.getInventory().add(pickedUpStack)) {
      player.drop(pickedUpStack, false);
    }
    level.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.0F);

    return InteractionResult.CONSUME;
  }

  private InteractionResult placeGlowStick(
    UseOnContext context, Level level, Player player, ItemStack itemStack) {
    Block glowStickBlock = this.blockSupplier.get();
    if (glowStickBlock == null) {
      return InteractionResult.PASS;
    }

    BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
    if (!blockPlaceContext.canPlace()) {
      return InteractionResult.PASS;
    }

    BlockPos placementPos = blockPlaceContext.getClickedPos();
    BlockState placementState = glowStickBlock.getStateForPlacement(blockPlaceContext);
    if (placementState == null || !placementState.canSurvive(level, placementPos)) {
      return InteractionResult.PASS;
    }

    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }

    level.setBlockAndUpdate(
      placementPos, placementState.setValue(GlowStickBlock.AGE, getAge(itemStack)));
    PlacementSounds.playPlacementSound(
      level, placementPos, level.getBlockState(placementPos.below()), level.random);
    RedstoneCapable.handleBlockPlacement(level, placementPos);
    if (player instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, placementPos, itemStack);
    }

    player.getCooldowns().addCooldown(itemStack, PLACEMENT_COOLDOWN_TICKS);
    if (!player.getAbilities().instabuild) {
      itemStack.shrink(1);
    }

    return InteractionResult.CONSUME;
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    ItemStack itemStack = player.getItemInHand(hand);
    if (!isActivated(itemStack)) {
      player.startUsingItem(hand);
      return InteractionResult.CONSUME;
    }

    if (!level.isClientSide()) {
      GlowStickProjectile entity = this.getGlowStickEntity(level, player);
      entity.setItem(itemStack);
      entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
      level.addFreshEntity(entity);
      level.playSound(
        null,
        player.getX(),
        player.getY(),
        player.getZ(),
        SoundEvents.SNOWBALL_THROW,
        SoundSource.NEUTRAL,
        0.5F,
        0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
      player.getCooldowns().addCooldown(itemStack, DURATION_TICKS);
      if (!player.getAbilities().instabuild) {
        itemStack.shrink(1);
      }
    }

    setActivated(itemStack, false);
    setStep(itemStack, 0);
    player.awardStat(Stats.ITEM_USED.get(this));

    return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
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
          SoundEvents.FLINTANDSTEEL_USE,
          SoundSource.NEUTRAL,
          0.8F,
          0.5F + level.random.nextFloat() * 0.2F);
      }
      if (step > ANIMATION_STEPS) {
        setStep(itemStack, 0);
      }
    }
  }

  @Override
  public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int timeLeft) {
    if (!isActivated(itemStack)) {
      setStep(itemStack, 0);
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
      Component.translatable(TOOLTIP_PREFIX + "_" + this.dyeColor + ".description")
        .withStyle(ChatFormatting.GRAY));
    ToolTips.addTooltip(
      tooltipConsumer,
      Component.translatable(TOOLTIP_PREFIX + ".usage").withStyle(ChatFormatting.YELLOW));
    ToolTips.addTooltip(
      tooltipConsumer,
      GlowSticksConfig.glowStickLifetimeSeconds <= 0
        ? Component.translatable(TOOLTIP_PREFIX + ".use_infinite")
          .withStyle(ChatFormatting.DARK_GREEN)
        : Component.translatable(
            TOOLTIP_PREFIX + ".use", GlowSticksConfig.glowStickLifetimeSeconds / 60)
          .withStyle(ChatFormatting.GREEN));

    if (GlowSticksConfig.allowGlowStickBlockPlacement) {
      ToolTips.addTooltip(
        tooltipConsumer,
        Component.translatable(TOOLTIP_PREFIX + ".place").withStyle(ChatFormatting.YELLOW));
    }

    if (GlowSticksConfig.allowGlowStickPickup) {
      ToolTips.addTooltip(
        tooltipConsumer,
        Component.translatable(TOOLTIP_PREFIX + ".pickup").withStyle(ChatFormatting.YELLOW));
    }

    int age = getAge(itemStack);
    if (age > 0) {
      ToolTips.addTooltip(
        tooltipConsumer,
        Component.translatable(
            TOOLTIP_PREFIX + ".remaining",
            Math.round((GlowSticksConfig.AGE_STEPS - age) * 100.0f / GlowSticksConfig.AGE_STEPS))
          .withStyle(ChatFormatting.GOLD));
    }
  }
}
