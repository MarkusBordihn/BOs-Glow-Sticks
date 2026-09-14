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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneCapable {

  public static final int UNCONTROLLED = 0;
  public static final int MAX_SIGNAL = 15;
  public static final int MAX_CONTROL_VALUE = MAX_SIGNAL + 1;
  public static final int CONTROLLED_LEVEL_OFFSET = BlockStateManager.MAX_AGE + 1;
  public static final int MAX_LEVEL = CONTROLLED_LEVEL_OFFSET + MAX_SIGNAL;

  public static int encodeControlState(boolean isControlled, int signal) {
    return isControlled ? Mth.clamp(signal, 0, MAX_SIGNAL) + 1 : UNCONTROLLED;
  }

  public static int getControlValue(BlockState blockState) {
    if (blockState.hasProperty(CreativeGlowStickBlock.REDSTONE_CONTROL)) {
      return blockState.getValue(CreativeGlowStickBlock.REDSTONE_CONTROL);
    }

    if (!blockState.hasProperty(GlowStickBlock.LEVEL)) {
      return UNCONTROLLED;
    }

    int level = blockState.getValue(GlowStickBlock.LEVEL);
    return level < CONTROLLED_LEVEL_OFFSET ? UNCONTROLLED : level - CONTROLLED_LEVEL_OFFSET + 1;
  }

  public static BlockState withControlValue(BlockState blockState, int controlValue) {
    if (blockState.hasProperty(CreativeGlowStickBlock.REDSTONE_CONTROL)) {
      return blockState.setValue(CreativeGlowStickBlock.REDSTONE_CONTROL, controlValue);
    }

    if (!blockState.hasProperty(GlowStickBlock.LEVEL)) {
      return blockState;
    }

    if (controlValue != UNCONTROLLED) {
      return blockState.setValue(GlowStickBlock.LEVEL, CONTROLLED_LEVEL_OFFSET + controlValue - 1);
    }

    if (!isControlled(blockState)) {
      return blockState;
    }

    return blockState.setValue(
        GlowStickBlock.LEVEL, BlockStateManager.MAX_AGE - getPower(blockState));
  }

  public static boolean isControlled(BlockState blockState) {
    return getControlValue(blockState) != UNCONTROLLED;
  }

  public static int getPower(BlockState blockState) {
    return Math.max(0, getControlValue(blockState) - 1);
  }

  public static boolean isPowered(BlockState blockState) {
    return getPower(blockState) > 0;
  }

  public static boolean isGlowStick(BlockState blockState) {
    return blockState.getBlock() instanceof GlowStickBlock
        || blockState.getBlock() instanceof CreativeGlowStickBlock;
  }

  public static boolean hasRedstoneCapableNeighbor(Level level, BlockPos blockPos) {
    for (Direction direction : Direction.values()) {
      if (level.getBlockState(blockPos.relative(direction)).isSignalSource()) {
        return true;
      }
    }
    return level.getBestNeighborSignal(blockPos) > 0;
  }

  public static void recomputeChain(Level level, BlockPos originPos) {
    if (level.isClientSide || !isGlowStick(level.getBlockState(originPos))) {
      return;
    }

    Set<BlockPos> chainPositions = collectChain(level, originPos);
    boolean isControlled = false;
    int chainPower = 0;
    for (BlockPos chainPos : chainPositions) {
      if (hasRedstoneCapableNeighbor(level, chainPos)) {
        isControlled = true;
      }
      chainPower = Math.max(chainPower, level.getBestNeighborSignal(chainPos));
    }

    for (BlockPos chainPos : chainPositions) {
      applyControlState(level, chainPos, isControlled, isControlled ? chainPower : 0);
    }
  }

  private static Set<BlockPos> collectChain(Level level, BlockPos originPos) {
    Set<BlockPos> visitedPositions = new LinkedHashSet<>();
    visitedPositions.add(originPos.immutable());

    int chainLimit = GlowSticksConfig.glowStickChainLimit;
    if (chainLimit <= 0) {
      return visitedPositions;
    }

    Deque<BlockPos> pendingPositions = new ArrayDeque<>(visitedPositions);
    while (!pendingPositions.isEmpty() && visitedPositions.size() < chainLimit) {
      BlockPos currentPos = pendingPositions.removeFirst();
      for (Direction direction : Direction.values()) {
        if (visitedPositions.size() >= chainLimit) {
          break;
        }
        BlockPos neighborPos = currentPos.relative(direction);
        if (level.isLoaded(neighborPos)
            && isGlowStick(level.getBlockState(neighborPos))
            && visitedPositions.add(neighborPos)) {
          pendingPositions.addLast(neighborPos);
        }
      }
    }

    return visitedPositions;
  }

  private static void applyControlState(
      Level level, BlockPos blockPos, boolean isControlled, int power) {
    BlockState blockState = level.getBlockState(blockPos);
    if (!isGlowStick(blockState)) {
      return;
    }

    int controlValue = encodeControlState(isControlled, power);
    if (getControlValue(blockState) == controlValue) {
      return;
    }

    level.setBlock(blockPos, withControlValue(blockState, controlValue), Block.UPDATE_CLIENTS);
  }

  public static BlockState getInitialPlacementState(
      BlockState defaultState, Level level, BlockPos blockPos) {
    return withControlValue(
        defaultState,
        encodeControlState(
            hasRedstoneCapableNeighbor(level, blockPos), level.getBestNeighborSignal(blockPos)));
  }
}
