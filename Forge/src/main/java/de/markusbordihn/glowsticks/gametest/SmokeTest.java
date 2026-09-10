package de.markusbordihn.glowsticks.gametest;

import de.markusbordihn.glowsticks.Constants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@SuppressWarnings("unused")
@PrefixGameTestTemplate(value = false)
@GameTestHolder(Constants.MOD_ID)
public class SmokeTest {

  @GameTest(template = "gametest.3x3x3")
  public void testModRegistered(GameTestHelper helper) {
    GameTestHelpers.assertTrue(
        helper,
        "Mod " + Constants.MOD_ID + " is not available!",
        ModList.get().isLoaded(Constants.MOD_ID));
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testGlowStickBreakDropsItem(GameTestHelper helper) {
    GlowStickTests.testGlowStickBreakDropsItem(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testGlowStickDespawnScheduled(GameTestHelper helper) {
    GlowStickTests.testGlowStickDespawnScheduled(helper);
  }

  @GameTest(template = "gametest.3x3x3", batch = "glowStickLifetimeVanish")
  public void testGlowStickAgesAndVanishes(GameTestHelper helper) {
    GlowStickTests.testGlowStickAgesAndVanishes(helper);
  }

  @GameTest(template = "gametest.3x3x3", batch = "glowStickLifetimeZero")
  public void testGlowStickNeverDespawnsWithZeroLifetime(GameTestHelper helper) {
    GlowStickTests.testGlowStickNeverDespawnsWithZeroLifetime(helper);
  }

  @GameTest(template = "gametest.3x3x3", batch = "glowStickLifetimeDrops")
  public void testGlowStickDropsOnDespawnWhenEnabled(GameTestHelper helper) {
    GlowStickTests.testGlowStickDropsOnDespawnWhenEnabled(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testCreativeGlowStickDropsCreativeItem(GameTestHelper helper) {
    GlowStickTests.testCreativeGlowStickDropsCreativeItem(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testLightLevelFadesEvenly(GameTestHelper helper) {
    GlowStickTests.testLightLevelFadesEvenly(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testGlowStickPickup(GameTestHelper helper) {
    GlowStickTests.testGlowStickPickup(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testGlowStickPlacement(GameTestHelper helper) {
    GlowStickTests.testGlowStickPlacement(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testGlowStickPlacedInWaterIsWaterlogged(GameTestHelper helper) {
    GlowStickTests.testGlowStickPlacedInWaterIsWaterlogged(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testRedstoneChainWithoutSignal(GameTestHelper helper) {
    GlowStickTests.testRedstoneChainWithoutSignal(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testRedstoneChainReleasedWhenSourceRemoved(GameTestHelper helper) {
    GlowStickTests.testRedstoneChainReleasedWhenSourceRemoved(helper);
  }

  @GameTest(template = "gametest.3x3x3", batch = "glowStickRedstoneFreeze")
  public void testRedstoneFreezesLifetime(GameTestHelper helper) {
    GlowStickTests.testRedstoneFreezesLifetime(helper);
  }

  @GameTest(template = "gametest.3x3x3", batch = "glowStickRedstoneRecharge")
  public void testRedstoneRechargesGlowStick(GameTestHelper helper) {
    GlowStickTests.testRedstoneRechargesGlowStick(helper);
  }

  @GameTest(template = "gametest.3x3x3", batch = "glowStickChainLimit")
  public void testChainLimitStopsPropagation(GameTestHelper helper) {
    GlowStickTests.testChainLimitStopsPropagation(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testGlowStickStillFalls(GameTestHelper helper) {
    GlowStickTests.testGlowStickStillFalls(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testGlowStickKeepsAgeAfterFalling(GameTestHelper helper) {
    GlowStickTests.testGlowStickKeepsAgeAfterFalling(helper);
  }

  @GameTest(template = "gametest.3x3x3")
  public void testCreativeRecipeFollowsConfig(GameTestHelper helper) {
    GlowStickTests.testCreativeRecipeFollowsConfig(helper);
  }
}
