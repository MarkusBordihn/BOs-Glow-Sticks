package de.markusbordihn.glowsticks.gametest;

import de.markusbordihn.glowsticks.Constants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@SuppressWarnings("unused")
@PrefixGameTestTemplate(value = false)
@GameTestHolder(Constants.MOD_ID)
public class SmokeTest {

  @GameTest(template = "gametest.3x3x3")
  public void testModRegistered(GameTestHelper helper) {
    GameTestHelpers.assertTrue(
        helper,
        "Mod " + Constants.MOD_ID + " is not available!",
        ModList.get().isLoaded(Constants.MOD_ID));
    helper.succeed();
  }
}
