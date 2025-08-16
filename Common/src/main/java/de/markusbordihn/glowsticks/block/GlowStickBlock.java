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

package de.markusbordihn.glowsticks.block;

import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import de.markusbordihn.glowsticks.utils.GlowStickPlacementHelper;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowStickBlock extends FallingBlock implements SimpleWaterloggedBlock {

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
  public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 1, 3);

  public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 2, 14);

  private final Supplier<Item> glowStickItemSupplier;

  public GlowStickBlock(Properties properties, Supplier<Item> glowStickItemSupplier) {
    super(properties);
    this.glowStickItemSupplier = glowStickItemSupplier;
    this.registerDefaultState(createInitialBlockState());
  }

  public static int getLightLevel(BlockState blockState) {
    int ageValue = blockState.getValue(AGE);
    return (int) Math.round(15 - (ageValue < 10 ? ageValue * 0.25 : ageValue * 0.75));
  }

  public boolean isDespawnEnabled() {
    try {
      if (glowStickItemSupplier.get() instanceof GlowStickItem glowStickItem) {
        return glowStickItem.isDespawnEnabled();
      }
    } catch (Exception e) {
      return GlowSticksConfig.glowStickDespawnEnabled;
    }
    return GlowSticksConfig.glowStickDespawnEnabled;
  }

  public int getDespawnTickRate() {
    try {
      if (glowStickItemSupplier.get() instanceof GlowStickItem glowStickItem) {
        return glowStickItem.getDespawnTickRate();
      }
    } catch (Exception e) {
      return GlowSticksConfig.glowStickDespawnTicks;
    }
    return GlowSticksConfig.glowStickDespawnTicks;
  }

  public Item getItem() {
    return glowStickItemSupplier.get();
  }

  @Override
  public void onLand(
      Level level,
      BlockPos blockPos,
      BlockState fallingBlockState,
      BlockState surfaceBlockState,
      FallingBlockEntity fallingBlockEntity) {

    if (GlowStickPlacementHelper.isLavaBlock(surfaceBlockState)) {
      handleLavaDestruction(level, blockPos, fallingBlockEntity);
    } else {
      handleNormalLanding(
          level, blockPos, fallingBlockState, surfaceBlockState, fallingBlockEntity);
    }
  }

  @Override
  public void onBrokenAfterFall(
      Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
    ItemEntity droppedItem =
        new ItemEntity(
            level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(getItem()));
    level.addFreshEntity(droppedItem);
  }

  @Override
  public VoxelShape getShape(
      BlockState blockState,
      BlockGetter blockGetter,
      BlockPos blockPos,
      CollisionContext collisionContext) {
    return SHAPE;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(AGE, FACING, VARIANT, WATERLOGGED);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
    Direction playerFacingDirection = blockPlaceContext.getHorizontalDirection().getOpposite();
    return this.defaultBlockState().setValue(FACING, playerFacingDirection);
  }

  @Override
  public FluidState getFluidState(BlockState blockState) {
    return blockState.getValue(WATERLOGGED)
        ? Fluids.WATER.getSource(false)
        : Fluids.EMPTY.defaultFluidState();
  }

  @Override
  public void randomTick(
      BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random) {
    if (!isDespawnEnabled()) {
      return;
    }

    // Use a simple probability check based on despawn tick rate
    if (random.nextInt(getDespawnTickRate()) == 0) {
      advanceGlowStickAge(blockState, serverLevel, blockPos);
    }
  }

  private BlockState createInitialBlockState() {
    return this.stateDefinition
        .any()
        .setValue(AGE, 0)
        .setValue(FACING, Direction.NORTH)
        .setValue(WATERLOGGED, false)
        .setValue(VARIANT, 1);
  }

  private void handleLavaDestruction(
      Level level, BlockPos lavaPos, FallingBlockEntity fallingBlockEntity) {
    GlowStickPlacementHelper.handleLavaDestruction(level, lavaPos);
    fallingBlockEntity.discard();
  }

  private void handleNormalLanding(
      Level level,
      BlockPos blockPos,
      BlockState glowStickState,
      BlockState surfaceState,
      FallingBlockEntity fallingBlockEntity) {
    GlowStickPlacementHelper.playPlacementSound(level, blockPos, surfaceState, level.random);
    level.setBlock(blockPos, glowStickState, 3);
    fallingBlockEntity.discard();
  }

  private void advanceGlowStickAge(
      BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
    int currentAge = blockState.getValue(AGE);
    int newAge = currentAge + 1;

    if (newAge >= 15) {
      serverLevel.destroyBlock(blockPos, true);
    } else {
      BlockState updatedState = blockState.setValue(AGE, newAge);
      serverLevel.setBlockAndUpdate(blockPos, updatedState);
    }
  }
}
