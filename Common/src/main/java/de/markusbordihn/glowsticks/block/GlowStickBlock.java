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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.block.glowstick.BlockStateManager;
import de.markusbordihn.glowsticks.block.glowstick.LavaInteraction;
import de.markusbordihn.glowsticks.block.glowstick.ParticleEffects;
import de.markusbordihn.glowsticks.block.glowstick.RedstoneCapable;
import de.markusbordihn.glowsticks.block.glowstick.WaypointNavigation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.DyeColor;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowStickBlock extends FallingBlock implements SimpleWaterloggedBlock {

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
  public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 5);
  public static final BooleanProperty CONTROLLED = BooleanProperty.create("controlled");
  public static final BooleanProperty POWERED = BooleanProperty.create("powered");
  public static final MapCodec<GlowStickBlock> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      propertiesCodec(),
                      DyeColor.CODEC.fieldOf("dye_color").forGetter(block -> block.dyeColor),
                      Codec.INT
                          .fieldOf("despawn_tick_rate")
                          .forGetter(block -> block.despawnTickRate))
                  .apply(instance, GlowStickBlock::new));
  public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 2, 14);

  private final DyeColor dyeColor;
  private final int despawnTickRate;

  public GlowStickBlock(Properties properties, DyeColor dyeColor, int despawnTickRate) {
    super(properties);
    this.dyeColor = dyeColor;
    this.despawnTickRate = despawnTickRate;
    this.registerDefaultState(BlockStateManager.createInitialBlockState(this));
  }

  public static int getLightLevel(BlockState blockState) {
    return BlockStateManager.calculateLightLevel(blockState);
  }

  public DyeColor getGlowStickColor() {
    return this.dyeColor;
  }

  @Override
  public Item asItem() {
    ResourceLocation resourceLocation =
        ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "glow_stick_" + this.dyeColor.getName());
    return BuiltInRegistries.ITEM.getValue(resourceLocation);
  }

  @Override
  public void onLand(
      Level level,
      BlockPos blockPos,
      BlockState fallingBlockState,
      BlockState surfaceBlockState,
      FallingBlockEntity fallingBlockEntity) {
    if (level.isClientSide) {
      return;
    }

    // Handle lava interaction first
    if (LavaInteraction.isLavaBlock(surfaceBlockState)) {
      LavaInteraction.handleLavaDestruction(level, blockPos);
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
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(AGE, FACING, VARIANT, WATERLOGGED, CONTROLLED, POWERED);
  }

  @Override
  public FluidState getFluidState(BlockState blockState) {
    return BlockStateManager.getFluidState(blockState);
  }

  @Override
  public void randomTick(
      BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random) {
    BlockStateManager.handleRandomTick(
        blockState, serverLevel, blockPos, random, this.despawnTickRate);
  }

  @Override
  public void neighborChanged(
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
  protected MapCodec<? extends FallingBlock> codec() {
    return CODEC;
  }

  @Override
  public void animateTick(
      BlockState blockState, Level level, BlockPos blockPos, RandomSource random) {
    super.animateTick(blockState, level, blockPos, random);

    if (!(level instanceof ClientLevel clientLevel)) {
      return;
    }

    // Handle normal glow particles
    ParticleEffects.handleParticleAnimation(blockState, level, blockPos, random, this.dyeColor);

    // Handle waypoint particles
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

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
    Level level = blockPlaceContext.getLevel();
    BlockPos placementPos = blockPlaceContext.getClickedPos();
    BlockPos belowPos = placementPos.below();
    BlockState belowState = level.getBlockState(belowPos);
    if (belowState.getBlock() instanceof GlowStickBlock) {
      return null;
    }

    // Check if there's already a GlowStickBlock at the placement position (replacement)
    BlockState existingState = level.getBlockState(placementPos);
    if (existingState.getBlock() instanceof GlowStickBlock) {
      return null;
    }

    return BlockStateManager.getStateForPlacement(this, blockPlaceContext);
  }
}
