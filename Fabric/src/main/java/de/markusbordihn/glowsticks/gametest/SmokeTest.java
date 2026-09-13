package de.markusbordihn.glowsticks.gametest;

import de.markusbordihn.glowsticks.Constants;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTestHelper;

@SuppressWarnings("unused")
public class SmokeTest {

  private static final String STRUCTURE = "glow_sticks:gametest.3x3x3";
  private static final int MAX_TICKS = 100;

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testModRegistered(GameTestHelper helper) {
    GameTestHelpers.assertTrue(
      helper,
      "Mod " + Constants.MOD_ID + " is not available!",
      FabricLoader.getInstance().isModLoaded(Constants.MOD_ID));
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testGlowStickBreakDropsItem(GameTestHelper helper) {
    GlowStickTests.testGlowStickBreakDropsItem(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testGlowStickDespawnScheduled(GameTestHelper helper) {
    GlowStickTests.testGlowStickDespawnScheduled(helper);
  }

  @GameTest(
    structure = STRUCTURE,
    maxTicks = MAX_TICKS,
    environment = TestEnvironments.GLOW_STICK_LIFETIME_VANISH)
  public void testGlowStickAgesAndVanishes(GameTestHelper helper) {
    GlowStickTests.testGlowStickAgesAndVanishes(helper);
  }

  @GameTest(
    structure = STRUCTURE,
    maxTicks = MAX_TICKS,
    environment = TestEnvironments.GLOW_STICK_LIFETIME_ZERO)
  public void testGlowStickNeverDespawnsWithZeroLifetime(GameTestHelper helper) {
    GlowStickTests.testGlowStickNeverDespawnsWithZeroLifetime(helper);
  }

  @GameTest(
    structure = STRUCTURE,
    maxTicks = MAX_TICKS,
    environment = TestEnvironments.GLOW_STICK_LIFETIME_DROPS)
  public void testGlowStickDropsOnDespawnWhenEnabled(GameTestHelper helper) {
    GlowStickTests.testGlowStickDropsOnDespawnWhenEnabled(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testCreativeGlowStickDropsCreativeItem(GameTestHelper helper) {
    GlowStickTests.testCreativeGlowStickDropsCreativeItem(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testLightLevelFadesEvenly(GameTestHelper helper) {
    GlowStickTests.testLightLevelFadesEvenly(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testGlowStickPickup(GameTestHelper helper) {
    GlowStickTests.testGlowStickPickup(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testGlowStickPlacement(GameTestHelper helper) {
    GlowStickTests.testGlowStickPlacement(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testGlowStickPlacedInWaterIsWaterlogged(GameTestHelper helper) {
    GlowStickTests.testGlowStickPlacedInWaterIsWaterlogged(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testRedstoneChainWithoutSignal(GameTestHelper helper) {
    GlowStickTests.testRedstoneChainWithoutSignal(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testRedstoneChainReleasedWhenSourceRemoved(GameTestHelper helper) {
    GlowStickTests.testRedstoneChainReleasedWhenSourceRemoved(helper);
  }

  @GameTest(
    structure = STRUCTURE,
    maxTicks = MAX_TICKS,
    environment = TestEnvironments.GLOW_STICK_REDSTONE_FREEZE)
  public void testRedstoneFreezesLifetime(GameTestHelper helper) {
    GlowStickTests.testRedstoneFreezesLifetime(helper);
  }

  @GameTest(
    structure = STRUCTURE,
    maxTicks = MAX_TICKS,
    environment = TestEnvironments.GLOW_STICK_REDSTONE_RECHARGE)
  public void testRedstoneRechargesGlowStick(GameTestHelper helper) {
    GlowStickTests.testRedstoneRechargesGlowStick(helper);
  }

  @GameTest(
    structure = STRUCTURE,
    maxTicks = MAX_TICKS,
    environment = TestEnvironments.GLOW_STICK_CHAIN_LIMIT)
  public void testChainLimitStopsPropagation(GameTestHelper helper) {
    GlowStickTests.testChainLimitStopsPropagation(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testGlowStickStillFalls(GameTestHelper helper) {
    GlowStickTests.testGlowStickStillFalls(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testGlowStickKeepsAgeAfterFalling(GameTestHelper helper) {
    GlowStickTests.testGlowStickKeepsAgeAfterFalling(helper);
  }

  @GameTest(structure = STRUCTURE, maxTicks = MAX_TICKS)
  public void testCreativeRecipeFollowsConfig(GameTestHelper helper) {
    GlowStickTests.testCreativeRecipeFollowsConfig(helper);
  }
}
