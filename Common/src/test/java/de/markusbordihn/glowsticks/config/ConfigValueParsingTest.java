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

import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfigValueParsingTest {

  @Test
  @DisplayName("A missing integer key is written back with its default value")
  void missingIntegerKeyIsWrittenBack() {
    Properties properties = new Properties();

    assertEquals(25, Config.parseConfigValue(properties, "randomParticleSpawnRate", 25));
    assertEquals("25", properties.getProperty("randomParticleSpawnRate"));
  }

  @Test
  @DisplayName("A missing boolean key is written back with its default value")
  void missingBooleanKeyIsWrittenBack() {
    Properties properties = new Properties();

    assertFalse(Config.parseConfigValue(properties, "enableCreativeGlowStickRecipe", false));
    assertEquals("false", properties.getProperty("enableCreativeGlowStickRecipe"));
  }

  @Test
  @DisplayName("A value above the maximum is clamped and written back")
  void valueAboveMaximumIsClampedAndWrittenBack() {
    Properties properties = new Properties();
    properties.setProperty("glowStickLifetimeSeconds", "999999");

    assertEquals(
      86400,
      Config.parseConfigRangedValue(properties, "glowStickLifetimeSeconds", 2160, 0, 86400));
    assertEquals("86400", properties.getProperty("glowStickLifetimeSeconds"));
  }

  @Test
  @DisplayName("A value below the minimum is clamped and written back")
  void valueBelowMinimumIsClampedAndWrittenBack() {
    Properties properties = new Properties();
    properties.setProperty("glowStickChainLimit", "-10");

    assertEquals(0, Config.parseConfigMinValue(properties, "glowStickChainLimit", 64, 0));
    assertEquals("0", properties.getProperty("glowStickChainLimit"));
  }

  @Test
  @DisplayName("An unparsable number falls back to the default value")
  void unparsableNumberFallsBackToDefault() {
    Properties properties = new Properties();
    properties.setProperty("glowStickChainLimit", "sixty-four");

    assertEquals(64, Config.parseConfigMinValue(properties, "glowStickChainLimit", 64, 0));
    assertEquals("64", properties.getProperty("glowStickChainLimit"));
  }

  @Test
  @DisplayName("An unparsable boolean falls back to false")
  void unparsableBooleanFallsBackToFalse() {
    Properties properties = new Properties();
    properties.setProperty("allowGlowStickPickup", "maybe");

    assertFalse(Config.parseConfigValue(properties, "allowGlowStickPickup", true));
  }

  @Test
  @DisplayName("A valid value is kept and the file stays unchanged")
  void validValueIsKept() {
    Properties properties = new Properties();
    properties.setProperty("glowStickLifetimeSeconds", "600");
    Properties unmodifiedProperties = (Properties) properties.clone();

    assertEquals(
      600, Config.parseConfigRangedValue(properties, "glowStickLifetimeSeconds", 2160, 0, 86400));
    assertEquals(unmodifiedProperties, properties);
  }
}
