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

  public static int encodeControlState(boolean isControlled, int signal) {
    return isControlled ? Mth.clamp(signal, 0, MAX_SIGNAL) + 1 : UNCONTROLLED;
  }

  public static boolean isControlled(BlockState blockState) {
    return blockState.getValue(GlowStickBlock.REDSTONE_CONTROL) != UNCONTROLLED;
  }

  public static int getPower(BlockState blockState) {
    return Math.max(0, blockState.getValue(GlowStickBlock.REDSTONE_CONTROL) - 1);
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

  public static void handleNeighborChange(Level level, BlockPos blockPos) {
    recomputeChain(level, blockPos);
  }

  public static void handleBlockPlacement(Level level, BlockPos blockPos) {
    recomputeChain(level, blockPos);
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
    if (blockState.getValue(GlowStickBlock.REDSTONE_CONTROL) == controlValue) {
      return;
    }

    level.setBlock(
        blockPos,
        blockState.setValue(GlowStickBlock.REDSTONE_CONTROL, controlValue),
        Block.UPDATE_CLIENTS);
  }

  public static BlockState getInitialPlacementState(
      BlockState defaultState, Level level, BlockPos blockPos) {
    return defaultState.setValue(
        GlowStickBlock.REDSTONE_CONTROL,
        encodeControlState(
            hasRedstoneCapableNeighbor(level, blockPos), level.getBestNeighborSignal(blockPos)));
  }
}
