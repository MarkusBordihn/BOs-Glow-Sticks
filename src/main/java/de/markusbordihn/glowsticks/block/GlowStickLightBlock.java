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
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import de.markusbordihn.glowsticks.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class GlowStickLightBlock extends Block {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected static final VoxelShape SHAPE_AABB = Block.box(7.5D, 7.5D, 7.5D, 8.5D, 8.5D, 8.5D);
  private static final int TICK_TTL = 10;

  public GlowStickLightBlock(Properties properties) {
    super(properties);
  }

  public void scheduleTick(World world, BlockPos blockPos) {
    if (!world.getBlockTicks().hasScheduledTick(blockPos, this)) {
      world.getBlockTicks().scheduleTick(blockPos, this, TICK_TTL);
    }
  }

  @Override
  public VoxelShape getShape(BlockState blockState, IBlockReader worldIn, BlockPos blockPos,
      ISelectionContext collisionContext) {
    return SHAPE_AABB;
  }

  @Override
  public void setPlacedBy(World world, BlockPos blockPos, BlockState blockState,
      @Nullable LivingEntity placer, ItemStack itemStack) {
    scheduleTick(world, blockPos);
  }

  @Override
  public void tick(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random) {
    Block block = blockState.getBlock();
    if (!world.isClientSide && block instanceof GlowStickLightBlock) {
      world.removeBlock(blockPos, true);
    }
  }

}
