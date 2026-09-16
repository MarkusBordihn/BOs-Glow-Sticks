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

import de.markusbordihn.glowsticks.block.glowstick.BlockStateManager;
import de.markusbordihn.glowsticks.block.glowstick.FallingLightTrail;
import de.markusbordihn.glowsticks.block.glowstick.LavaInteraction;
import de.markusbordihn.glowsticks.block.glowstick.ParticleEffects;
import de.markusbordihn.glowsticks.block.glowstick.RedstoneCapable;
import de.markusbordihn.glowsticks.block.glowstick.RegisteredItemReference;
import de.markusbordihn.glowsticks.block.glowstick.WaypointNavigation;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.item.GlowStickColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowStickBlock extends FallingBlock implements SimpleWaterloggedBlock {

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final IntegerProperty LEVEL =
    IntegerProperty.create("level", 0, RedstoneCapable.MAX_LEVEL);

  public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 2, 14);

  private static final int PLACEMENT_TICK_DELAY = 2;

  private final GlowStickColor glowStickColor;
  private final RegisteredItemReference itemReference;

  public GlowStickBlock(Properties properties, GlowStickColor glowStickColor) {
    super(properties);
    this.glowStickColor = glowStickColor;
    this.itemReference = new RegisteredItemReference("glow_stick_" + glowStickColor.getName());
    this.registerDefaultState(BlockStateManager.createInitialBlockState(this));
  }

  public static int getLightLevel(BlockState blockState) {
    return BlockStateManager.calculateLightLevel(blockState);
  }

  public GlowStickColor getGlowStickColor() {
    return this.glowStickColor;
  }

  @Override
  public Item asItem() {
    return this.itemReference.get();
  }

  @Override
  public void onLand(
    Level level,
    BlockPos blockPos,
    BlockState fallingBlockState,
    BlockState surfaceBlockState,
    FallingBlockEntity fallingBlockEntity) {
    if (level.isClientSide()) {
      return;
    }

    if (LavaInteraction.isLavaBlock(surfaceBlockState)) {
      LavaInteraction.handleLavaDestruction(level, blockPos, fallingBlockEntity);
      fallingBlockEntity.discard();
    }
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
  public boolean canSurvive(BlockState blockState, LevelReader level, BlockPos blockPos) {
    return !RedstoneCapable.isGlowStick(level.getBlockState(blockPos.below()));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(LEVEL, FACING, WATERLOGGED);
  }

  @Override
  public FluidState getFluidState(BlockState blockState) {
    return BlockStateManager.getFluidState(blockState);
  }

  public void scheduleDespawnTick(ScheduledTickAccess scheduledTickAccess, BlockPos blockPos) {
    int stageTicks = GlowSticksConfig.getGlowStickStageTicks();
    if (stageTicks > 0 && !scheduledTickAccess.getBlockTicks().hasScheduledTick(blockPos, this)) {
      scheduledTickAccess.scheduleTick(blockPos, this, stageTicks);
    }
  }

  @Override
  public void onPlace(
    BlockState blockState,
    Level level,
    BlockPos blockPos,
    BlockState oldBlockState,
    boolean isMoving) {
    if (isFree(level.getBlockState(blockPos.below()))) {
      if (!level.getBlockTicks().hasScheduledTick(blockPos, this)) {
        level.scheduleTick(blockPos, this, PLACEMENT_TICK_DELAY);
      }
      return;
    }

    this.scheduleDespawnTick(level, blockPos);
  }

  @Override
  protected BlockState updateShape(
    BlockState blockState,
    LevelReader level,
    ScheduledTickAccess scheduledTickAccess,
    BlockPos blockPos,
    Direction direction,
    BlockPos neighborPos,
    BlockState neighborState,
    RandomSource random) {
    if (blockState.getValue(WATERLOGGED)) {
      scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
    }
    return blockState;
  }

  @Override
  public void tick(
    BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random) {
    if (isFree(serverLevel.getBlockState(blockPos.below()))
      && blockPos.getY() >= serverLevel.getMinY()) {
      FallingBlockEntity.fall(serverLevel, blockPos, blockState);
      FallingLightTrail.placeTrail(serverLevel, blockPos);
      return;
    }

    BlockStateManager.handleDespawnTick(blockState, serverLevel, blockPos, this);
  }

  @Override
  public void randomTick(
    BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random) {
    this.scheduleDespawnTick(serverLevel, blockPos);
  }

  @Override
  public void neighborChanged(
    BlockState blockState,
    Level level,
    BlockPos blockPos,
    Block neighborBlock,
    Orientation orientation,
    boolean isMoving) {
    if (level.isClientSide()) {
      return;
    }

    if (level instanceof ServerLevel serverLevel
      && isFree(serverLevel.getBlockState(blockPos.below()))
      && blockPos.getY() >= serverLevel.getMinY()) {
      FallingBlockEntity.fall(serverLevel, blockPos, blockState);
      FallingLightTrail.placeTrail(serverLevel, blockPos);
      return;
    }

    RedstoneCapable.recomputeChain(level, blockPos);
  }

  @Override
  public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
    return this.glowStickColor.getOpaqueArgb();
  }

  @Override
  public void animateTick(
    BlockState blockState, Level level, BlockPos blockPos, RandomSource random) {
    super.animateTick(blockState, level, blockPos, random);

    if (!(level instanceof ClientLevel clientLevel)) {
      return;
    }

    ParticleEffects.handleParticleAnimation(
      blockState, clientLevel, blockPos, random, this.glowStickColor);
    WaypointNavigation.handleWaypointParticles(clientLevel, blockPos, random, this.glowStickColor);
  }

  @Override
  public void setPlacedBy(
    Level level,
    BlockPos blockPos,
    BlockState blockState,
    LivingEntity placer,
    ItemStack itemStack) {
    super.setPlacedBy(level, blockPos, blockState, placer, itemStack);
    RedstoneCapable.recomputeChain(level, blockPos);
  }

  @Override
  public int getSignal(
    BlockState blockState, BlockGetter level, BlockPos blockPos, Direction direction) {
    return 0;
  }

  @Override
  public int getDirectSignal(
    BlockState blockState, BlockGetter level, BlockPos blockPos, Direction direction) {
    return 0;
  }

  @Override
  public boolean isSignalSource(BlockState blockState) {
    return false;
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
    Level level = blockPlaceContext.getLevel();
    BlockPos placementPos = blockPlaceContext.getClickedPos();

    if (level.getBlockState(placementPos).getBlock() instanceof GlowStickBlock) {
      return null;
    }

    return BlockStateManager.getStateForPlacement(this, blockPlaceContext);
  }
}
