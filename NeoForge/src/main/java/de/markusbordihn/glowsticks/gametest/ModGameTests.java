package de.markusbordihn.glowsticks.gametest;

import de.markusbordihn.glowsticks.Constants;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public final class ModGameTests {

  private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
    DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, Constants.MOD_ID);
  private static final List<TestEntry> TEST_ENTRIES = new ArrayList<>();
  private static final Identifier STRUCTURE = Identifier.parse("glow_sticks:gametest.3x3x3");
  private static final String DEFAULT_ENVIRONMENT = "glow_sticks:default";
  private static final int MAX_TICKS = 100;
  private static final int EXTENDED_MAX_TICKS = 200;

  static {
    register("mod_registered", SmokeTest::testModRegistered);
    register("glow_stick_break_drops_item", GlowStickTests::testGlowStickBreakDropsItem);
    register("glow_stick_despawn_scheduled", GlowStickTests::testGlowStickDespawnScheduled);
    register(
      "glow_stick_ages_and_vanishes",
      GlowStickTests::testGlowStickAgesAndVanishes,
      TestEnvironments.GLOW_STICK_LIFETIME_VANISH);
    register(
      "glow_stick_never_despawns_with_zero_lifetime",
      GlowStickTests::testGlowStickNeverDespawnsWithZeroLifetime,
      TestEnvironments.GLOW_STICK_LIFETIME_ZERO);
    register(
      "glow_stick_drops_on_despawn_when_enabled",
      GlowStickTests::testGlowStickDropsOnDespawnWhenEnabled,
      TestEnvironments.GLOW_STICK_LIFETIME_DROPS);
    register(
      "creative_glow_stick_drops_creative_item",
      GlowStickTests::testCreativeGlowStickDropsCreativeItem);
    register("light_level_fades_evenly", GlowStickTests::testLightLevelFadesEvenly);
    register("glow_stick_pickup", GlowStickTests::testGlowStickPickup);
    register("glow_stick_placement", GlowStickTests::testGlowStickPlacement);
    register(
      "glow_stick_placed_in_water_is_waterlogged",
      GlowStickTests::testGlowStickPlacedInWaterIsWaterlogged);
    register(
      "glow_stick_lights_up_without_redstone",
      GlowStickTests::testGlowStickLightsUpWithoutRedstone);
    register("glow_stick_light_dims_with_age", GlowStickTests::testGlowStickLightDimsWithAge);
    register(
      "redstone_signal_lights_glow_stick_fully",
      GlowStickTests::testRedstoneSignalLightsGlowStickFully);
    register(
      "unpowered_redstone_turns_glow_stick_off",
      GlowStickTests::testUnpoweredRedstoneTurnsGlowStickOff);
    register(
      "glow_stick_light_follows_every_signal_strength",
      GlowStickTests::testGlowStickLightFollowsEverySignalStrength,
      EXTENDED_MAX_TICKS);
    register(
      "glow_stick_keeps_brightness_when_redstone_is_removed",
      GlowStickTests::testGlowStickKeepsBrightnessWhenRedstoneIsRemoved);
    register(
      "creative_glow_stick_follows_redstone",
      GlowStickTests::testCreativeGlowStickFollowsRedstone);
    register("redstone_chain_without_signal", GlowStickTests::testRedstoneChainWithoutSignal);
    register(
      "redstone_chain_released_when_source_removed",
      GlowStickTests::testRedstoneChainReleasedWhenSourceRemoved);
    register(
      "redstone_freezes_lifetime",
      GlowStickTests::testRedstoneFreezesLifetime,
      TestEnvironments.GLOW_STICK_REDSTONE_FREEZE);
    register(
      "redstone_recharges_glow_stick",
      GlowStickTests::testRedstoneRechargesGlowStick,
      TestEnvironments.GLOW_STICK_REDSTONE_RECHARGE);
    register(
      "chain_limit_stops_propagation",
      GlowStickTests::testChainLimitStopsPropagation,
      TestEnvironments.GLOW_STICK_CHAIN_LIMIT);
    register("glow_stick_still_falls", GlowStickTests::testGlowStickStillFalls);
    register(
      "glow_stick_keeps_age_after_falling", GlowStickTests::testGlowStickKeepsAgeAfterFalling);
    register("creative_recipe_follows_config", GlowStickTests::testCreativeRecipeFollowsConfig);
  }

  private ModGameTests() {
  }

  public static void register(IEventBus modEventBus) {
    if (FMLEnvironment.isProduction()) {
      return;
    }

    TEST_FUNCTIONS.register(modEventBus);
  }

  private static void register(String name, Consumer<GameTestHelper> testFunction) {
    register(name, testFunction, DEFAULT_ENVIRONMENT, MAX_TICKS);
  }

  private static void register(
    String name, Consumer<GameTestHelper> testFunction, String environment) {
    register(name, testFunction, environment, MAX_TICKS);
  }

  private static void register(String name, Consumer<GameTestHelper> testFunction, int maxTicks) {
    register(name, testFunction, DEFAULT_ENVIRONMENT, maxTicks);
  }

  private static void register(
    String name, Consumer<GameTestHelper> testFunction, String environment, int maxTicks) {
    TEST_ENTRIES.add(
      new TestEntry(TEST_FUNCTIONS.register(name, () -> testFunction), environment, maxTicks));
  }

  @SubscribeEvent
  public static void registerGameTests(RegisterGameTestsEvent event) {
    Map<String, Holder<TestEnvironmentDefinition<?>>> environments = new LinkedHashMap<>();
    for (TestEntry testEntry : TEST_ENTRIES) {
      environments.computeIfAbsent(
        testEntry.environment(),
        environment ->
          event.registerEnvironment(
            Identifier.parse(environment), new TestEnvironmentDefinition.AllOf(List.of())));
    }

    for (TestEntry testEntry : TEST_ENTRIES) {
      event.registerTest(
        testEntry.testFunction().getId(),
        new FunctionGameTestInstance(
          testEntry.testFunction().getKey(),
          new TestData<>(
            environments.get(testEntry.environment()),
            STRUCTURE,
            testEntry.maxTicks(),
            0,
            true)));
    }
  }

  private record TestEntry(
    DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> testFunction,
    String environment,
    int maxTicks) {

  }
}
