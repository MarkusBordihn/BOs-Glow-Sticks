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
import de.markusbordihn.glowsticks.utils.GlowStickPlacementHelper;
import java.util.Objects;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
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
  protected Supplier<Block> defaultBlock = null;
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
    return this.defaultItem != null ? this.defaultItem.get() : null;
  }

  private boolean canPlaceBlock(final BlockState blockState) {
    // Check if block is air or water first for performance
    if (blockState.isAir() || blockState.is(Blocks.WATER)) {
      return true;
    }

    // Check material properties - covers most liquid and replaceable blocks
    Material material = blockState.getMaterial();
    if (material.isLiquid() || material.isReplaceable()) {
      return true;
    }

    // Check for snow layers and powder snow (but NOT ice blocks themselves)
    if (blockState.is(Blocks.SNOW) || blockState.is(Blocks.POWDER_SNOW)) {
      return true;
    }

    // Check if block can be destroyed by player without tools (covers most vegetation)
    if (blockState.getDestroySpeed(level, BlockPos.ZERO) == 0.0f) {
      return true;
    }

    // Additional check for blocks that can be replaced but might not be caught above
    // This covers most vegetation, flowers, grass variants, and other decorative blocks
    return material == Material.REPLACEABLE_PLANT
        || material == Material.REPLACEABLE_WATER_PLANT
        || material == Material.REPLACEABLE_FIREPROOF_PLANT;
  }

  private void dropDefaultItem(final Level level, final BlockPos blockPos) {
    level.addFreshEntity(
        new ItemEntity(
            level,
            blockPos.getX(),
            blockPos.getY(),
            blockPos.getZ(),
            new ItemStack(getDefaultItem())));
  }

  @Override
  protected void onHit(HitResult hitResult) {
    super.onHit(hitResult);
    if (!this.level.isClientSide) {
      this.level.broadcastEntityEvent(this, (byte) 3);
      this.discard();
    }
  }

  @Override
  protected void onHitEntity(EntityHitResult entityHitResult) {
    super.onHitEntity(entityHitResult);
    if (!this.level.isClientSide) {
      dropDefaultItem(this.level, new BlockPos(entityHitResult.getLocation()));
    }

    // Remove projectile after hitting an entity
    this.discard();
  }

  @Override
  protected void onHitBlock(BlockHitResult blockHitResult) {
    super.onHitBlock(blockHitResult);
    if (!this.level.isClientSide && defaultBlock != null) {
      BlockPos blockPos = blockHitResult.getBlockPos();
      BlockPos placePos = blockPos.relative(blockHitResult.getDirection());

      BlockState newGlowStickState = defaultBlock.get().defaultBlockState();
      BlockState targetState = level.getBlockState(placePos);

      if (canPlaceBlock(targetState)) {
        handleGlowStickPlacement(placePos, newGlowStickState, targetState);
      } else {
        dropDefaultItem(this.level, placePos);
      }
    }

    this.discard();
  }

  private void handleGlowStickPlacement(
      BlockPos placePos, BlockState glowStickState, BlockState targetState) {
    if (GlowStickPlacementHelper.isLavaBlock(targetState)) {
      handleLavaDestruction(placePos);
    } else {
      handleNormalPlacement(placePos, glowStickState, targetState);
    }
  }

  private void handleLavaDestruction(BlockPos lavaPos) {
    GlowStickPlacementHelper.handleLavaDestruction(this.level, lavaPos);
  }

  private void handleNormalPlacement(
      BlockPos placePos, BlockState glowStickState, BlockState targetState) {
    GlowStickPlacementHelper.playPlacementSound(this.level, placePos, targetState, this.random);
    placeGlowStickNormally(placePos, glowStickState, targetState);
  }

  private void placeGlowStickNormally(
      BlockPos placePos, BlockState glowStickState, BlockState targetState) {
    boolean isWater =
        targetState.is(Blocks.WATER)
            && targetState.getFluidState().getAmount() >= FluidState.AMOUNT_FULL;

    BlockState finalState =
        glowStickState
            .setValue(HorizontalDirectionalBlock.FACING, defaultDirection)
            .setValue(GlowStickBlock.WATERLOGGED, isWater)
            .setValue(GlowStickBlock.VARIANT, random.nextInt(1, 4));
    this.level.setBlockAndUpdate(placePos, finalState);
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
    if (this.level.isClientSide && !this.isInWater()) {
      if (ticks % 12 == 0) { // Average of previous range (10-15)
        Vec3 deltaMovement = this.getDeltaMovement();
        this.level.addParticle(
            ParticleTypes.END_ROD,
            this.getX() + deltaMovement.x * 0.75,
            this.getY() + deltaMovement.y * 0.75,
            this.getZ() + deltaMovement.z * 0.75,
            deltaMovement.x,
            deltaMovement.y,
            deltaMovement.z);
      }
    }

    // Server-side light block placement - only if projectile will continue to exist
    if (!this.level.isClientSide) {
      BlockPos lightBlockPosition = this.blockPosition().above();
      BlockState currentBlockState = this.level.getBlockState(lightBlockPosition);

      if (currentBlockState.isAir() || currentBlockState.is(Blocks.WATER)) {
        BlockState newLightBlockState =
            currentBlockState.is(Blocks.WATER)
                ? lightWaterBlock.get().defaultBlockState()
                : lightBlock.get().defaultBlockState();

        this.level.setBlockAndUpdate(lightBlockPosition, newLightBlockState);

        // Schedule tick for the light block if it's a GlowStickLightBlock
        Block placedBlock = this.level.getBlockState(lightBlockPosition).getBlock();
        if (placedBlock instanceof GlowStickLightBlock glowStickLightBlock) {
          glowStickLightBlock.scheduleTick(level, lightBlockPosition);
        }
      }
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof GlowStickProjectile other)) return false;
    if (!super.equals(obj)) return false;

    return ticks == other.ticks
        && dyeColor == other.dyeColor
        && defaultDirection == other.defaultDirection
        && Objects.equals(defaultBlock, other.defaultBlock)
        && Objects.equals(defaultItem, other.defaultItem)
        && Objects.equals(lightBlock, other.lightBlock)
        && Objects.equals(lightWaterBlock, other.lightWaterBlock);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + ticks;
    result = 31 * result + (dyeColor != null ? dyeColor.hashCode() : 0);
    result = 31 * result + (defaultDirection != null ? defaultDirection.hashCode() : 0);
    result = 31 * result + (defaultBlock != null ? defaultBlock.hashCode() : 0);
    result = 31 * result + (defaultItem != null ? defaultItem.hashCode() : 0);
    result = 31 * result + (lightBlock != null ? lightBlock.hashCode() : 0);
    result = 31 * result + (lightWaterBlock != null ? lightWaterBlock.hashCode() : 0);
    return result;
  }
}
