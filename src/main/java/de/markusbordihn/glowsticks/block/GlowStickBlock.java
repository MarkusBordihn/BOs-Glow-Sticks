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
    log.info("onBrokenAfterFall {} {}", blockPos);
    level.addFreshEntity(new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
        new ItemStack(getItem())));
  }

  @Override
  public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
      CollisionContext collisionContext) {
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

  @Override
  public FluidState getFluidState(BlockState blockState) {
    return Boolean.TRUE.equals(blockState.getValue(WATERLOGGED)) ? Fluids.WATER.getSource(false)
        : Fluids.EMPTY.defaultFluidState();
  }

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
