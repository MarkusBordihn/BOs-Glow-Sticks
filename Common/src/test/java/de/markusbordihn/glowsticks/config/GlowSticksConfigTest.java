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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GlowSticksConfigTest {

  private static final String LIFETIME_KEY = "glowStickLifetimeSeconds";
  private static final String LEGACY_KEY = "despawnTicks";

  private int originalLifetimeSeconds;

  @BeforeEach
  void captureLifetime() {
    this.originalLifetimeSeconds = GlowSticksConfig.glowStickLifetimeSeconds;
  }

  @AfterEach
  void restoreLifetime() {
    GlowSticksConfig.glowStickLifetimeSeconds = this.originalLifetimeSeconds;
  }

  @Test
  @DisplayName("A lifetime of 0 disables the despawn timer")
  void zeroLifetimeDisablesTimer() {
    GlowSticksConfig.glowStickLifetimeSeconds = 0;

    assertEquals(0, GlowSticksConfig.getGlowStickStageTicks());
  }

  @Test
  @DisplayName("A negative lifetime is treated like 0")
  void negativeLifetimeDisablesTimer() {
    GlowSticksConfig.glowStickLifetimeSeconds = -5;

    assertEquals(0, GlowSticksConfig.getGlowStickStageTicks());
  }

  @Test
  @DisplayName("The default lifetime is split into 16 even stages")
  void defaultLifetimeIsSplitIntoEvenStages() {
    GlowSticksConfig.glowStickLifetimeSeconds = 2160;

    assertEquals(2700, GlowSticksConfig.getGlowStickStageTicks());
    assertEquals(2160 * 20, GlowSticksConfig.getGlowStickStageTicks() * GlowSticksConfig.AGE_STEPS);
  }

  @Test
  @DisplayName("A very short lifetime still advances at least one tick per stage")
  void shortLifetimeKeepsAtLeastOneTickPerStage() {
    GlowSticksConfig.glowStickLifetimeSeconds = 1;

    assertEquals(1, GlowSticksConfig.getGlowStickStageTicks());
  }

  @Test
  @DisplayName("The maximum lifetime stays within the integer range per stage")
  void maximumLifetimeStaysWithinIntegerRange() {
    GlowSticksConfig.glowStickLifetimeSeconds = 86400;

    assertEquals(108000, GlowSticksConfig.getGlowStickStageTicks());
  }

  @Test
  @DisplayName("A legacy despawnTicks value is converted into a lifetime in seconds")
  void legacyDespawnTicksIsConverted() {
    Properties properties = new Properties();
    properties.setProperty(LEGACY_KEY, "1");

    GlowSticksConfig.migrateLegacyDespawnTicks(properties);

    assertEquals("1088", properties.getProperty(LIFETIME_KEY));
    assertFalse(properties.containsKey(LEGACY_KEY));
  }

  @Test
  @DisplayName("A legacy despawnTicks value of 0 keeps glow sticks permanent")
  void legacyDespawnTicksZeroKeepsGlowSticksPermanent() {
    Properties properties = new Properties();
    properties.setProperty(LEGACY_KEY, "0");

    GlowSticksConfig.migrateLegacyDespawnTicks(properties);

    assertEquals("0", properties.getProperty(LIFETIME_KEY));
    assertFalse(properties.containsKey(LEGACY_KEY));
  }

  @Test
  @DisplayName("A migrated lifetime is capped at one day")
  void migratedLifetimeIsCapped() {
    Properties properties = new Properties();
    properties.setProperty(LEGACY_KEY, "100000");

    GlowSticksConfig.migrateLegacyDespawnTicks(properties);

    assertEquals("86400", properties.getProperty(LIFETIME_KEY));
  }

  @Test
  @DisplayName("An existing lifetime wins over the legacy value")
  void existingLifetimeWinsOverLegacyValue() {
    Properties properties = new Properties();
    properties.setProperty(LEGACY_KEY, "5");
    properties.setProperty(LIFETIME_KEY, "600");

    GlowSticksConfig.migrateLegacyDespawnTicks(properties);

    assertEquals("600", properties.getProperty(LIFETIME_KEY));
    assertFalse(properties.containsKey(LEGACY_KEY));
  }

  @Test
  @DisplayName("A config without the legacy key is left untouched")
  void configWithoutLegacyKeyIsUntouched() {
    Properties properties = new Properties();

    GlowSticksConfig.migrateLegacyDespawnTicks(properties);

    assertTrue(properties.isEmpty());
  }
}
