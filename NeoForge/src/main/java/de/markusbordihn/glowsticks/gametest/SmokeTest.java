package de.markusbordihn.glowsticks.gametest;

import de.markusbordihn.glowsticks.Constants;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;

@SuppressWarnings("unused")
public class SmokeTest {

  public void testModRegistered(GameTestHelper helper) {
    GameTestHelpers.assertTrue(
        helper,
        "Mod " + Constants.MOD_ID + " is not available!",
        ModList.get().isLoaded(Constants.MOD_ID));
    helper.succeed();
  }
}
