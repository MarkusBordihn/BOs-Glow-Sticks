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
import de.markusbordihn.glowsticks.block.CreativeGlowStickBlock;
import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.block.glowstick.BlockStateManager;
import de.markusbordihn.glowsticks.block.glowstick.RedstoneCapable;
import de.markusbordihn.glowsticks.utils.ToolTips;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeGlowStickItem extends Item {

  public static final String NAME = "creative_glow_stick";
  public static final String TOOLTIP_PREFIX = Constants.TEXT_PREFIX + NAME;

  private final Supplier<Block> blockSupplier;
  private final DyeColor dyeColor;

  public CreativeGlowStickItem(
      Properties properties, Supplier<Block> blockSupplier, DyeColor dyeColor) {
    super(properties);
    this.blockSupplier = blockSupplier;
    this.dyeColor = dyeColor;
  }

  public DyeColor getDyeColor() {
    return this.dyeColor;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    Player player = context.getPlayer();
    if (level.isClientSide || player == null) {
      return InteractionResult.SUCCESS;
    }

    BlockPos clickedPos = context.getClickedPos();
    BlockState clickedState = level.getBlockState(clickedPos);

    ItemStack itemStack = context.getItemInHand();
    if (clickedState.getBlock() instanceof GlowStickBlock) {
      return this.replaceGlowStickBlock(level, clickedPos, clickedState, player, itemStack);
    } else if (clickedState.getBlock() instanceof CreativeGlowStickBlock existingCreative) {
      return this.handleCreativeGlowStickReplacement(
          level, clickedPos, clickedState, existingCreative, player, itemStack);
    }

    BlockPos placementPos = context.getClickedPos().relative(context.getClickedFace());
    BlockState targetState = level.getBlockState(placementPos);
    if (targetState.getBlock() instanceof GlowStickBlock) {
      return this.replaceGlowStickBlock(level, placementPos, targetState, player, itemStack);
    }

    if (targetState.getBlock() instanceof CreativeGlowStickBlock existingCreative) {
      return this.handleCreativeGlowStickReplacement(
          level, placementPos, targetState, existingCreative, player, itemStack);
    }

    if (!targetState.isAir() && !targetState.canBeReplaced()) {
      return InteractionResult.FAIL;
    }

    return this.attemptBlockPlacement(level, placementPos, context, player, itemStack);
  }

  private InteractionResult attemptBlockPlacement(
      Level level,
      BlockPos placementPos,
      UseOnContext context,
      Player player,
      ItemStack itemStack) {
    Block glowStickBlock = this.blockSupplier.get();
    if (glowStickBlock == null) {
      return InteractionResult.FAIL;
    }

    BlockState proposedState = glowStickBlock.getStateForPlacement(new BlockPlaceContext(context));
    if (proposedState == null || !proposedState.canSurvive(level, placementPos)) {
      return InteractionResult.FAIL;
    }

    this.placeBlockAndHandleRedstone(level, placementPos, proposedState);
    this.consumeItemInSurvival(player, itemStack);
    return InteractionResult.CONSUME;
  }

  private void placeBlockAndHandleRedstone(Level level, BlockPos position, BlockState blockState) {
    level.setBlockAndUpdate(position, blockState);
    RedstoneCapable.handleBlockPlacement(level, position);
  }

  private InteractionResult handleCreativeGlowStickReplacement(
      Level level,
      BlockPos placementPos,
      BlockState existingState,
      CreativeGlowStickBlock existingBlock,
      Player player,
      ItemStack itemStack) {
    Block newBlock = this.blockSupplier.get();
    if (!(newBlock instanceof CreativeGlowStickBlock newCreative)) {
      return InteractionResult.FAIL;
    }

    if (newCreative.getGlowStickColor() == existingBlock.getGlowStickColor()) {
      this.replaceWithNextVariant(level, placementPos, existingState);
      this.consumeItemInSurvival(player, itemStack);
      return InteractionResult.CONSUME;
    }

    return this.replaceGlowStickBlock(level, placementPos, existingState, player, itemStack);
  }

  private InteractionResult replaceGlowStickBlock(
      Level level,
      BlockPos placementPos,
      BlockState existingState,
      Player player,
      ItemStack itemStack) {
    Block blockToPlace = this.blockSupplier.get();
    if (blockToPlace == null) {
      return InteractionResult.FAIL;
    }

    BlockState proposedState =
        blockToPlace
            .defaultBlockState()
            .setValue(GlowStickBlock.FACING, existingState.getValue(GlowStickBlock.FACING));

    if (existingState.hasProperty(CreativeGlowStickBlock.VARIANT)
        && proposedState.hasProperty(CreativeGlowStickBlock.VARIANT)) {
      proposedState =
          BlockStateManager.setVariant(proposedState, BlockStateManager.getVariant(existingState));
    }

    if (existingState.hasProperty(CreativeGlowStickBlock.FACE)
        && proposedState.hasProperty(CreativeGlowStickBlock.FACE)) {
      proposedState =
          proposedState.setValue(
              CreativeGlowStickBlock.FACE, existingState.getValue(CreativeGlowStickBlock.FACE));
    }

    proposedState = RedstoneCapable.getInitialPlacementState(proposedState, level, placementPos);
    if (!proposedState.canSurvive(level, placementPos)) {
      return InteractionResult.FAIL;
    }

    Block.dropResources(existingState, level, placementPos);
    this.placeBlockAndHandleRedstone(level, placementPos, proposedState);
    this.consumeItemInSurvival(player, itemStack);
    return InteractionResult.CONSUME;
  }

  private void replaceWithNextVariant(Level level, BlockPos position, BlockState existingState) {
    level.setBlockAndUpdate(
        position,
        BlockStateManager.setVariant(
            existingState, BlockStateManager.getNextVariant(existingState)));
  }

  private void consumeItemInSurvival(Player player, ItemStack itemStack) {
    if (!player.getAbilities().instabuild) {
      itemStack.shrink(1);
    }
  }

  @Override
  public boolean isFoil(ItemStack itemStack) {
    return true;
  }

  @Override
  public void appendHoverText(
      ItemStack itemStack,
      Item.TooltipContext tooltipContext,
      List<Component> tooltipList,
      TooltipFlag tooltipFlag) {
    ToolTips.addTooltip(
        tooltipList,
        Component.translatable(GlowStickItem.TOOLTIP_PREFIX + "_" + this.dyeColor + ".description")
            .withStyle(ChatFormatting.GRAY));
    ToolTips.addTooltip(
        tooltipList,
        Component.translatable(TOOLTIP_PREFIX + ".usage").withStyle(ChatFormatting.YELLOW));
    ToolTips.addTooltip(
        tooltipList,
        Component.translatable(TOOLTIP_PREFIX + ".use").withStyle(ChatFormatting.GREEN));
    ToolTips.addTooltip(
        tooltipList,
        Component.translatable(TOOLTIP_PREFIX + ".permanent").withStyle(ChatFormatting.GOLD));
  }
}
