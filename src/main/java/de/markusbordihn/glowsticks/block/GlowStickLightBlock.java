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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import de.markusbordihn.glowsticks.Constants;

public class GlowStickLightBlock extends Block {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected static final VoxelShape SHAPE_AABB = Block.box(7.5D, 7.5D, 7.5D, 8.5D, 8.5D, 8.5D);
  private static final int TICK_TTL = 10;

  public GlowStickLightBlock(Properties properties) {
    super(properties);
  }

  public void scheduleTick(Level level, BlockPos blockPos) {
    if (!level.getBlockTicks().hasScheduledTick(blockPos, this)) {
      level.getBlockTicks().scheduleTick(blockPos, this, TICK_TTL);
    }
  }

  @Override
  public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
      CollisionContext collisionContext) {
    return SHAPE_AABB;
  }

  @Override
  public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState,
      @Nullable LivingEntity placer, ItemStack itemStack) {
    scheduleTick(level, blockPos);
  }

  @Override
  public void tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random) {
    Block block = blockState.getBlock();
    if (!level.isClientSide && block instanceof GlowStickLightBlock) {
      level.removeBlock(blockPos, true);
    }
  }

}
