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
import de.markusbordihn.glowsticks.block.glowstick.RedstoneCapable;
import de.markusbordihn.glowsticks.utils.ToolTips;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.level.block.state.properties.AttachFace;

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
    return dyeColor;
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
    if (clickedState.getBlock() instanceof GlowStickBlock existingGlowStick) {
      return handleGlowStickReplacement(
          level, clickedPos, clickedState, existingGlowStick, player, itemStack);
    } else if (clickedState.getBlock() instanceof CreativeGlowStickBlock existingCreative) {
      return handleCreativeGlowStickReplacement(
          level, clickedPos, clickedState, existingCreative, player, itemStack);
    }

    BlockPos placementPos = context.getClickedPos().relative(context.getClickedFace());
    BlockState targetState = level.getBlockState(placementPos);
    if (targetState.getBlock() instanceof GlowStickBlock existingGlowStick) {
      return handleGlowStickReplacement(
          level, placementPos, targetState, existingGlowStick, player, itemStack);
    }
    if (targetState.getBlock() instanceof CreativeGlowStickBlock existingCreative) {
      return handleCreativeGlowStickReplacement(
          level, placementPos, targetState, existingCreative, player, itemStack);
    }

    if (!targetState.isAir() && !targetState.canBeReplaced()) {
      return InteractionResult.FAIL;
    }

    return attemptBlockPlacement(level, placementPos, context, player, itemStack);
  }

  private InteractionResult attemptBlockPlacement(
      Level level,
      BlockPos placementPos,
      UseOnContext context,
      Player player,
      ItemStack itemStack) {
    Block glowStickBlock = blockSupplier.get();
    if (glowStickBlock == null) {
      return InteractionResult.FAIL;
    }

    BlockState proposedState = glowStickBlock.getStateForPlacement(new BlockPlaceContext(context));
    if (proposedState == null || !canPlaceBlockAt(level, placementPos, proposedState)) {
      spawnGlowStickItem(level, placementPos, glowStickBlock);
    } else {
      placeBlockAndHandleRedstone(level, placementPos, proposedState);
    }

    consumeItemInSurvival(player, itemStack);
    return InteractionResult.CONSUME;
  }

  private boolean canPlaceBlockAt(Level level, BlockPos position, BlockState blockState) {
    return blockState.canSurvive(level, position);
  }

  private void placeBlockAndHandleRedstone(Level level, BlockPos position, BlockState blockState) {
    level.setBlockAndUpdate(position, blockState);
    RedstoneCapable.handleBlockPlacement(level, position, blockState);
  }

  private InteractionResult handleCreativeGlowStickReplacement(
      Level level,
      BlockPos placementPos,
      BlockState existingState,
      CreativeGlowStickBlock existingBlock,
      Player player,
      ItemStack itemStack) {
    Block newBlock = blockSupplier.get();
    if (!(newBlock instanceof CreativeGlowStickBlock newCreative)) {
      return InteractionResult.FAIL;
    }

    if (newCreative.getGlowStickColor() == existingBlock.getGlowStickColor()) {
      replaceWithNextVariant(level, placementPos, existingState);
      consumeItemInSurvival(player, itemStack);
      return InteractionResult.CONSUME;
    } else {
      int preservedVariant = existingState.getValue(GlowStickBlock.VARIANT);
      Direction preservedFacing = existingState.getValue(GlowStickBlock.FACING);
      AttachFace preservedFace = existingState.getValue(CreativeGlowStickBlock.FACE);
      level.removeBlock(placementPos, false);
      return placeBlockWithVariantAndFacing(
          level,
          placementPos,
          newBlock,
          preservedVariant,
          preservedFacing,
          preservedFace,
          player,
          itemStack);
    }
  }

  private InteractionResult handleGlowStickReplacement(
      Level level,
      BlockPos placementPos,
      BlockState existingState,
      GlowStickBlock existingGlowStick,
      Player player,
      ItemStack itemStack) {
    Block newGlowStickBlock = blockSupplier.get();
    if (!(newGlowStickBlock instanceof GlowStickBlock newGlowStick)) {
      return InteractionResult.FAIL;
    }

    if (newGlowStick.getGlowStickColor() == existingGlowStick.getGlowStickColor()) {
      replaceWithNextVariant(level, placementPos, existingState);
      consumeItemInSurvival(player, itemStack);
      return InteractionResult.CONSUME;
    } else {
      int preservedVariant = existingState.getValue(GlowStickBlock.VARIANT);
      Direction preservedFacing = existingState.getValue(GlowStickBlock.FACING);
      AttachFace preservedFace = null;
      level.removeBlock(placementPos, false);
      return placeBlockWithVariantAndFacing(
          level,
          placementPos,
          newGlowStickBlock,
          preservedVariant,
          preservedFacing,
          preservedFace,
          player,
          itemStack);
    }
  }

  private InteractionResult placeBlockWithVariantAndFacing(
      Level level,
      BlockPos placementPos,
      Block blockToPlace,
      int variant,
      Direction facing,
      AttachFace face,
      Player player,
      ItemStack itemStack) {
    BlockState proposedState =
        blockToPlace
            .defaultBlockState()
            .setValue(GlowStickBlock.VARIANT, variant)
            .setValue(GlowStickBlock.FACING, facing);

    if (face != null && blockToPlace instanceof CreativeGlowStickBlock) {
      proposedState = proposedState.setValue(CreativeGlowStickBlock.FACE, face);
    }

    proposedState = RedstoneCapable.getInitialPlacementState(proposedState, level, placementPos);
    if (canPlaceBlockAt(level, placementPos, proposedState)) {
      placeBlockAndHandleRedstone(level, placementPos, proposedState);
    } else {
      spawnGlowStickItem(level, placementPos, blockToPlace);
    }

    consumeItemInSurvival(player, itemStack);
    return InteractionResult.CONSUME;
  }

  private void replaceWithNextVariant(Level level, BlockPos position, BlockState existingState) {
    int currentVariant = existingState.getValue(GlowStickBlock.VARIANT);
    level.setBlockAndUpdate(
        position,
        existingState.setValue(
            GlowStickBlock.VARIANT, currentVariant >= 5 ? 0 : currentVariant + 1));
  }

  private void spawnGlowStickItem(Level level, BlockPos position, Block glowStickBlock) {
    ItemEntity itemEntity =
        new ItemEntity(
            level,
            position.getX() + 0.5,
            position.getY() + 0.5,
            position.getZ() + 0.5,
            new ItemStack(glowStickBlock.asItem()));

    itemEntity.setDeltaMovement(
        (level.random.nextFloat() - 0.5F) * 0.1F, 0.2F, (level.random.nextFloat() - 0.5F) * 0.1F);

    level.addFreshEntity(itemEntity);
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
