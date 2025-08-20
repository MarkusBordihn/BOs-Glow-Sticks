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

import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import de.markusbordihn.glowsticks.utils.GlowStickPlacementHelper;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class GlowStickBlock extends FallingBlock implements SimpleWaterloggedBlock {

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
  public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 1, 3);

  public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 2, 14);

  private final Supplier<Item> glowStickItemSupplier;

  public GlowStickBlock(Properties properties, Supplier<Item> glowStickItemSupplier) {
    super(properties);
    this.glowStickItemSupplier = glowStickItemSupplier;
    this.registerDefaultState(createInitialBlockState());
  }

  public static int getLightLevel(BlockState blockState) {
    int ageValue = blockState.getValue(AGE);
    return (int) Math.round(15 - (ageValue < 10 ? ageValue * 0.25 : ageValue * 0.75));
  }

  public boolean isDespawnEnabled() {
    try {
      if (glowStickItemSupplier.get() instanceof GlowStickItem glowStickItem) {
        return glowStickItem.isDespawnEnabled();
      }
    } catch (Exception e) {
      // Fallback to default despawn configuration
    }
    return GlowSticksConfig.despawnEnabled;
  }

  public int getDespawnTickRate() {
    try {
      if (glowStickItemSupplier.get() instanceof GlowStickItem glowStickItem) {
        return glowStickItem.getDespawnTickRate();
      }
    } catch (Exception e) {
      // Fallback to default despawn tick rate configuration
    }
    return GlowSticksConfig.despawnTicks;
  }

  public DyeColor getGlowStickColor() {
    try {
      if (glowStickItemSupplier.get() instanceof GlowStickItem glowStickItem) {
        return glowStickItem.getDyeColor();
      }
    } catch (Exception e) {
      // Fallback to default color
    }
    return DyeColor.WHITE;
  }

  public Item getItem() {
    return glowStickItemSupplier.get();
  }

  @Override
  public void onLand(
      Level level,
      BlockPos blockPos,
      BlockState fallingBlockState,
      BlockState surfaceBlockState,
      FallingBlockEntity fallingBlockEntity) {

    if (GlowStickPlacementHelper.isLavaBlock(surfaceBlockState)) {
      handleLavaDestruction(level, blockPos, fallingBlockEntity);
    } else {
      handleNormalLanding(
          level, blockPos, fallingBlockState, surfaceBlockState, fallingBlockEntity);
    }
  }

  @Override
  public void onBrokenAfterFall(
      Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
    ItemEntity droppedItem =
        new ItemEntity(
            level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(getItem()));
    level.addFreshEntity(droppedItem);
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
    builder.add(AGE, FACING, VARIANT, WATERLOGGED);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
    Direction playerFacingDirection = blockPlaceContext.getHorizontalDirection().getOpposite();
    return this.defaultBlockState().setValue(FACING, playerFacingDirection);
  }

  @Override
  public FluidState getFluidState(BlockState blockState) {
    return blockState.getValue(WATERLOGGED)
        ? Fluids.WATER.getSource(false)
        : Fluids.EMPTY.defaultFluidState();
  }

  @Override
  public void randomTick(
      BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random) {
    if (!isDespawnEnabled()) {
      return;
    }

    // Use a simple probability check based on despawn tick rate
    if (random.nextInt(getDespawnTickRate()) == 0) {
      advanceGlowStickAge(blockState, serverLevel, blockPos);
    }
  }

  @Override
  public void animateTick(
      BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
    super.animateTick(blockState, level, blockPos, randomSource);

    if (!(level instanceof ClientLevel clientLevel)) {
      return;
    }

    // Normal glow particles
    if (shouldSpawnNormalParticles(blockState, randomSource)) {
      int currentAge = blockState.getValue(AGE);
      ParticleProperties particleProperties = calculateParticleProperties(currentAge);
      Vector3f adjustedColors =
          calculateAdjustedColors(
              getGlowStickColor().getTextureDiffuseColors(), particleProperties.brightness);
      spawnMainParticle(
          clientLevel, blockPos, randomSource, adjustedColors, particleProperties.size);

      if (shouldSpawnExtraParticle(currentAge, randomSource)) {
        spawnExtraParticle(
            clientLevel, blockPos, randomSource, adjustedColors, particleProperties.size);
      }
    }

    // Waypoint particles from player to nearest matching glow stick
    if (shouldSpawnWaypointParticles(clientLevel, blockPos, randomSource)) {
      spawnWaypointParticles(clientLevel, blockPos, randomSource);
    }
  }

  private boolean shouldSpawnNormalParticles(BlockState blockState, RandomSource randomSource) {
    if (!GlowSticksConfig.spawnRandomParticles) {
      return false;
    }

    if (randomSource.nextInt(GlowSticksConfig.randomParticleSpawnRate) != 0) {
      return false;
    }

    return blockState.getValue(AGE) < 15;
  }

  private ParticleProperties calculateParticleProperties(int currentAge) {
    float ageFactor = Math.max(0.2f, 1.0f - (currentAge / 15.0f));
    float size = Math.max(0.8f, ageFactor * 1.2f);

    return new ParticleProperties(1, ageFactor, size);
  }

  private Vector3f calculateAdjustedColors(float[] baseColors, float brightness) {
    float adjustedRed = Math.min(1.0f, baseColors[0] * brightness);
    float adjustedGreen = Math.min(1.0f, baseColors[1] * brightness);
    float adjustedBlue = Math.min(1.0f, baseColors[2] * brightness);

    return new Vector3f(adjustedRed, adjustedGreen, adjustedBlue);
  }

  private void spawnMainParticle(
      ClientLevel clientLevel,
      BlockPos blockPos,
      RandomSource randomSource,
      Vector3f colors,
      float size) {
    clientLevel.addParticle(
        new DustParticleOptions(colors, size),
        blockPos.getX() + 0.5 + (randomSource.nextFloat() - 0.5) * 0.3,
        blockPos.getY() + 0.1,
        blockPos.getZ() + 0.5 + (randomSource.nextFloat() - 0.5) * 0.3,
        0,
        0.01,
        0);
  }

  private boolean shouldSpawnExtraParticle(int currentAge, RandomSource randomSource) {
    return currentAge <= 5 && randomSource.nextInt(3) == 0;
  }

  private void spawnExtraParticle(
      ClientLevel clientLevel,
      BlockPos blockPos,
      RandomSource randomSource,
      Vector3f colors,
      float size) {
    clientLevel.addParticle(
        new DustParticleOptions(colors, size * 0.7f),
        blockPos.getX() + 0.5 + (randomSource.nextFloat() - 0.5) * 0.4,
        blockPos.getY() + 0.15,
        blockPos.getZ() + 0.5 + (randomSource.nextFloat() - 0.5) * 0.4,
        0,
        0.005,
        0);
  }

  private BlockState createInitialBlockState() {
    return this.stateDefinition
        .any()
        .setValue(AGE, 0)
        .setValue(FACING, Direction.NORTH)
        .setValue(WATERLOGGED, false)
        .setValue(VARIANT, 1);
  }

  private void handleLavaDestruction(
      Level level, BlockPos lavaPos, FallingBlockEntity fallingBlockEntity) {
    GlowStickPlacementHelper.handleLavaDestruction(level, lavaPos);
    fallingBlockEntity.discard();
  }

  private void handleNormalLanding(
      Level level,
      BlockPos blockPos,
      BlockState glowStickState,
      BlockState surfaceState,
      FallingBlockEntity fallingBlockEntity) {
    GlowStickPlacementHelper.playPlacementSound(level, blockPos, surfaceState, level.random);
    level.setBlock(blockPos, glowStickState, 3);
    fallingBlockEntity.discard();
  }

  private void advanceGlowStickAge(
      BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
    int newAge = blockState.getValue(AGE) + 1;
    if (newAge >= 15) {
      serverLevel.destroyBlock(blockPos, true);
    } else {
      BlockState updatedState = blockState.setValue(AGE, newAge);
      serverLevel.setBlockAndUpdate(blockPos, updatedState);
    }
  }

  private boolean shouldSpawnWaypointParticles(
      ClientLevel clientLevel, BlockPos blockPos, RandomSource randomSource) {
    if (!GlowSticksConfig.spawnWaypointParticles) {
      return false;
    }

    // Check if player is sneaking.
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null || !player.isShiftKeyDown() || randomSource.nextInt(2) != 0) {
      return false;
    }

    // Check if player is holding a glow stick of the correct color.
    DyeColor heldColor = getHeldGlowStickColor(player);
    if (heldColor == null || heldColor != getGlowStickColor()) {
      return false;
    }

    return isNearestMatchingGlowStick(clientLevel, blockPos, player, heldColor);
  }

  private DyeColor getHeldGlowStickColor(LocalPlayer player) {
    if (player.getMainHandItem().getItem() instanceof GlowStickItem glowStickItem) {
      return glowStickItem.getDyeColor();
    }
    if (player.getOffhandItem().getItem() instanceof GlowStickItem glowStickItem) {
      return glowStickItem.getDyeColor();
    }

    return DyeColor.WHITE;
  }

  private boolean isNearestMatchingGlowStick(
      ClientLevel clientLevel, BlockPos currentBlockPos, LocalPlayer player, DyeColor targetColor) {
    BlockPos playerPos = player.blockPosition();
    double currentDistance = playerPos.distSqr(currentBlockPos);

    // Always ignore glow stick if player is standing on or very close to it
    if (currentDistance < 6.0) {
      return false;
    }

    // Search for nearest matching glow stick within the defined radius
    int horizontalRadius = GlowSticksConfig.waypointSearchRadius;
    int verticalRadius = GlowSticksConfig.waypointVerticalSearchRadius;
    for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
      for (int y = -verticalRadius; y <= verticalRadius; y++) {
        for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
          BlockPos checkPos = playerPos.offset(x, y, z);
          if (checkPos.equals(currentBlockPos)) {
            continue;
          }

          // Check if the block at the position is a glow stick of the target color
          BlockState checkState = clientLevel.getBlockState(checkPos);
          if (checkState.getBlock() instanceof GlowStickBlock otherGlowStick
              && otherGlowStick.getGlowStickColor() == targetColor) {
            double otherDistance = playerPos.distSqr(checkPos);
            // Skip glow sticks that are too close (player standing on them)
            if (otherDistance >= 6.0 && otherDistance < currentDistance) {
              return false;
            }
          }
        }
      }
    }

    return true;
  }

  private void spawnWaypointParticles(
      ClientLevel clientLevel, BlockPos blockPos, RandomSource randomSource) {
    // Preserve original color ratios while ensuring visibility
    float[] baseColors = getGlowStickColor().getTextureDiffuseColors();
    float maxColorValue = Math.max(baseColors[0], Math.max(baseColors[1], baseColors[2]));
    float colorMultiplier = maxColorValue > 0.3f ? 1.8f : 2.5f;
    Vector3f particleColors =
        new Vector3f(
            Math.min(1.0f, baseColors[0] * colorMultiplier),
            Math.min(1.0f, baseColors[1] * colorMultiplier),
            Math.min(1.0f, baseColors[2] * colorMultiplier));

    // Get local player.
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) {
      return;
    }

    // Calculate player position and target position.
    double playerX = player.getX();
    double playerY = player.getY() + player.getEyeHeight();
    double playerZ = player.getZ();
    double targetX = blockPos.getX() + 0.5;
    double targetY = blockPos.getY() + 0.5;
    double targetZ = blockPos.getZ() + 0.5;
    double deltaX = targetX - playerX;
    double deltaY = targetY - playerY;
    double deltaZ = targetZ - playerZ;
    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

    // If player is too close to the target, skip particle spawning
    if (distance < 2.0) {
      return;
    }

    // Calculate the starting position behind the player
    float playerYaw = player.getYRot();
    double behindPlayerOffsetX = -Math.sin(Math.toRadians(playerYaw)) * 1.5;
    double behindPlayerOffsetZ = Math.cos(Math.toRadians(playerYaw)) * 1.5;
    double startX = playerX + behindPlayerOffsetX;
    double startY = playerY - 0.2;
    double startZ = playerZ + behindPlayerOffsetZ;
    deltaX = targetX - startX;
    deltaY = targetY - startY;
    deltaZ = targetZ - startZ;
    distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

    int baseWaypoints = Math.max(8, Math.min(40, (int) (distance * 2.2)));
    int particlesPerWaypoint = distance < 5.0 ? 1 : (distance < 15.0 ? 2 : 3);
    double waypointStep = 1.0 / (baseWaypoints + 1);
    double minSpawnDistance = 1.0;

    // Calculate the number of waypoints and spawn particles
    for (int i = 1; i <= baseWaypoints; i++) {
      double progress = i * waypointStep;
      if (progress * distance < minSpawnDistance) {
        continue;
      }
      double waypointX = startX + (deltaX * progress) + (randomSource.nextGaussian() - 0.5) * 0.06;
      double waypointY = startY + (deltaY * progress) + (randomSource.nextGaussian() - 0.5) * 0.06;
      double waypointZ = startZ + (deltaZ * progress) + (randomSource.nextGaussian() - 0.5) * 0.06;

      // Spawn particles around the waypoint
      for (int j = 0; j < particlesPerWaypoint; j++) {
        float particleSize = (float) Math.min(2.0f, 0.9f + (distance * 0.08f) - (progress * 0.2f));

        clientLevel.addParticle(
            new DustParticleOptions(particleColors, particleSize),
            waypointX + (randomSource.nextGaussian() - 0.5) * 0.04,
            waypointY + (randomSource.nextGaussian() - 0.5) * 0.04,
            waypointZ + (randomSource.nextGaussian() - 0.5) * 0.04,
            (randomSource.nextGaussian() - 0.5) * 0.003,
            0.003 + randomSource.nextGaussian() * 0.001,
            (randomSource.nextGaussian() - 0.5) * 0.003);
      }
    }

    // Spawn additional particles at the target position
    int targetParticleCount = Math.max(3, Math.min(8, (int) (distance * 0.5)));
    for (int i = 0; i < targetParticleCount; i++) {
      clientLevel.addParticle(
          new DustParticleOptions(particleColors, 1.5f),
          targetX + (randomSource.nextGaussian() - 0.5) * 0.2,
          targetY - 0.35 + randomSource.nextFloat() * 0.3,
          targetZ + (randomSource.nextGaussian() - 0.5) * 0.2,
          (randomSource.nextGaussian() - 0.5) * 0.004,
          0.004 + randomSource.nextGaussian() * 0.002,
          (randomSource.nextGaussian() - 0.5) * 0.004);
    }

    // Spawn helper particles if the distance is significant
    if (distance > 12.0) {
      int helperParticleCount = Math.min(5, baseWaypoints / 8);
      for (int i = 0; i < helperParticleCount; i++) {
        double randomProgress = 0.3 + (randomSource.nextDouble() * 0.4);
        if (randomProgress * distance < minSpawnDistance) {
          continue;
        }

        clientLevel.addParticle(
            new DustParticleOptions(particleColors, 0.8f),
            startX + (deltaX * randomProgress) + (randomSource.nextGaussian() - 0.5) * 0.12,
            startY + (deltaY * randomProgress) + (randomSource.nextGaussian() - 0.5) * 0.12,
            startZ + (deltaZ * randomProgress) + (randomSource.nextGaussian() - 0.5) * 0.12,
            (randomSource.nextGaussian() - 0.5) * 0.002,
            0.002,
            (randomSource.nextGaussian() - 0.5) * 0.002);
      }
    }
  }

  private record ParticleProperties(int spawnRate, float brightness, float size) {}
}
