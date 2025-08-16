package de.markusbordihn.glowsticks;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {

  public static final String MOD_ID = "glow_sticks";
  public static final String MOD_NAME = "Glow Sticks";
  public static final String MOD_COMMAND = MOD_ID;
  public static final String LOG_NAME = MOD_NAME;
  public static final String LOG_SUB_REGISTER_PREFIX = "- Register " + LOG_NAME;
  public static final String LOG_REGISTER_PREFIX = "Register " + MOD_NAME;
  public static final String TEXT_PREFIX = "text." + MOD_ID + ".";

  public static Path GAME_DIR = Paths.get("").toAbsolutePath();
  public static Path CONFIG_DIR = GAME_DIR.resolve("config");

  public static boolean IS_FABRIC = false;
  public static boolean IS_FORGE = false;
  public static boolean IS_NEOFORGE = false;

  public static boolean HAS_FABRIC_TOOLTIPFIX_MOD = false;

  private Constants() {}
}
