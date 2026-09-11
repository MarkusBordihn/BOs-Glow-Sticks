package de.markusbordihn.glowsticks.gametest;

import de.markusbordihn.glowsticks.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestHelpers {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private GameTestHelpers() {}

  public static void assertTrue(GameTestHelper helper, String message, boolean condition) {
    if (condition) {
      helper.succeed();
    } else {
      helper.fail(message);
    }
  }

  public static void assertNotNull(GameTestHelper helper, String message, Object object) {
    assertTrue(helper, message, object != null);
  }

  public static void assertCondition(GameTestHelper helper, String message, boolean condition) {
    if (!condition) {
      helper.fail(message);
    }
  }

  public static void assertBlockPresent(GameTestHelper helper, BlockPos blockPos, Block block) {
    if (!helper.getBlockState(blockPos).is(block)) {
      helper.fail("Expected block " + block + " at " + blockPos + "!");
    }
  }

  public static void assertBlockAbsent(GameTestHelper helper, BlockPos blockPos, Block block) {
    if (helper.getBlockState(blockPos).is(block)) {
      helper.fail("Did not expect block " + block + " at " + blockPos + "!");
    }
  }
}
