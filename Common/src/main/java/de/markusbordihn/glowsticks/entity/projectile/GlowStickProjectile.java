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

package de.markusbordihn.glowsticks.entity.projectile;

import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.block.GlowStickLightBlock;
import de.markusbordihn.glowsticks.block.glowstick.LavaInteraction;
import de.markusbordihn.glowsticks.block.glowstick.PlacementSounds;
import de.markusbordihn.glowsticks.block.glowstick.RedstoneCapable;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GlowStickProjectile extends ThrowableItemProjectile {

  private static final int TICK_TTL = 650;
  protected final Supplier<Block> lightWaterBlock;
  protected final DyeColor dyeColor;
  protected final Supplier<Item> defaultItem;
  protected final Supplier<Block> lightBlock;
  protected Supplier<Block> defaultBlock;
  protected Direction defaultDirection = Direction.NORTH;
  private int ticks;

  public GlowStickProjectile(
      EntityType<? extends GlowStickProjectile> entityType,
      Level level,
      DyeColor dyeColor,
      Supplier<Block> block,
      Supplier<Item> item,
      Supplier<Block> light,
      Supplier<Block> lightWater) {
    super(entityType, level);
    this.dyeColor = dyeColor;
    this.defaultBlock = block;
    this.defaultItem = item;
    this.lightBlock = light;
    this.lightWaterBlock = lightWater;
  }

  public GlowStickProjectile(
      EntityType<? extends GlowStickProjectile> entityType,
      Level level,
      LivingEntity thrower,
      DyeColor dyeColor,
      Supplier<Block> block,
      Supplier<Item> item,
      Supplier<Block> light,
      Supplier<Block> lightWater) {
    super(entityType, thrower, level);
    this.dyeColor = dyeColor;
    this.defaultBlock = block;
    this.defaultDirection = thrower.getDirection().getOpposite();
    this.defaultItem = item;
    this.lightBlock = light;
    this.lightWaterBlock = lightWater;
  }

  public GlowStickProjectile(
      EntityType<? extends GlowStickProjectile> entityType,
      Level level,
      double x,
      double y,
      double z,
      DyeColor dyeColor,
      Supplier<Block> block,
      Supplier<Item> item,
      Supplier<Block> light,
      Supplier<Block> lightWater) {
    super(entityType, x, y, z, level);
    this.dyeColor = dyeColor;
    this.defaultBlock = block;
    this.defaultItem = item;
    this.lightBlock = light;
    this.lightWaterBlock = lightWater;
  }

  public DyeColor getDyeColor() {
    return dyeColor;
  }

  @Override
  protected Item getDefaultItem() {
    if (this.defaultItem != null) {
      return this.defaultItem.get();
    }
    return Items.STICK;
  }

  private boolean canPlaceBlock(final BlockState blockState, final BlockPos blockPos) {
    // Check if block is air or water first for performance
    if (blockState.isAir() || blockState.is(Blocks.WATER)) {
      return true;
    }

    // Check if block is replaceable using the modern method
    if (blockState.canBeReplaced()) {
      return true;
    }

    // Check for snow layers and powder snow (but NOT ice blocks themselves)
    if (blockState.is(Blocks.SNOW) || blockState.is(Blocks.POWDER_SNOW)) {
      return true;
    }

    // Check if block can be destroyed instantly (covers vegetation, torches, etc.)
    if (blockState.getDestroySpeed(this.level(), blockPos) == 0.0f) {
      return true;
    }

    // Additional check for liquid blocks
    if (!blockState.getFluidState().isEmpty()) {
      return true;
    }

    return false;
  }

  private void dropDefaultItem(final Level level, final BlockPos blockPos) {
    ItemEntity droppedItem =
        new ItemEntity(
            level,
            blockPos.getX() + 0.5,
            blockPos.getY() + 0.5,
            blockPos.getZ() + 0.5,
            new ItemStack(getDefaultItem()));

    droppedItem.setDeltaMovement(
        (level.random.nextFloat() - 0.5F) * 0.3F, 0.25F, (level.random.nextFloat() - 0.5F) * 0.3F);

    level.addFreshEntity(droppedItem);
  }

  @Override
  protected void onHit(HitResult hitResult) {
    super.onHit(hitResult);
    if (!this.level().isClientSide) {
      this.level().broadcastEntityEvent(this, (byte) 3);
      this.discard();
    }
  }

  @Override
  protected void onHitEntity(EntityHitResult entityHitResult) {
    super.onHitEntity(entityHitResult);
    if (!this.level().isClientSide) {
      Vec3 location = entityHitResult.getLocation();
      dropDefaultItem(
          this.level(), new BlockPos((int) location.x, (int) location.y, (int) location.z));
    }
    this.discard();
  }

  @Override
  protected void onHitBlock(BlockHitResult blockHitResult) {
    super.onHitBlock(blockHitResult);
    if (!this.level().isClientSide && defaultBlock != null) {
      BlockPos blockPos = blockHitResult.getBlockPos();
      BlockPos placePos = blockPos.relative(blockHitResult.getDirection());
      BlockState targetState = this.level().getBlockState(placePos);
      BlockPos belowPos = placePos.below();
      BlockState belowState = this.level().getBlockState(belowPos);

      // Check if there's a GlowStickBlock at placement position or below
      if (targetState.getBlock() instanceof GlowStickBlock
          || belowState.getBlock() instanceof GlowStickBlock) {
        dropDefaultItem(this.level(), placePos);
      } else if (canPlaceBlock(targetState, placePos)) {
        handleGlowStickPlacement(placePos, targetState);
      } else {
        dropDefaultItem(this.level(), placePos);
      }
    }
    this.discard();
  }

  private void handleGlowStickPlacement(BlockPos placePos, BlockState targetState) {
    if (LavaInteraction.isLavaBlock(targetState)) {
      handleLavaDestruction(placePos);
    } else {
      handleNormalPlacement(placePos, targetState);
    }
  }

  private void handleLavaDestruction(BlockPos lavaPos) {
    LavaInteraction.handleLavaDestruction(this.level(), lavaPos);
  }

  private void handleNormalPlacement(BlockPos placePos, BlockState targetState) {
    PlacementSounds.playPlacementSound(this.level(), placePos, targetState, this.random);
    placeGlowStickNormally(placePos, targetState);
  }

  private void placeGlowStickNormally(BlockPos placePos, BlockState targetState) {
    boolean isWater =
        targetState.is(Blocks.WATER)
            && targetState.getFluidState().getAmount() >= FluidState.AMOUNT_FULL;

    BlockState finalState = createProjectileGlowStickState(targetState, isWater);
    this.level().setBlockAndUpdate(placePos, finalState);
    RedstoneCapable.handleBlockPlacement(this.level(), placePos, finalState);
  }

  private BlockState createProjectileGlowStickState(BlockState targetState, boolean isWater) {
    return defaultBlock
        .get()
        .defaultBlockState()
        .setValue(HorizontalDirectionalBlock.FACING, defaultDirection)
        .setValue(GlowStickBlock.WATERLOGGED, isWater)
        .setValue(GlowStickBlock.VARIANT, random.nextInt(1, 6));
  }

  @Override
  public void tick() {
    if (!isAlive()) {
      return;
    }
    super.tick();

    // Remove projectile after TTL expires - check this BEFORE doing any expensive operations
    if (++ticks > TICK_TTL) {
      this.remove(RemovalReason.DISCARDED);
      return;
    }

    // Client-side particle effects - only if projectile will continue to exist
    if (this.level().isClientSide && !this.isInWater()) {
      if (ticks % 12 == 0) { // Average of previous range (10-15)
        Vec3 deltaMovement = this.getDeltaMovement();
        this.level()
            .addParticle(
                ParticleTypes.END_ROD,
                this.getX() + deltaMovement.x * 0.75,
                this.getY() + deltaMovement.y * 0.75,
                this.getZ() + deltaMovement.z * 0.75,
                deltaMovement.x,
                deltaMovement.y,
                deltaMovement.z);
      }
    }

    // Server-side light block placement - only every 5 ticks for performance
    if (!this.level().isClientSide && ticks % 5 == 0) {
      BlockPos lightBlockPosition = this.blockPosition().above();
      BlockState currentBlockState = this.level().getBlockState(lightBlockPosition);

      // Place new light block if position is air or water
      if (currentBlockState.isAir() || currentBlockState.is(Blocks.WATER)) {
        BlockState newLightBlockState =
            currentBlockState.is(Blocks.WATER)
                ? lightWaterBlock.get().defaultBlockState()
                : lightBlock.get().defaultBlockState();

        this.level().setBlockAndUpdate(lightBlockPosition, newLightBlockState);

        // Schedule tick for the light block if it's a GlowStickLightBlock
        Block placedBlock = this.level().getBlockState(lightBlockPosition).getBlock();
        if (placedBlock instanceof GlowStickLightBlock glowStickLightBlock) {
          glowStickLightBlock.scheduleTick(this.level(), lightBlockPosition);
        }
      }
      // Update existing light block to refresh its despawn timer
      else if (currentBlockState.getBlock() instanceof GlowStickLightBlock glowStickLightBlock) {
        glowStickLightBlock.scheduleTick(this.level(), lightBlockPosition);
      }
    }
  }
}
