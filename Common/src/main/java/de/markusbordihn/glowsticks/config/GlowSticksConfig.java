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

  Glow Stick Despawn:
  - glowStickDespawnEnabled: Enable or disable the automatic despawn of glow sticks.
  - glowStickDespawnTicks: Controls the aging probability of glow sticks during random ticks.
    A value of 1 means every random tick has a 100% chance to age the glow stick.
    A value of 2 means every random tick has a 50% chance to age the glow stick.
    A value of 5 means every random tick has a 20% chance to age the glow stick.
    Higher values make glow sticks last longer on average.
    When a glow stick reaches age 15, it will despawn automatically.

""";

  public static boolean glowStickDespawnEnabled = true;
  public static int glowStickDespawnTicks = 2;

  public static void registerConfig() {
    registerConfigFile(CONFIG_FILE_NAME, CONFIG_FILE_HEADER);
    parseConfigFile();
  }

  public static void parseConfigFile() {
    File configFile = getConfigFile(CONFIG_FILE_NAME);
    Properties properties = readConfigFile(configFile);
    Properties unmodifiedProperties = (Properties) properties.clone();

    // Config entries
    glowStickDespawnEnabled =
        parseConfigValue(properties, "glowStickDespawnEnabled", glowStickDespawnEnabled);
    glowStickDespawnTicks =
        parseConfigValue(properties, "glowStickDespawnTicks", glowStickDespawnTicks);

    // Update config file if needed
    updateConfigFileIfChanged(configFile, CONFIG_FILE_HEADER, properties, unmodifiedProperties);
  }
}
