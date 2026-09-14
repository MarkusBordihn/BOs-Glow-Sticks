package de.markusbordihn.glowsticks.gametest;

import de.markusbordihn.glowsticks.Constants;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;

public final class ModGameTests {

  private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
    DeferredRegister.create(Registries.TEST_FUNCTION, Constants.MOD_ID);

  static {
    TEST_FUNCTIONS.register("mod_registered", () -> SmokeTest::testModRegistered);
    TEST_FUNCTIONS.register(
      "glow_stick_break_drops_item", () -> GlowStickTests::testGlowStickBreakDropsItem);
    TEST_FUNCTIONS.register(
      "glow_stick_despawn_scheduled", () -> GlowStickTests::testGlowStickDespawnScheduled);
    TEST_FUNCTIONS.register(
      "glow_stick_ages_and_vanishes", () -> GlowStickTests::testGlowStickAgesAndVanishes);
    TEST_FUNCTIONS.register(
      "glow_stick_never_despawns_with_zero_lifetime",
      () -> GlowStickTests::testGlowStickNeverDespawnsWithZeroLifetime);
    TEST_FUNCTIONS.register(
      "glow_stick_drops_on_despawn_when_enabled",
      () -> GlowStickTests::testGlowStickDropsOnDespawnWhenEnabled);
    TEST_FUNCTIONS.register(
      "creative_glow_stick_drops_creative_item",
      () -> GlowStickTests::testCreativeGlowStickDropsCreativeItem);
    TEST_FUNCTIONS.register(
      "light_level_fades_evenly", () -> GlowStickTests::testLightLevelFadesEvenly);
    TEST_FUNCTIONS.register("glow_stick_pickup", () -> GlowStickTests::testGlowStickPickup);
    TEST_FUNCTIONS.register("glow_stick_placement", () -> GlowStickTests::testGlowStickPlacement);
    TEST_FUNCTIONS.register(
      "glow_stick_placed_in_water_is_waterlogged",
      () -> GlowStickTests::testGlowStickPlacedInWaterIsWaterlogged);
    TEST_FUNCTIONS.register(
      "glow_stick_lights_up_without_redstone",
      () -> GlowStickTests::testGlowStickLightsUpWithoutRedstone);
    TEST_FUNCTIONS.register(
      "glow_stick_light_dims_with_age", () -> GlowStickTests::testGlowStickLightDimsWithAge);
    TEST_FUNCTIONS.register(
      "redstone_signal_lights_glow_stick_fully",
      () -> GlowStickTests::testRedstoneSignalLightsGlowStickFully);
    TEST_FUNCTIONS.register(
      "unpowered_redstone_turns_glow_stick_off",
      () -> GlowStickTests::testUnpoweredRedstoneTurnsGlowStickOff);
    TEST_FUNCTIONS.register(
      "glow_stick_light_follows_every_signal_strength",
      () -> GlowStickTests::testGlowStickLightFollowsEverySignalStrength);
    TEST_FUNCTIONS.register(
      "glow_stick_keeps_brightness_when_redstone_is_removed",
      () -> GlowStickTests::testGlowStickKeepsBrightnessWhenRedstoneIsRemoved);
    TEST_FUNCTIONS.register(
      "creative_glow_stick_follows_redstone",
      () -> GlowStickTests::testCreativeGlowStickFollowsRedstone);
    TEST_FUNCTIONS.register(
      "redstone_chain_without_signal", () -> GlowStickTests::testRedstoneChainWithoutSignal);
    TEST_FUNCTIONS.register(
      "redstone_chain_released_when_source_removed",
      () -> GlowStickTests::testRedstoneChainReleasedWhenSourceRemoved);
    TEST_FUNCTIONS.register(
      "redstone_freezes_lifetime", () -> GlowStickTests::testRedstoneFreezesLifetime);
    TEST_FUNCTIONS.register(
      "redstone_recharges_glow_stick", () -> GlowStickTests::testRedstoneRechargesGlowStick);
    TEST_FUNCTIONS.register(
      "chain_limit_stops_propagation", () -> GlowStickTests::testChainLimitStopsPropagation);
    TEST_FUNCTIONS.register("glow_stick_still_falls",
      () -> GlowStickTests::testGlowStickStillFalls);
    TEST_FUNCTIONS.register(
      "glow_stick_keeps_age_after_falling",
      () -> GlowStickTests::testGlowStickKeepsAgeAfterFalling);
    TEST_FUNCTIONS.register(
      "creative_recipe_follows_config", () -> GlowStickTests::testCreativeRecipeFollowsConfig);
  }

  private ModGameTests() {
  }

  public static void register(BusGroup modBusGroup) {
    if (FMLLoader.isProduction()) {
      return;
    }

    TEST_FUNCTIONS.register(modBusGroup);
  }
}
