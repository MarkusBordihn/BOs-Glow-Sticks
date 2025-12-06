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

import de.markusbordihn.glowsticks.block.GlowStickBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneCapable {

  public static boolean hasRedstoneCapableNeighbor(Level level, BlockPos blockPos) {
    for (Direction direction : Direction.values()) {
      BlockPos neighborPos = blockPos.relative(direction);
      BlockState neighborState = level.getBlockState(neighborPos);

      if (neighborState.isSignalSource()
          || level.getSignal(neighborPos, direction) > 0
          || level.getDirectSignal(neighborPos, direction) > 0) {
        return true;
      }
    }
    return false;
  }

  public static int getStrongestNeighborSignal(Level level, BlockPos blockPos) {
    int maxSignal = 0;

    for (Direction direction : Direction.values()) {
      BlockPos neighborPos = blockPos.relative(direction);

      int directSignal = level.getSignal(neighborPos, direction);
      maxSignal = Math.max(maxSignal, directSignal);
      if (maxSignal >= 15) {
        return 15;
      }

      int indirectSignal = level.getDirectSignal(neighborPos, direction);
      maxSignal = Math.max(maxSignal, indirectSignal);
      if (maxSignal >= 15) {
        return 15;
      }
    }

    return maxSignal;
  }

  public static void handleNeighborChange(BlockState blockState, Level level, BlockPos blockPos) {
    boolean shouldBeControlled = hasRedstoneCapableNeighbor(level, blockPos);
    boolean currentlyControlled = blockState.getValue(GlowStickBlock.CONTROLLED);

    if (shouldBeControlled != currentlyControlled) {
      updateControlState(blockState, level, blockPos, shouldBeControlled);
      return;
    }

    if (!currentlyControlled) {
      return;
    }

    updateRedstoneState(blockState, level, blockPos);
  }

  public static void handleBlockPlacement(Level level, BlockPos blockPos, BlockState blockState) {
    if (level.isClientSide) {
      return;
    }

    boolean hasRedstoneNeighbor = hasRedstoneCapableNeighbor(level, blockPos);
    if (hasRedstoneNeighbor != blockState.getValue(GlowStickBlock.CONTROLLED)) {
      updateControlState(blockState, level, blockPos, hasRedstoneNeighbor);
      updateNeighborConnections(level, blockPos);
    }
  }

  private static void updateControlState(
      BlockState blockState, Level level, BlockPos blockPos, boolean isControlled) {
    BlockState updatedState = blockState.setValue(GlowStickBlock.CONTROLLED, isControlled);

    if (isControlled) {
      int redstonePowerLevel = getStrongestNeighborSignal(level, blockPos);
      updatedState =
          updatedState
              .setValue(GlowStickBlock.POWERED, redstonePowerLevel > 0)
              .setValue(GlowStickBlock.AGE, 15 - redstonePowerLevel);
    } else {
      updatedState =
          updatedState.setValue(GlowStickBlock.POWERED, false).setValue(GlowStickBlock.AGE, 0);
    }

    level.setBlock(blockPos, updatedState, 3);
  }

  private static void updateRedstoneState(BlockState blockState, Level level, BlockPos blockPos) {
    int currentRedstonePower = 15 - blockState.getValue(GlowStickBlock.AGE);
    int newRedstonePower = getStrongestNeighborSignal(level, blockPos);

    if (currentRedstonePower != newRedstonePower) {
      BlockState updatedState =
          blockState
              .setValue(GlowStickBlock.POWERED, newRedstonePower > 0)
              .setValue(GlowStickBlock.AGE, 15 - newRedstonePower);
      level.setBlock(blockPos, updatedState, 3);
    }
  }

  public static BlockState getInitialPlacementState(
      BlockState defaultState, Level level, BlockPos blockPos) {
    boolean isRedstoneControlled = hasRedstoneCapableNeighbor(level, blockPos);
    int initialAge = 0;
    boolean isPowered = false;

    if (isRedstoneControlled) {
      int redstonePowerLevel = getStrongestNeighborSignal(level, blockPos);
      isPowered = redstonePowerLevel > 0;
      initialAge = 15 - redstonePowerLevel;
    }

    return defaultState
        .setValue(GlowStickBlock.CONTROLLED, isRedstoneControlled)
        .setValue(GlowStickBlock.POWERED, isPowered)
        .setValue(GlowStickBlock.AGE, initialAge);
  }

  private static void updateNeighborConnections(Level level, BlockPos blockPos) {
    level.updateNeighborsAt(blockPos, level.getBlockState(blockPos).getBlock());
    for (Direction direction : Direction.values()) {
      BlockPos neighborPos = blockPos.relative(direction);
      level.updateNeighborsAt(neighborPos, level.getBlockState(neighborPos).getBlock());
    }
  }
}
