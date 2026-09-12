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

package de.markusbordihn.glowsticks.gametest;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.block.GlowStickBlock;
import de.markusbordihn.glowsticks.block.glowstick.RedstoneCapable;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GlowStickTests {

  private static final BlockPos SUPPORT_POS = new BlockPos(1, 0, 1);
  private static final BlockPos GLOW_STICK_POS = new BlockPos(1, 1, 1);
  private static final BlockPos UPPER_GLOW_STICK_POS = new BlockPos(1, 2, 1);
  private static final BlockPos NEIGHBOR_SUPPORT_POS = new BlockPos(2, 0, 1);
  private static final BlockPos NEIGHBOR_GLOW_STICK_POS = new BlockPos(2, 1, 1);
  private static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 1);
  private static final BlockPos LAMP_POS = new BlockPos(1, 1, 2);
  private static final BlockPos CHAIN_SOURCE_POS = new BlockPos(2, 1, 0);
  private static final BlockPos CHAIN_START_POS = new BlockPos(1, 1, 0);
  private static final BlockPos CHAIN_MIDDLE_POS = new BlockPos(1, 1, 1);
  private static final BlockPos CHAIN_END_POS = new BlockPos(1, 1, 2);
  private static final int AGE_BEFORE_PICKUP = 5;

  protected GlowStickTests() {}

  public static void testGlowStickBreakDropsItem(GameTestHelper helper) {
    Block glowStickBlock = getGlowStickBlock();
    placeGlowStick(helper, glowStickBlock);

    BlockPos absolutePos = helper.absolutePos(GLOW_STICK_POS);
    List<ItemStack> drops =
        Block.getDrops(helper.getBlockState(GLOW_STICK_POS), helper.getLevel(), absolutePos, null);

    GameTestHelpers.assertTrue(
        helper,
        "Broken glow stick did not drop its item!",
        drops.size() == 1 && drops.get(0).is(glowStickBlock.asItem()));
  }

  public static void testGlowStickDespawnScheduled(GameTestHelper helper) {
    Block glowStickBlock = getGlowStickBlock();
    placeGlowStick(helper, glowStickBlock);

    GameTestHelpers.assertCondition(
        helper,
        "Placed glow stick has no scheduled tick!",
        helper
            .getLevel()
            .getBlockTicks()
            .hasScheduledTick(helper.absolutePos(GLOW_STICK_POS), glowStickBlock));

    helper.runAfterDelay(
        10,
        () ->
            GameTestHelpers.assertTrue(
                helper,
                "A freshly placed glow stick lost a lifetime step right after placement!",
                helper.getBlockState(GLOW_STICK_POS).getValue(GlowStickBlock.AGE) == 0));
  }

  public static void testGlowStickAgesAndVanishes(GameTestHelper helper) {
    int configuredLifetimeSeconds = GlowSticksConfig.glowStickLifetimeSeconds;
    GlowSticksConfig.glowStickLifetimeSeconds = 1;
    Block glowStickBlock = getGlowStickBlock();
    placeGlowStick(helper, glowStickBlock);

    helper.runAfterDelay(
        60,
        () -> {
          GlowSticksConfig.glowStickLifetimeSeconds = configuredLifetimeSeconds;
          GameTestHelpers.assertCondition(
              helper,
              "Glow stick did not fade away!",
              helper.getBlockState(GLOW_STICK_POS).isAir());
          GameTestHelpers.assertTrue(
              helper,
              "Faded glow stick dropped its item although drops are disabled!",
              countDroppedItems(helper, glowStickBlock.asItem()) == 0);
        });
  }

  public static void testGlowStickNeverDespawnsWithZeroLifetime(GameTestHelper helper) {
    int configuredLifetimeSeconds = GlowSticksConfig.glowStickLifetimeSeconds;
    GlowSticksConfig.glowStickLifetimeSeconds = 0;
    Block glowStickBlock = getGlowStickBlock();
    placeGlowStick(helper, glowStickBlock);

    helper.runAfterDelay(
        20,
        () -> {
          GlowSticksConfig.glowStickLifetimeSeconds = configuredLifetimeSeconds;
          BlockState blockState = helper.getBlockState(GLOW_STICK_POS);
          GameTestHelpers.assertCondition(
              helper,
              "Glow stick aged although its lifetime is disabled!",
              blockState.is(glowStickBlock) && blockState.getValue(GlowStickBlock.AGE) == 0);
          GameTestHelpers.assertTrue(
              helper,
              "Glow stick still has a despawn tick scheduled!",
              !helper
                  .getLevel()
                  .getBlockTicks()
                  .hasScheduledTick(helper.absolutePos(GLOW_STICK_POS), glowStickBlock));
        });
  }

  public static void testGlowStickDropsOnDespawnWhenEnabled(GameTestHelper helper) {
    int configuredLifetimeSeconds = GlowSticksConfig.glowStickLifetimeSeconds;
    boolean configuredDropsOnDespawn = GlowSticksConfig.glowStickDropsOnDespawn;
    GlowSticksConfig.glowStickLifetimeSeconds = 1;
    GlowSticksConfig.glowStickDropsOnDespawn = true;
    Block glowStickBlock = getGlowStickBlock();
    placeGlowStick(helper, glowStickBlock);

    helper.runAfterDelay(
        60,
        () -> {
          GlowSticksConfig.glowStickLifetimeSeconds = configuredLifetimeSeconds;
          GlowSticksConfig.glowStickDropsOnDespawn = configuredDropsOnDespawn;
          GameTestHelpers.assertCondition(
              helper,
              "Glow stick did not fade away!",
              helper.getBlockState(GLOW_STICK_POS).isAir());
          GameTestHelpers.assertTrue(
              helper,
              "Faded glow stick did not drop its item although drops are enabled!",
              countDroppedItems(helper, glowStickBlock.asItem()) > 0);
        });
  }

  public static void testCreativeGlowStickDropsCreativeItem(GameTestHelper helper) {
    Block creativeGlowStickBlock = getBlock("creative_glow_stick_white");
    helper.setBlock(SUPPORT_POS, Blocks.STONE);
    helper.setBlock(GLOW_STICK_POS, creativeGlowStickBlock);

    List<ItemStack> drops =
        Block.getDrops(
            helper.getBlockState(GLOW_STICK_POS),
            helper.getLevel(),
            helper.absolutePos(GLOW_STICK_POS),
            null);

    GameTestHelpers.assertTrue(
        helper,
        "Broken creative glow stick did not drop the creative item!",
        drops.size() == 1 && drops.get(0).is(creativeGlowStickBlock.asItem()));
  }

  public static void testLightLevelFadesEvenly(GameTestHelper helper) {
    BlockState freshState = getGlowStickBlock().defaultBlockState();

    GameTestHelpers.assertCondition(
        helper,
        "A fresh glow stick does not emit full light!",
        freshState.getLightEmission() == 15);
    GameTestHelpers.assertCondition(
        helper,
        "The light level does not fade in even steps!",
        freshState.setValue(GlowStickBlock.AGE, 7).getLightEmission() == 8
            && freshState.setValue(GlowStickBlock.AGE, 14).getLightEmission() == 1);
    GameTestHelpers.assertCondition(
        helper,
        "A fully faded glow stick is not dimmed to the lowest light level!",
        freshState.setValue(GlowStickBlock.AGE, 15).getLightEmission() == 1);
    GameTestHelpers.assertTrue(
        helper,
        "A controlled glow stick does not follow its redstone power!",
        freshState
                .setValue(
                    GlowStickBlock.REDSTONE_CONTROL, RedstoneCapable.encodeControlState(true, 9))
                .getLightEmission()
            == 9);
  }

  public static void testRedstoneChainReleasedWhenSourceRemoved(GameTestHelper helper) {
    Block glowStickBlock = getGlowStickBlock();
    placeSupportedGlowStick(helper, CHAIN_START_POS, glowStickBlock.defaultBlockState());
    placeSupportedGlowStick(helper, CHAIN_MIDDLE_POS, glowStickBlock.defaultBlockState());
    helper.setBlock(CHAIN_SOURCE_POS, Blocks.REDSTONE_BLOCK);

    GameTestHelpers.assertCondition(
        helper,
        "The glow stick chain was not controlled by the redstone block!",
        RedstoneCapable.isControlled(helper.getBlockState(CHAIN_MIDDLE_POS)));

    helper.setBlock(CHAIN_SOURCE_POS, Blocks.AIR);

    BlockState startState = helper.getBlockState(CHAIN_START_POS);
    BlockState middleState = helper.getBlockState(CHAIN_MIDDLE_POS);
    GameTestHelpers.assertTrue(
        helper,
        "The glow stick chain stayed controlled after the redstone source was removed!",
        !RedstoneCapable.isControlled(startState) && !RedstoneCapable.isControlled(middleState));
  }

  public static void testRedstoneFreezesLifetime(GameTestHelper helper) {
    int configuredLifetimeSeconds = GlowSticksConfig.glowStickLifetimeSeconds;
    GlowSticksConfig.glowStickLifetimeSeconds = 1;
    Block glowStickBlock = getGlowStickBlock();
    placeSupportedGlowStick(
        helper,
        CHAIN_START_POS,
        glowStickBlock.defaultBlockState().setValue(GlowStickBlock.AGE, AGE_BEFORE_PICKUP));
    helper.setBlock(CHAIN_SOURCE_POS, Blocks.LEVER);

    helper.runAfterDelay(
        60,
        () -> {
          GlowSticksConfig.glowStickLifetimeSeconds = configuredLifetimeSeconds;
          BlockState blockState = helper.getBlockState(CHAIN_START_POS);
          GameTestHelpers.assertTrue(
              helper,
              "A wired but unpowered glow stick kept aging!",
              RedstoneCapable.isControlled(blockState)
                  && !RedstoneCapable.isPowered(blockState)
                  && blockState.getValue(GlowStickBlock.AGE) == AGE_BEFORE_PICKUP);
        });
  }

  public static void testRedstoneRechargesGlowStick(GameTestHelper helper) {
    int configuredLifetimeSeconds = GlowSticksConfig.glowStickLifetimeSeconds;
    GlowSticksConfig.glowStickLifetimeSeconds = 1;
    Block glowStickBlock = getGlowStickBlock();
    placeSupportedGlowStick(
        helper,
        CHAIN_START_POS,
        glowStickBlock.defaultBlockState().setValue(GlowStickBlock.AGE, 10));
    helper.setBlock(CHAIN_SOURCE_POS, Blocks.REDSTONE_BLOCK);

    helper.runAfterDelay(
        60,
        () -> {
          GlowSticksConfig.glowStickLifetimeSeconds = configuredLifetimeSeconds;
          GameTestHelpers.assertTrue(
              helper,
              "A powered glow stick did not recharge back to full brightness!",
              helper.getBlockState(CHAIN_START_POS).getValue(GlowStickBlock.AGE) == 0);
        });
  }

  public static void testChainLimitStopsPropagation(GameTestHelper helper) {
    int configuredChainLimit = GlowSticksConfig.glowStickChainLimit;
    GlowSticksConfig.glowStickChainLimit = 2;
    Block glowStickBlock = getGlowStickBlock();
    placeSupportedGlowStick(helper, CHAIN_START_POS, glowStickBlock.defaultBlockState());
    placeSupportedGlowStick(helper, CHAIN_MIDDLE_POS, glowStickBlock.defaultBlockState());
    placeSupportedGlowStick(helper, CHAIN_END_POS, glowStickBlock.defaultBlockState());
    helper.setBlock(CHAIN_SOURCE_POS, Blocks.REDSTONE_BLOCK);
    GlowSticksConfig.glowStickChainLimit = configuredChainLimit;

    GameTestHelpers.assertTrue(
        helper,
        "The redstone signal was not limited to the configured chain size!",
        RedstoneCapable.isControlled(helper.getBlockState(CHAIN_START_POS))
            && RedstoneCapable.isControlled(helper.getBlockState(CHAIN_MIDDLE_POS))
            && !RedstoneCapable.isControlled(helper.getBlockState(CHAIN_END_POS)));
  }

  public static void testGlowStickPlacement(GameTestHelper helper) {
    Block glowStickBlock = getGlowStickBlock();
    helper.setBlock(SUPPORT_POS, Blocks.STONE);

    ItemStack itemStack = new ItemStack(glowStickBlock.asItem(), 2);
    GlowStickItem.setAge(itemStack, AGE_BEFORE_PICKUP);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
    useOnBlock(helper, player, SUPPORT_POS);

    BlockState placedState = helper.getBlockState(GLOW_STICK_POS);
    GameTestHelpers.assertTrue(
        helper,
        "Using a glow stick on a block did not place it with its remaining lifetime!",
        placedState.is(glowStickBlock)
            && placedState.getValue(GlowStickBlock.AGE) == AGE_BEFORE_PICKUP
            && itemStack.getCount() == 1);
  }

  public static void testGlowStickPlacedInWaterIsWaterlogged(GameTestHelper helper) {
    Block glowStickBlock = getGlowStickBlock();
    helper.setBlock(SUPPORT_POS, Blocks.STONE);
    helper.setBlock(GLOW_STICK_POS, Blocks.WATER);

    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(glowStickBlock.asItem()));
    useOnBlock(helper, player, SUPPORT_POS);

    BlockState placedState = helper.getBlockState(GLOW_STICK_POS);
    GameTestHelpers.assertTrue(
        helper,
        "A glow stick placed in water was not waterlogged!",
        placedState.is(glowStickBlock)
            && Boolean.TRUE.equals(placedState.getValue(GlowStickBlock.WATERLOGGED))
            && placedState.getFluidState().getType() == Fluids.WATER);
  }

  public static void testGlowStickKeepsAgeAfterFalling(GameTestHelper helper) {
    Block glowStickBlock = getGlowStickBlock();
    helper.setBlock(SUPPORT_POS, Blocks.STONE);
    helper.setBlock(GLOW_STICK_POS, Blocks.STONE);
    helper.setBlock(
        UPPER_GLOW_STICK_POS,
        glowStickBlock.defaultBlockState().setValue(GlowStickBlock.AGE, AGE_BEFORE_PICKUP));
    helper.setBlock(GLOW_STICK_POS, Blocks.AIR);

    helper.runAfterDelay(
        20,
        () -> {
          BlockState landedState = helper.getBlockState(GLOW_STICK_POS);
          GameTestHelpers.assertTrue(
              helper,
              "A fallen glow stick did not keep its remaining lifetime!",
              landedState.is(glowStickBlock)
                  && landedState.getValue(GlowStickBlock.AGE) == AGE_BEFORE_PICKUP);
        });
  }

  public static void testCreativeRecipeFollowsConfig(GameTestHelper helper) {
    boolean recipeLoaded =
        helper
            .getLevel()
            .getServer()
            .getRecipeManager()
            .byKey(
                ResourceKey.create(
                    Registries.RECIPE,
                    Identifier.fromNamespaceAndPath(Constants.MOD_ID, "creative_glow_stick_white")))
            .isPresent();

    GameTestHelpers.assertTrue(
        helper,
        "The creative glow stick recipe does not follow enableCreativeGlowStickRecipe!",
        recipeLoaded == GlowSticksConfig.enableCreativeGlowStickRecipe);
  }

  public static void testGlowStickPickup(GameTestHelper helper) {
    Block glowStickBlock = getGlowStickBlock();
    helper.setBlock(SUPPORT_POS, Blocks.STONE);
    helper.setBlock(
        GLOW_STICK_POS,
        glowStickBlock.defaultBlockState().setValue(GlowStickBlock.AGE, AGE_BEFORE_PICKUP));

    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setShiftKeyDown(true);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(glowStickBlock.asItem()));

    BlockPos absolutePos = helper.absolutePos(GLOW_STICK_POS);
    glowStickBlock
        .asItem()
        .useOn(
            new UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                    Vec3.atCenterOf(absolutePos), Direction.UP, absolutePos, false)));

    GameTestHelpers.assertTrue(
        helper,
        "Glow stick was not picked up with its remaining lifetime!",
        helper.getBlockState(GLOW_STICK_POS).isAir()
            && findPickedUpAge(player) == AGE_BEFORE_PICKUP);
  }

  public static void testRedstoneChainWithoutSignal(GameTestHelper helper) {
    Block glowStickBlock = getGlowStickBlock();
    helper.setBlock(SUPPORT_POS, Blocks.STONE);
    helper.setBlock(NEIGHBOR_SUPPORT_POS, Blocks.STONE);
    helper.setBlock(GLOW_STICK_POS, glowStickBlock);
    helper.setBlock(NEIGHBOR_GLOW_STICK_POS, glowStickBlock);
    helper.setBlock(LAMP_POS, Blocks.REDSTONE_LAMP);
    helper.setBlock(REDSTONE_POS, Blocks.REDSTONE_BLOCK);

    BlockState neighborState = helper.getBlockState(NEIGHBOR_GLOW_STICK_POS);
    GameTestHelpers.assertTrue(
        helper,
        "Powered glow stick chain did not light up or leaked a redstone signal!",
        RedstoneCapable.isPowered(neighborState)
            && Boolean.FALSE.equals(
                helper.getBlockState(LAMP_POS).getValue(BlockStateProperties.LIT)));
  }

  public static void testGlowStickStillFalls(GameTestHelper helper) {
    placeGlowStick(helper, getGlowStickBlock());
    helper.setBlock(SUPPORT_POS, Blocks.AIR);

    helper.runAfterDelay(
        5,
        () ->
            GameTestHelpers.assertTrue(
                helper,
                "Glow stick did not fall down!",
                helper.getBlockState(GLOW_STICK_POS).isAir()));
  }

  private static void placeGlowStick(GameTestHelper helper, Block glowStickBlock) {
    helper.setBlock(SUPPORT_POS, Blocks.STONE);
    helper.setBlock(GLOW_STICK_POS, glowStickBlock);
  }

  private static void placeSupportedGlowStick(
      GameTestHelper helper, BlockPos blockPos, BlockState blockState) {
    helper.setBlock(blockPos.below(), Blocks.STONE);
    helper.setBlock(blockPos, blockState);
  }

  private static void useOnBlock(GameTestHelper helper, Player player, BlockPos blockPos) {
    BlockPos absolutePos = helper.absolutePos(blockPos);
    player
        .getMainHandItem()
        .getItem()
        .useOn(
            new UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                    Vec3.atCenterOf(absolutePos), Direction.UP, absolutePos, false)));
  }

  private static int countDroppedItems(GameTestHelper helper, Item item) {
    AABB searchArea = new AABB(helper.absolutePos(GLOW_STICK_POS)).inflate(3.0);
    int count = 0;
    for (ItemEntity itemEntity :
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, searchArea)) {
      if (itemEntity.getItem().is(item)) {
        count += itemEntity.getItem().getCount();
      }
    }

    return count;
  }

  private static Block getBlock(String name) {
    return BuiltInRegistries.BLOCK.getValue(
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
  }

  private static Block getGlowStickBlock() {
    return getBlock("glow_stick_white");
  }

  private static int findPickedUpAge(Player player) {
    for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
      int age = GlowStickItem.getAge(player.getInventory().getItem(slot));
      if (age > 0) {
        return age;
      }
    }

    return 0;
  }
}
