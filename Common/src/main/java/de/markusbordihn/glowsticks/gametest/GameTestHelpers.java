package de.markusbordihn.glowsticks.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

public class GameTestHelpers {

  private GameTestHelpers() {
  }

  public static void assertTrue(GameTestHelper helper, String message, boolean condition) {
    if (condition) {
      helper.succeed();
    } else {
      helper.fail(Component.literal(message));
    }
  }

  public static void assertNotNull(GameTestHelper helper, String message, Object object) {
    assertTrue(helper, message, object != null);
  }

  public static void assertCondition(GameTestHelper helper, String message, boolean condition) {
    if (!condition) {
      helper.fail(Component.literal(message));
    }
  }

  public static void assertBlockPresent(GameTestHelper helper, BlockPos blockPos, Block block) {
    if (!helper.getBlockState(blockPos).is(block)) {
      helper.fail(Component.literal("Expected block " + block + " at " + blockPos + "!"));
    }
  }

  public static void assertBlockAbsent(GameTestHelper helper, BlockPos blockPos, Block block) {
    if (helper.getBlockState(blockPos).is(block)) {
      helper.fail(Component.literal("Did not expect block " + block + " at " + blockPos + "!"));
    }
  }
}
