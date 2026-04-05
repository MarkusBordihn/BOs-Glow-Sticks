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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class BlockStateManager {

  public static BlockState createInitialBlockState(GlowStickBlock block) {
    return block.getStateDefinition().any().setValue(GlowStickBlock.AGE, 0)
      .setValue(GlowStickBlock.FACING, Direction.NORTH)
      .setValue(GlowStickBlock.WATERLOGGED, false)
      .setValue(GlowStickBlock.VARIANT, 1)
      .setValue(GlowStickBlock.CONTROLLED, false)
      .setValue(GlowStickBlock.POWERED, false);
  }

  public static BlockState getCreativeStateForPlacement(CreativeGlowStickBlock block,
    BlockPlaceContext context) {
    Direction clickedFace = context.getClickedFace();
    BlockPos blockPlacementPos = context.getClickedPos();
    if (!context.canPlace()) {
      blockPlacementPos = blockPlacementPos.relative(clickedFace);
    }

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
        .setValue(
          GlowStickBlock.WATERLOGGED,
          context.getLevel().getFluidState(blockPlacementPos).getType() == Fluids.WATER),
      context.getLevel(),
      blockPlacementPos);
  }

  public static BlockState getStateForPlacement(GlowStickBlock block, BlockPlaceContext context) {
    BlockPos blockPlacementPos = context.getClickedPos();
    if (!context.canPlace()) {
      blockPlacementPos = blockPlacementPos.relative(context.getClickedFace());
    }

    return RedstoneCapable.getInitialPlacementState(block.defaultBlockState()
        .setValue(GlowStickBlock.FACING, context.getHorizontalDirection().getOpposite()),
      context.getLevel(), blockPlacementPos);
  }

  public static FluidState getFluidState(BlockState blockState) {
    return blockState.getValue(GlowStickBlock.WATERLOGGED) ? Fluids.WATER.getSource(false)
      : Fluids.EMPTY.defaultFluidState();
  }

  public static void handleRandomTick(BlockState blockState, ServerLevel serverLevel,
    BlockPos blockPos, RandomSource random, int despawnTickRate) {
    if (despawnTickRate <= 0 || random.nextInt(despawnTickRate) != 0 || blockState.getValue(
      GlowStickBlock.CONTROLLED)) {
      return;
    }

    int currentAge = blockState.getValue(GlowStickBlock.AGE);
    if (currentAge >= 15) {
      serverLevel.destroyBlock(blockPos, true);
      return;
    }

    serverLevel.setBlockAndUpdate(blockPos,
      blockState.setValue(GlowStickBlock.AGE, currentAge + 1));
  }

  public static int calculateLightLevel(BlockState blockState) {
    if (blockState.getValue(GlowStickBlock.CONTROLLED) && !blockState.getValue(
      GlowStickBlock.POWERED)) {
      return 0;
    }

    int ageValue = blockState.getValue(GlowStickBlock.AGE);
    return (int) Math.round(15 - (ageValue < 10 ? ageValue * 0.25 : ageValue * 0.75));
  }
}
