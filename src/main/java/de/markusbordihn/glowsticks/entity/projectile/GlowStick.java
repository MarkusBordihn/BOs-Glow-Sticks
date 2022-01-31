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

package de.markusbordihn.glowsticks.entity.projectile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.registries.RegistryObject;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.block.GlowStickLightBlock;
import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.item.ModItems;

public class GlowStick extends ThrowableItemProjectile {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final int TICK_TTL = 650;
  private int ticks;

  private RegistryObject<Block> defaultBlock = null;
  private Direction defaultDirection = Direction.NORTH;

  public GlowStick(EntityType<? extends GlowStick> entityType, Level level) {
    super(entityType, level);
  }

  public GlowStick(Level level, LivingEntity player) {
    super(ModEntity.GLOW_STICK.get(), player, level);
    defaultDirection = player.getDirection().getOpposite();
  }

  public GlowStick(Level level, LivingEntity player, RegistryObject<Block> block) {
    super(ModEntity.GLOW_STICK.get(), player, level);
    defaultBlock = block;
    defaultDirection = player.getDirection().getOpposite();
  }

  public GlowStick(Level level, double x, double y, double z) {
    super(ModEntity.GLOW_STICK.get(), x, y, z, level);
  }

  protected Item getDefaultItem() {
    if (defaultBlock != null && defaultBlock.get() instanceof GlowStickBlock glowStickBlock) {
      return glowStickBlock.getItem();
    }
    return ModItems.GLOW_STICK_GREEN.get();
  }

  private boolean canPlaceBlock(BlockState blockState) {
    Material material = blockState.getMaterial();
    return blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.GRASS)
        || blockState.is(Blocks.TALL_GRASS) || blockState.is(Blocks.POPPY)
        || blockState.is(Blocks.KELP) || blockState.is(Blocks.SEAGRASS)
        || blockState.is(Blocks.TALL_SEAGRASS) || material.isLiquid() || material.isReplaceable()
        || blockState.getBlock() instanceof GlowStickBlock;
  }

  private void dropDefaultItem(Level level, BlockPos blockPos) {
    level.addFreshEntity(new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
        new ItemStack(getDefaultItem())));
  }

  @Override
  protected void onHit(HitResult result) {
    super.onHit(result);
    if (!this.level.isClientSide) {
      this.level.broadcastEntityEvent(this, (byte) 3);
      this.discard();
    }
  }

  @Override
  protected void onHitEntity(EntityHitResult result) {
    // Drop item if we hit an entity like the player or so.
    super.onHitEntity(result);
    if (!this.level.isClientSide) {
      dropDefaultItem(this.level, new BlockPos(result.getLocation()));
    }
  }

  @Override
  protected void onHitBlock(BlockHitResult result) {
    // Perform some checks to decide if we drop the item or place the item block instead.
    super.onHitBlock(result);
    if (!this.level.isClientSide && defaultBlock != null) {
      BlockPos blockPos = result.getBlockPos();
      BlockPos possibleBlockPosition = null;
      Direction direction = result.getDirection();
      BlockState blockState = defaultBlock.get().defaultBlockState();

      // Check for possible direction to place the glow stick
      if (direction == Direction.UP && canPlaceBlock(this.level.getBlockState(blockPos.above()))) {
        possibleBlockPosition = blockPos.above();
      } else if (direction == Direction.NORTH
          && canPlaceBlock(this.level.getBlockState(blockPos.north()))) {
        possibleBlockPosition = blockPos.north();
      } else if (direction == Direction.EAST
          && canPlaceBlock(this.level.getBlockState(blockPos.east()))) {
        possibleBlockPosition = blockPos.east();
      } else if (direction == Direction.SOUTH
          && canPlaceBlock(this.level.getBlockState(blockPos.south()))) {
        possibleBlockPosition = blockPos.south();
      } else if (direction == Direction.WEST
          && canPlaceBlock(this.level.getBlockState(blockPos.west()))) {
        possibleBlockPosition = blockPos.west();
      } else if (direction == Direction.DOWN
          && canPlaceBlock(this.level.getBlockState(blockPos.below()))) {
        possibleBlockPosition = blockPos.below();
      }

      // Check if we could place the block or if we are hitting any edge case.
      if (possibleBlockPosition != null && (this.level.getBlockState(possibleBlockPosition.below())
          .getBlock() instanceof GlowStickBlock
          || this.level.getBlockState(possibleBlockPosition)
              .getBlock() instanceof GlowStickBlock)) {
        // Skip existing glow stick block below or at place to avoid floating sticks
        dropDefaultItem(this.level, possibleBlockPosition);
      } else if (possibleBlockPosition != null) {
        // Check if placing position is full (>=8) water block to avoid jumping animations.
        BlockState blockStatePossiblePosition = this.level.getBlockState(possibleBlockPosition);
        boolean isWaterBlock = blockStatePossiblePosition.is(Blocks.WATER)
            && blockStatePossiblePosition.getFluidState().getAmount() >= FluidState.AMOUNT_FULL;

        // Place and update block facing to the current facing direction and use a random number for
        // the variants between 1 and 3.
        this.level.setBlockAndUpdate(possibleBlockPosition,
            blockState.setValue(GlowStickBlock.FACING, defaultDirection)
                .setValue(GlowStickBlock.WATERLOGGED, isWaterBlock)
                .setValue(GlowStickBlock.VARIANT, random.nextInt(1, 4)));
      }
    }
  }

  @Override
  public void tick() {
    if (!isAlive()) {
      return;
    }
    super.tick();

    if (this.level.isClientSide && !this.isInWater() && ticks % random.nextInt(10, 15) == 0) {
      // Add basic particle effect for every 10 to 15 ticks
      Vec3 vec3 = this.getDeltaMovement();
      double d2 = this.getX() + vec3.x;
      double d0 = this.getY() + vec3.y;
      double d1 = this.getZ() + vec3.z;
      this.level.addParticle(ParticleTypes.END_ROD, d2 - vec3.x * 0.25D, d0 - vec3.y * 0.25D,
          d1 - vec3.z * 0.25D, vec3.x, vec3.y, vec3.z);
    } else {
      // Place light blocks which are following the projectile
      BlockPos blockPos = this.blockPosition();
      if (blockPos != null) {

        BlockPos blockPosAbove = blockPos.above();
        BlockState blockState = this.level.getBlockState(blockPosAbove);

        // Make sure we are not destroying anything and only place the block for air and water.
        if (blockState.is(Blocks.AIR) || blockState.is(Blocks.WATER)) {
          // Use different kind of blocks depending on the environment.
          BlockState newBlockState = blockState.is(Blocks.WATER)
              ? ModBlocks.GLOW_STICK_LIGHT_WATER.get().defaultBlockState()
              : ModBlocks.GLOW_STICK_LIGHT.get().defaultBlockState();
          this.level.setBlockAndUpdate(blockPosAbove, newBlockState);
          if (this.level.getBlockState(blockPosAbove)
              .getBlock() instanceof GlowStickLightBlock glowStickLightBlock) {
            glowStickLightBlock.scheduleTick(level, blockPosAbove);
          }
        }
      }
    }

    // Automatically kill entity if it exceeds time to live.
    if (ticks++ > TICK_TTL) {
      this.remove(RemovalReason.DISCARDED);
    }
  }

}
