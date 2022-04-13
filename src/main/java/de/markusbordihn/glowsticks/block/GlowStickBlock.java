/**
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

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraftforge.fmllegacy.RegistryObject;

import de.markusbordihn.glowsticks.Constants;

public class GlowStickBlock extends FallingBlock implements SimpleWaterloggedBlock {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
  public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 1, 3);

  public static final VoxelShape SHAPE_AABB = Block.box(0, 0, 0, 16, 1, 16);

  public static final VoxelShape SHAPE_1_0deg_AABB = Block.box(1.65, 0, 4.75, 5.75, 1, 12.6);
  public static final VoxelShape SHAPE_1_90deg_AABB = Block.box(4.75, 0, 10.25, 12.6, 1, 14.35);
  public static final VoxelShape SHAPE_1_180deg_AABB = Block.box(10.25, 0, 3.4, 14.35, 1, 11.25);
  public static final VoxelShape SHAPE_1_270deg_AABB = Block.box(3.4, 0, 1.65, 11.25, 1, 5.75);

  public static final VoxelShape SHAPE_2_0deg_AABB = Block.box(5.5, 0, 2.4, 12, 1, 8.9);
  public static final VoxelShape SHAPE_2_90deg_AABB = Block.box(2.4, 0, 4, 8.9, 1, 10.5);
  public static final VoxelShape SHAPE_2_180deg_AABB = Block.box(4, 0, 7.1, 10.5, 1, 13.6);
  public static final VoxelShape SHAPE_2_270deg_AABB = Block.box(7.1, 0, 5.5, 13.6, 1, 12);

  public static final VoxelShape SHAPE_3_0deg_AABB = Block.box(2.5, 0, 9.1, 10.4, 1, 13.1);
  public static final VoxelShape SHAPE_3_90deg_AABB = Block.box(9.1, 0, 5.6, 13.1, 1, 13.5);
  public static final VoxelShape SHAPE_3_180deg_AABB = Block.box(5.6, 0, 2.9, 13.5, 1, 6.9);
  public static final VoxelShape SHAPE_3_270deg_AABB = Block.box(2.9, 0, 2.5, 6.9, 1, 10.4);

  private int randomTicker = 0;

  private RegistryObject<Item> registryItem = null;

  public GlowStickBlock(Properties properties, RegistryObject<Item> item) {
    super(properties);
    this.registerDefaultState(
        this.stateDefinition.any().setValue(AGE, 0).setValue(FACING, Direction.NORTH)
            .setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(VARIANT, 1));
    registryItem = item;
  }

  public static int getLightLevel(BlockState blockState) {
    int age = blockState.getValue(AGE);
    return (int) Math.round(15 - (age < 10 ? age * 0.25 : age * 0.75));
  }

  public Item getItem() {
    return registryItem != null ? registryItem.get() : null;
  }

  @Override
  public void onLand(Level level, BlockPos blockPos, BlockState blockState1, BlockState blockState2,
      FallingBlockEntity fallingBlockEntity) {
    fallingBlockEntity.playSound(SoundEvents.SCAFFOLDING_HIT, 1, 1);
  }

  @Override
  public void onBrokenAfterFall(Level level, BlockPos blockPos,
      FallingBlockEntity fallingBlockEntity) {
    level.addFreshEntity(new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
        new ItemStack(getItem())));
  }

  /** @deprecated */
  @Deprecated
  @Override
  public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
      CollisionContext collisionContext) {
    int shapeVariant = blockState.getValue(VARIANT);
    Direction shapeFacing = blockState.getValue(FACING);
    if (shapeVariant == 1) {
      switch (shapeFacing) {
        case NORTH:
          return SHAPE_1_90deg_AABB;
        case SOUTH:
          return SHAPE_1_270deg_AABB;
        case WEST:
          return SHAPE_1_180deg_AABB;
        case EAST:
        default:
          return SHAPE_1_0deg_AABB;
      }
    }
    if (shapeVariant == 2) {
      switch (shapeFacing) {
        case NORTH:
          return SHAPE_2_90deg_AABB;
        case SOUTH:
          return SHAPE_2_270deg_AABB;
        case WEST:
          return SHAPE_2_180deg_AABB;
        case EAST:
        default:
          return SHAPE_2_0deg_AABB;
      }
    }
    if (shapeVariant == 3) {
      switch (shapeFacing) {
        case NORTH:
          return SHAPE_3_90deg_AABB;
        case SOUTH:
          return SHAPE_3_270deg_AABB;
        case WEST:
          return SHAPE_3_180deg_AABB;
        case EAST:
        default:
          return SHAPE_3_0deg_AABB;
      }
    }
    return SHAPE_AABB;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockState) {
    blockState.add(AGE, FACING, VARIANT, WATERLOGGED);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return this.defaultBlockState().setValue(FACING,
        context.getHorizontalDirection().getOpposite());
  }

  /** @deprecated */
  @Deprecated
  @Override
  public FluidState getFluidState(BlockState blockState) {
    return Boolean.TRUE.equals(blockState.getValue(WATERLOGGED)) ? Fluids.WATER.getSource(false)
        : Fluids.EMPTY.defaultFluidState();
  }

  /** @deprecated */
  @Deprecated
  @Override
  public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos,
      Random random) {
    int age = blockState.getValue(AGE);
    if (age >= 15) {
      serverLevel.destroyBlock(blockPos, true);
    } else if (randomTicker++ % 2 == 0) {
      serverLevel.setBlockAndUpdate(blockPos, blockState.setValue(AGE, Integer.valueOf(age + 1)));
    }
  }

}
