package de.markusbordihn.glowsticks.gametest;

import de.markusbordihn.glowsticks.Constants;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTest;

@SuppressWarnings("unused")
public class SmokeTest {

  @GameTest(structure = "glow_sticks:gametest.3x3x3")
  public void testModRegistered(GameTestHelper helper) {
    GameTestHelpers.assertTrue(
        helper,
        "Mod " + Constants.MOD_ID + " is not available!",
        ModList.get().isLoaded(Constants.MOD_ID));
    helper.succeed();
  }
}
