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

package de.markusbordihn.glowsticks.config;

import java.io.File;
import java.util.Properties;

public class GlowSticksConfig extends Config {
  public static final String CONFIG_FILE_NAME = "glow_sticks.cfg";
  public static final String CONFIG_FILE_HEADER =
"""
  Glow Sticks Configuration

  This file contains the configuration for the Glow Sticks mod.
  You can enable or disable features and adjust settings as needed.

  Glow Stick Lifetime:
  - glowStickLifetimeSeconds: How long a placed glow stick lasts before it despawns, in seconds.
    The glow stick fades over 16 evenly spaced steps and vanishes at the end. Default: 2160 (36 min)
    A value of 0 means glow sticks never fade and never despawn.
  - glowStickDropsOnDespawn: Whether a glow stick drops its item when its lifetime runs out.
  - glowStickRedstoneFreezesLifetime: Whether glow sticks next to redstone stop aging, even while
    unpowered. Useful for permanent builds, disable this for a stricter survival balance.
  - glowStickRedstoneRecharges: Whether powered glow sticks slowly recharge back to full brightness.
  - glowStickChainLimit: How many connected glow sticks a single redstone signal can control.
    A value of 0 disables chains, so only glow sticks touching redstone directly are controlled.

  Glow Stick Handling:
  - allowGlowStickBlockPlacement: Whether glow sticks can be placed as a block by right-clicking.
  - allowGlowStickPickup: Whether placed glow sticks can be picked up again by sneak right-clicking.
  - enableCreativeGlowStickRecipe: Whether permanent glow sticks can be crafted in survival.
    Requires a world restart or /reload to take effect.

  Particle Effects (Client Side Only):
  - spawnRandomParticles: Enable or disable random glow particles from placed glow sticks.
  - randomParticleSpawnRate: How often random glow particles spawn (higher = less frequent). Default: 25
  - spawnWaypointParticles: Enable waypoint particles when sneaking and holding a glow stick to show path to nearest matching glow stick.
  - waypointSearchRadius: Horizontal radius in blocks to search for nearest glow stick. Default: 32
  - waypointVerticalSearchRadius: Vertical radius in blocks to search for nearest glow stick. Default: 16

""";

  public static final int AGE_STEPS = 16;
  private static final String LEGACY_DESPAWN_TICKS_KEY = "despawnTicks";
  private static final int LEGACY_RANDOM_TICK_SECONDS = 68;
  private static final int MAX_LIFETIME_SECONDS = 86400;

  public static int glowStickLifetimeSeconds = 2160;
  public static boolean glowStickDropsOnDespawn = false;
  public static boolean glowStickRedstoneFreezesLifetime = true;
  public static boolean glowStickRedstoneRecharges = true;
  public static int glowStickChainLimit = 64;
  public static boolean allowGlowStickBlockPlacement = true;
  public static boolean allowGlowStickPickup = true;
  public static boolean enableCreativeGlowStickRecipe = false;
  public static boolean spawnRandomParticles = true;
  public static int randomParticleSpawnRate = 25;
  public static boolean spawnWaypointParticles = true;
  public static int waypointSearchRadius = 32;
  public static int waypointVerticalSearchRadius = 16;

  public static void registerConfig() {
    registerConfigFile(CONFIG_FILE_NAME, CONFIG_FILE_HEADER);
    parseConfigFile();
  }

  public static int getGlowStickStageTicks() {
    if (glowStickLifetimeSeconds <= 0) {
      return 0;
    }

    return Math.max(1, Math.round(glowStickLifetimeSeconds * 20.0f / AGE_STEPS));
  }

  static void migrateLegacyDespawnTicks(final Properties properties) {
    if (!properties.containsKey(LEGACY_DESPAWN_TICKS_KEY)) {
      return;
    }

    if (!properties.containsKey("glowStickLifetimeSeconds")) {
      int legacyDespawnTicks = parseConfigMinValue(properties, LEGACY_DESPAWN_TICKS_KEY, 0, 0);
      int migratedLifetimeSeconds =
          legacyDespawnTicks <= 0
              ? 0
              : Math.min(
                  MAX_LIFETIME_SECONDS,
                  legacyDespawnTicks * AGE_STEPS * LEGACY_RANDOM_TICK_SECONDS);
      properties.setProperty("glowStickLifetimeSeconds", Integer.toString(migratedLifetimeSeconds));
      log.info(
          "Migrated legacy despawnTicks {} to glowStickLifetimeSeconds {}",
          legacyDespawnTicks,
          migratedLifetimeSeconds);
    }

    properties.remove(LEGACY_DESPAWN_TICKS_KEY);
  }

  public static void parseConfigFile() {
    File configFile = getConfigFile(CONFIG_FILE_NAME);
    Properties properties = readConfigFile(configFile);
    Properties unmodifiedProperties = (Properties) properties.clone();

    migrateLegacyDespawnTicks(properties);

    glowStickLifetimeSeconds =
        parseConfigRangedValue(
            properties,
            "glowStickLifetimeSeconds",
            glowStickLifetimeSeconds,
            0,
            MAX_LIFETIME_SECONDS);
    glowStickDropsOnDespawn =
        parseConfigValue(properties, "glowStickDropsOnDespawn", glowStickDropsOnDespawn);
    glowStickRedstoneFreezesLifetime =
        parseConfigValue(
            properties, "glowStickRedstoneFreezesLifetime", glowStickRedstoneFreezesLifetime);
    glowStickRedstoneRecharges =
        parseConfigValue(properties, "glowStickRedstoneRecharges", glowStickRedstoneRecharges);
    glowStickChainLimit =
        parseConfigMinValue(properties, "glowStickChainLimit", glowStickChainLimit, 0);
    allowGlowStickBlockPlacement =
        parseConfigValue(properties, "allowGlowStickBlockPlacement", allowGlowStickBlockPlacement);
    allowGlowStickPickup =
        parseConfigValue(properties, "allowGlowStickPickup", allowGlowStickPickup);
    enableCreativeGlowStickRecipe =
        parseConfigValue(
            properties, "enableCreativeGlowStickRecipe", enableCreativeGlowStickRecipe);
    spawnRandomParticles =
        parseConfigValue(properties, "spawnRandomParticles", spawnRandomParticles);
    randomParticleSpawnRate =
        parseConfigMinValue(properties, "randomParticleSpawnRate", randomParticleSpawnRate, 1);
    spawnWaypointParticles =
        parseConfigValue(properties, "spawnWaypointParticles", spawnWaypointParticles);
    waypointSearchRadius =
        parseConfigMinValue(properties, "waypointSearchRadius", waypointSearchRadius, 0);
    waypointVerticalSearchRadius =
        parseConfigMinValue(
            properties, "waypointVerticalSearchRadius", waypointVerticalSearchRadius, 0);

    updateConfigFileIfChanged(configFile, CONFIG_FILE_HEADER, properties, unmodifiedProperties);
  }
}
