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
import de.markusbordihn.glowsticks.item.GlowStickColors;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
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

  private static final int LIFETIME_TICKS = 650;
  protected final Supplier<Block> lightWaterBlock;
  protected final DyeColor dyeColor;
  protected final Supplier<Item> defaultItem;
  protected final Supplier<Block> lightBlock;
  protected final Supplier<Block> defaultBlock;
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

    if (item != null && item.get() != null) {
      this.setItem(new ItemStack(item.get()));
    }
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
    super(entityType, thrower, level, new ItemStack(item.get()));
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
    super(entityType, x, y, z, level, new ItemStack(item.get()));
    this.dyeColor = dyeColor;
    this.defaultBlock = block;
    this.defaultItem = item;
    this.lightBlock = light;
    this.lightWaterBlock = lightWater;
  }

  public DyeColor getDyeColor() {
    return this.dyeColor;
  }

  @Override
  protected Item getDefaultItem() {
    if (this.defaultItem != null) {
      return this.defaultItem.get();
    }

    return Items.STICK;
  }

  private boolean canPlaceBlock(final BlockState blockState, final BlockPos blockPos) {
    return blockState.isAir()
      || blockState.is(Blocks.WATER)
      || blockState.canBeReplaced()
      || blockState.is(Blocks.SNOW)
      || blockState.is(Blocks.POWDER_SNOW)
      || !blockState.getFluidState().isEmpty()
      || blockState.getDestroySpeed(this.level(), blockPos) == 0.0f;
  }

  private void dropDefaultItem(final Level level, final BlockPos blockPos) {
    ItemEntity droppedItem =
      new ItemEntity(
        level,
        blockPos.getX() + 0.5,
        blockPos.getY() + 0.5,
        blockPos.getZ() + 0.5,
        new ItemStack(this.getDefaultItem()));

    droppedItem.setDeltaMovement(
      (level.getRandom().nextFloat() - 0.5F) * 0.3F,
      0.25F,
      (level.getRandom().nextFloat() - 0.5F) * 0.3F);
    droppedItem.setPickUpDelay(10);

    if (this.getOwner() != null) {
      droppedItem.setThrower(this.getOwner());
    }

    level.addFreshEntity(droppedItem);
  }

  @Override
  protected void onHit(HitResult hitResult) {
    super.onHit(hitResult);
    if (!this.level().isClientSide()) {
      this.level().broadcastEntityEvent(this, (byte) 3);
      this.discard();
    }
  }

  @Override
  protected void onHitEntity(EntityHitResult entityHitResult) {
    super.onHitEntity(entityHitResult);
    if (!this.level().isClientSide()) {
      Vec3 location = entityHitResult.getLocation();
      this.dropDefaultItem(
        this.level(), new BlockPos((int) location.x, (int) location.y, (int) location.z));
    }
    this.discard();
  }

  @Override
  protected void onHitBlock(BlockHitResult blockHitResult) {
    super.onHitBlock(blockHitResult);
    if (!this.level().isClientSide() && this.defaultBlock != null) {
      BlockPos blockPos = blockHitResult.getBlockPos();
      BlockPos placePos = blockPos.relative(blockHitResult.getDirection());
      BlockState targetState = this.level().getBlockState(placePos);
      BlockPos belowPos = placePos.below();
      BlockState belowState = this.level().getBlockState(belowPos);

      if (RedstoneCapable.isGlowStick(targetState) || RedstoneCapable.isGlowStick(belowState)) {
        this.dropDefaultItem(this.level(), placePos);
      } else if (this.canPlaceBlock(targetState, placePos)) {
        this.handleGlowStickPlacement(placePos, targetState);
      } else {
        this.dropDefaultItem(this.level(), placePos);
      }
    }
    this.discard();
  }

  private void handleGlowStickPlacement(BlockPos placePos, BlockState targetState) {
    if (LavaInteraction.isLavaBlock(targetState)) {
      this.handleLavaDestruction(placePos);
    } else {
      this.handleNormalPlacement(placePos, targetState);
    }
  }

  private void handleLavaDestruction(BlockPos lavaPos) {
    LavaInteraction.handleLavaDestruction(this.level(), lavaPos, this.getOwner());
  }

  private void handleNormalPlacement(BlockPos placePos, BlockState targetState) {
    PlacementSounds.playPlacementSound(this.level(), placePos, targetState, this.random);
    this.placeGlowStickNormally(placePos, targetState);
  }

  private void placeGlowStickNormally(BlockPos placePos, BlockState targetState) {
    boolean isWater =
      targetState.is(Blocks.WATER)
        && targetState.getFluidState().getAmount() >= FluidState.AMOUNT_FULL;

    this.level().setBlockAndUpdate(placePos, this.createProjectileGlowStickState(isWater));
    RedstoneCapable.handleBlockPlacement(this.level(), placePos);
  }

  private BlockState createProjectileGlowStickState(boolean isWater) {
    return this.defaultBlock
      .get()
      .defaultBlockState()
      .setValue(HorizontalDirectionalBlock.FACING, this.defaultDirection)
      .setValue(GlowStickBlock.WATERLOGGED, isWater);
  }

  @Override
  public void tick() {
    if (!this.isAlive()) {
      return;
    }

    super.tick();

    if (++this.ticks > LIFETIME_TICKS) {
      this.remove(RemovalReason.DISCARDED);
      return;
    }

    if (this.level().isClientSide()) {
      this.spawnTrailParticle();
    }

    if (!this.level().isClientSide()
      && this.ticks % 5 == 0
      && !this.placeLightBlock(this.blockPosition())) {
      this.placeLightBlock(this.blockPosition().above());
    }
  }

  private void spawnTrailParticle() {
    Vec3 deltaMovement = this.getDeltaMovement();
    if (this.isInWater()) {
      if (this.ticks % 4 == 0) {
        this.level()
          .addParticle(
            ParticleTypes.BUBBLE,
            this.getX(),
            this.getY(),
            this.getZ(),
            deltaMovement.x * 0.1,
            deltaMovement.y * 0.1,
            deltaMovement.z * 0.1);
      }
      return;
    }

    if (this.ticks % 2 == 0) {
      this.level()
        .addParticle(
          new DustParticleOptions(GlowStickColors.getRgb(this.dyeColor), 0.8F),
          this.getX() - deltaMovement.x * 0.5,
          this.getY() - deltaMovement.y * 0.5,
          this.getZ() - deltaMovement.z * 0.5,
          0.0,
          0.0,
          0.0);
    }
  }

  private boolean placeLightBlock(BlockPos lightBlockPosition) {
    BlockState currentBlockState = this.level().getBlockState(lightBlockPosition);
    if (currentBlockState.getBlock() instanceof GlowStickLightBlock existingLightBlock) {
      existingLightBlock.scheduleTick(this.level(), lightBlockPosition);
      return true;
    }

    if (!currentBlockState.isAir() && !currentBlockState.is(Blocks.WATER)) {
      return false;
    }

    this.level()
      .setBlockAndUpdate(
        lightBlockPosition,
        currentBlockState.is(Blocks.WATER)
          ? this.lightWaterBlock.get().defaultBlockState()
          : this.lightBlock.get().defaultBlockState());

    Block placedBlock = this.level().getBlockState(lightBlockPosition).getBlock();
    if (placedBlock instanceof GlowStickLightBlock glowStickLightBlock) {
      glowStickLightBlock.scheduleTick(this.level(), lightBlockPosition);
    }

    return true;
  }
}
