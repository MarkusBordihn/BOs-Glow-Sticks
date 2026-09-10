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

package de.markusbordihn.glowsticks.block.glowstick;

import de.markusbordihn.glowsticks.block.CreativeGlowStickBlock;
import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class BlockStateManager {

  public static final int MAX_AGE = 15;
  public static final int MIN_VARIANT = 0;
  public static final int MAX_VARIANT = 5;

  public static BlockState createInitialBlockState(GlowStickBlock block) {
    return block
        .getStateDefinition()
        .any()
        .setValue(GlowStickBlock.AGE, 0)
        .setValue(GlowStickBlock.FACING, Direction.NORTH)
        .setValue(GlowStickBlock.WATERLOGGED, false)
        .setValue(GlowStickBlock.REDSTONE_CONTROL, RedstoneCapable.UNCONTROLLED);
  }

  public static int getAge(BlockState blockState) {
    return blockState.hasProperty(GlowStickBlock.AGE) ? blockState.getValue(GlowStickBlock.AGE) : 0;
  }

  public static int getVariant(BlockState blockState) {
    return blockState.getValue(CreativeGlowStickBlock.VARIANT);
  }

  public static BlockState setVariant(BlockState blockState, int variant) {
    return blockState.setValue(
        CreativeGlowStickBlock.VARIANT, Mth.clamp(variant, MIN_VARIANT, MAX_VARIANT));
  }

  public static int getNextVariant(BlockState blockState) {
    int currentVariant = getVariant(blockState);
    return currentVariant >= MAX_VARIANT ? MIN_VARIANT : currentVariant + 1;
  }

  public static BlockState getCreativeStateForPlacement(
      CreativeGlowStickBlock block, BlockPlaceContext context) {
    Direction clickedFace = context.getClickedFace();
    AttachFace attachFace;
    Direction horizontalFacing;
    if (clickedFace == Direction.UP) {
      attachFace = AttachFace.FLOOR;
      horizontalFacing = context.getHorizontalDirection().getOpposite();
    } else if (clickedFace == Direction.DOWN) {
      attachFace = AttachFace.CEILING;
      horizontalFacing = context.getHorizontalDirection().getOpposite();
    } else {
      attachFace = AttachFace.WALL;
      horizontalFacing = clickedFace.getOpposite();
    }

    return RedstoneCapable.getInitialPlacementState(
        block
            .defaultBlockState()
            .setValue(CreativeGlowStickBlock.FACE, attachFace)
            .setValue(GlowStickBlock.FACING, horizontalFacing)
            .setValue(GlowStickBlock.WATERLOGGED, isWaterAt(context, context.getClickedPos())),
        context.getLevel(),
        context.getClickedPos());
  }

  private static boolean isWaterAt(BlockPlaceContext context, BlockPos blockPos) {
    return context.getLevel().getFluidState(blockPos).getType() == Fluids.WATER;
  }

  public static BlockState getStateForPlacement(GlowStickBlock block, BlockPlaceContext context) {
    BlockPos blockPlacementPos = context.getClickedPos();
    if (!context.canPlace()) {
      blockPlacementPos = blockPlacementPos.relative(context.getClickedFace());
    }

    return RedstoneCapable.getInitialPlacementState(
        block
            .defaultBlockState()
            .setValue(GlowStickBlock.FACING, context.getHorizontalDirection().getOpposite())
            .setValue(GlowStickBlock.WATERLOGGED, isWaterAt(context, blockPlacementPos)),
        context.getLevel(),
        blockPlacementPos);
  }

  public static FluidState getFluidState(BlockState blockState) {
    return blockState.getValue(GlowStickBlock.WATERLOGGED)
        ? Fluids.WATER.getSource(false)
        : Fluids.EMPTY.defaultFluidState();
  }

  public static void handleDespawnTick(
      BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, GlowStickBlock block) {
    if (GlowSticksConfig.getGlowStickStageTicks() <= 0) {
      return;
    }

    block.scheduleDespawnTick(serverLevel, blockPos);

    int currentAge = blockState.getValue(GlowStickBlock.AGE);
    if (RedstoneCapable.isControlled(blockState)) {
      if (GlowSticksConfig.glowStickRedstoneRecharges && RedstoneCapable.isPowered(blockState)) {
        setAge(blockState, serverLevel, blockPos, currentAge - 1);
      } else if (!GlowSticksConfig.glowStickRedstoneFreezesLifetime && currentAge < MAX_AGE) {
        setAge(blockState, serverLevel, blockPos, currentAge + 1);
      }
      return;
    }

    if (currentAge >= MAX_AGE) {
      serverLevel.destroyBlock(blockPos, GlowSticksConfig.glowStickDropsOnDespawn);
      return;
    }

    setAge(blockState, serverLevel, blockPos, currentAge + 1);
  }

  private static void setAge(
      BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, int age) {
    int normalizedAge = Mth.clamp(age, 0, MAX_AGE);
    if (normalizedAge == blockState.getValue(GlowStickBlock.AGE)) {
      return;
    }

    serverLevel.setBlock(
        blockPos, blockState.setValue(GlowStickBlock.AGE, normalizedAge), Block.UPDATE_CLIENTS);
  }

  public static int calculateLightLevel(BlockState blockState) {
    if (RedstoneCapable.isControlled(blockState)) {
      return RedstoneCapable.getPower(blockState);
    }

    return Mth.clamp(MAX_AGE - blockState.getValue(GlowStickBlock.AGE), 1, MAX_AGE);
  }

  public static int calculateCreativeLightLevel(BlockState blockState) {
    if (RedstoneCapable.isControlled(blockState)) {
      return RedstoneCapable.getPower(blockState);
    }

    return MAX_AGE;
  }
}
