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
import de.markusbordihn.glowsticks.block.glowstick.ParticleEffects;
import de.markusbordihn.glowsticks.block.glowstick.RedstoneCapable;
import de.markusbordihn.glowsticks.block.glowstick.WaypointNavigation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CreativeGlowStickBlock extends Block implements SimpleWaterloggedBlock {

  public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

  private static final VoxelShape SHAPE_FLOOR = Block.box(2, 0, 2, 14, 2, 14);
  private static final VoxelShape SHAPE_CEILING = Block.box(2, 14, 2, 14, 16, 14);
  private static final VoxelShape SHAPE_WALL_NORTH = Block.box(2, 2, 0, 14, 14, 2);
  private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(2, 2, 14, 14, 14, 16);
  private static final VoxelShape SHAPE_WALL_EAST = Block.box(14, 2, 2, 16, 14, 14);
  private static final VoxelShape SHAPE_WALL_WEST = Block.box(0, 2, 2, 2, 14, 14);

  private final DyeColor dyeColor;

  public CreativeGlowStickBlock(Properties properties, DyeColor dyeColor) {
    super(properties);
    this.dyeColor = dyeColor;
    this.registerDefaultState(
      this.stateDefinition
        .any()
        .setValue(FACE, AttachFace.FLOOR)
        .setValue(GlowStickBlock.FACING, Direction.NORTH)
        .setValue(GlowStickBlock.AGE, 0)
        .setValue(GlowStickBlock.VARIANT, 1)
        .setValue(GlowStickBlock.WATERLOGGED, false)
        .setValue(GlowStickBlock.CONTROLLED, false)
        .setValue(GlowStickBlock.POWERED, false));
  }

  public static int getLightLevel(BlockState blockState) {
    return BlockStateManager.calculateLightLevel(blockState);
  }

  public DyeColor getGlowStickColor() {
    return this.dyeColor;
  }

  private Direction getConnectedDirection(BlockState state) {
    return switch (state.getValue(FACE)) {
      case FLOOR -> Direction.DOWN;
      case CEILING -> Direction.UP;
      case WALL -> state.getValue(GlowStickBlock.FACING);
    };
  }

  @Override
  public VoxelShape getShape(
    BlockState blockState,
    BlockGetter blockGetter,
    BlockPos blockPos,
    CollisionContext collisionContext) {
    return switch (blockState.getValue(FACE)) {
      case FLOOR -> SHAPE_FLOOR;
      case CEILING -> SHAPE_CEILING;
      case WALL -> switch (blockState.getValue(GlowStickBlock.FACING)) {
        case NORTH -> SHAPE_WALL_NORTH;
        case SOUTH -> SHAPE_WALL_SOUTH;
        case EAST -> SHAPE_WALL_EAST;
        case WEST -> SHAPE_WALL_WEST;
        default -> SHAPE_FLOOR;
      };
    };
  }

  @Override
  public boolean canSurvive(BlockState blockState, LevelReader level, BlockPos pos) {
    Direction connected = getConnectedDirection(blockState);
    BlockPos supportPos = pos.relative(connected);
    return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, connected.getOpposite());
  }

  @Override
  protected BlockState updateShape(
    BlockState state,
    LevelReader level,
    ScheduledTickAccess scheduledTickAccess,
    BlockPos pos,
    Direction direction,
    BlockPos neighborPos,
    BlockState neighborState,
    RandomSource random) {
    if (direction == getConnectedDirection(state) && !state.canSurvive(level, pos)) {
      return Blocks.AIR.defaultBlockState();
    }
    if (state.getValue(GlowStickBlock.WATERLOGGED)) {
      scheduledTickAccess.scheduleTick(pos, Fluids.WATER, 5);
    }
    return super.updateShape(
      state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(
      FACE,
      GlowStickBlock.AGE,
      GlowStickBlock.FACING,
      GlowStickBlock.VARIANT,
      GlowStickBlock.WATERLOGGED,
      GlowStickBlock.CONTROLLED,
      GlowStickBlock.POWERED);
  }

  @Override
  public FluidState getFluidState(BlockState blockState) {
    return BlockStateManager.getFluidState(blockState);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
    return BlockStateManager.getCreativeStateForPlacement(this, blockPlaceContext);
  }

  @Override
  protected void neighborChanged(
    BlockState blockState,
    Level level,
    BlockPos blockPos,
    Block neighborBlock,
    Orientation orientation,
    boolean isMoving) {
    super.neighborChanged(blockState, level, blockPos, neighborBlock, orientation, isMoving);
    RedstoneCapable.handleNeighborChange(blockState, level, blockPos);
  }

  @Override
  public void animateTick(
    BlockState blockState, Level level, BlockPos blockPos, RandomSource random) {
    super.animateTick(blockState, level, blockPos, random);

    if (!(level instanceof ClientLevel clientLevel)) {
      return;
    }

    ParticleEffects.handleParticleAnimation(blockState, level, blockPos, random, this.dyeColor);
    WaypointNavigation.handleWaypointParticles(clientLevel, blockPos, random, this.dyeColor);
  }

  @Override
  public void setPlacedBy(
    Level level,
    BlockPos blockPos,
    BlockState blockState,
    LivingEntity placer,
    ItemStack itemStack) {
    super.setPlacedBy(level, blockPos, blockState, placer, itemStack);
    RedstoneCapable.handleBlockPlacement(level, blockPos, blockState);
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
    return blockState.getValue(GlowStickBlock.CONTROLLED);
  }
}
