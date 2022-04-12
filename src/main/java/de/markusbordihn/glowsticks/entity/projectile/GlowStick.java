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

import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import net.minecraftforge.fml.RegistryObject;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.block.GlowStickLightBlock;
import de.markusbordihn.glowsticks.block.GlowStickLightWaterBlock;
import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.item.ModItems;

public class GlowStick extends ProjectileItemEntity {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  public static final int FLUID_AMOUNT_FULL = 8;

  private static final int TICK_TTL = 650;
  private int ticks;

  private RegistryObject<Block> defaultBlock = null;
  private Direction defaultDirection = Direction.NORTH;

  public GlowStick(EntityType<? extends GlowStick> entityType, World world) {
    super(entityType, world);
  }

  public GlowStick(World world, LivingEntity player) {
    super(ModEntity.GLOW_STICK.get(), player, world);
    defaultDirection = player.getDirection().getOpposite();
  }

  public GlowStick(World world, LivingEntity player, RegistryObject<Block> block) {
    super(EntityType.SNOWBALL, player, world);
    defaultBlock = block;
    defaultDirection = player.getDirection().getOpposite();
  }

  public GlowStick(World level, double x, double y, double z) {
    super(ModEntity.GLOW_STICK.get(), x, y, z, level);
  }

  private boolean canPlaceBlock(BlockState blockState) {
    return blockState.is(Blocks.AIR) || blockState.is(Blocks.CAVE_AIR)
        || blockState.is(Blocks.VOID_AIR) || blockState.is(Blocks.WATER)
        || blockState.is(Blocks.GRASS) || blockState.is(Blocks.TALL_GRASS)
        || blockState.is(Blocks.POPPY) || blockState.is(Blocks.SNOW) || blockState.is(Blocks.KELP)
        || blockState.is(Blocks.SEAGRASS) || blockState.is(Blocks.TALL_SEAGRASS)
        || blockState.getBlock() instanceof GlowStickBlock
        || blockState.getBlock() instanceof GlowStickLightBlock
        || blockState.getBlock() instanceof GlowStickLightWaterBlock;
  }

  private void dropDefaultItem(World level, BlockPos blockPos) {
    level.addFreshEntity(new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
        new ItemStack(getDefaultItem())));
  }

  @Override
  protected Item getDefaultItem() {
    if (defaultBlock != null && defaultBlock.get() instanceof GlowStickBlock) {
      return ((GlowStickBlock) defaultBlock.get()).getItem();
    }
    return ModItems.GLOW_STICK_GREEN.get();
  }

  @Override
  protected void onHit(RayTraceResult result) {
    super.onHit(result);
    if (!this.level.isClientSide) {
      this.level.broadcastEntityEvent(this, (byte) 3);
      this.remove();
    }
  }

  @Override
  protected void onHitEntity(EntityRayTraceResult result) {
    // Drop item if we hit an entity like the player or so.
    super.onHitEntity(result);
    if (!this.level.isClientSide) {
      dropDefaultItem(this.level, new BlockPos(result.getLocation()));
    }
  }

  @Override
  protected void onHitBlock(BlockRayTraceResult result) {
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
            && blockStatePossiblePosition.getFluidState().getAmount() >= FLUID_AMOUNT_FULL;

        // Place and update block facing to the current facing direction and use a random number for
        // the variants between 1 and 3.
        this.level.setBlockAndUpdate(possibleBlockPosition,
            blockState.setValue(GlowStickBlock.FACING, defaultDirection)
                .setValue(GlowStickBlock.WATERLOGGED, isWaterBlock)
                .setValue(GlowStickBlock.VARIANT, RandomUtils.nextInt(1, 4)));
      }
    }
  }

  @Override
  public void tick() {
    if (!isAlive()) {
      return;
    }
    super.tick();

    if (this.level.isClientSide && !this.isInWater() && ticks % RandomUtils.nextInt(10, 15) == 0) {
      // Add basic particle effect for every 10 to 15 ticks
      Vector3d vec3 = this.getDeltaMovement();
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
        if (blockState.is(Blocks.AIR) || blockState.is(Blocks.CAVE_AIR)
            || blockState.is(Blocks.VOID_AIR) || blockState.is(Blocks.WATER)) {
          // Use different kind of blocks depending on the environment.
          BlockState newBlockState = blockState.is(Blocks.WATER)
              ? ModBlocks.GLOW_STICK_LIGHT_WATER.get().defaultBlockState()
              : ModBlocks.GLOW_STICK_LIGHT.get().defaultBlockState();
          this.level.setBlockAndUpdate(blockPosAbove, newBlockState);
          Block block = this.level.getBlockState(blockPosAbove).getBlock();
          if (block instanceof GlowStickLightBlock) {
            ((GlowStickLightBlock) block).scheduleTick(level, blockPosAbove);
          }
        }
      }
    }

    // Automatically kill entity if it exceeds time to live.
    if (ticks++ > TICK_TTL) {
      this.remove();
    }
  }

  @Override
  public IPacket<?> getAddEntityPacket() {
    return new SSpawnObjectPacket(this);
  }

}
