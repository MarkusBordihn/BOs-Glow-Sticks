package de.markusbordihn.glowsticks;

import de.markusbordihn.glowsticks.client.renderer.ClientRenderer;
import de.markusbordihn.glowsticks.tabs.ModTabs;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlowSticksClient implements ClientModInitializer {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @Override
  public void onInitializeClient() {
    log.info("Initializing {} (Fabric-Client) ...", Constants.MOD_NAME);

    log.info("{} Client Renderer ...", Constants.LOG_REGISTER_PREFIX);
    ClientRenderer.registerItemRenderer();
    ClientRenderer.registerRenderers();

    log.info("{} Tabs ...", Constants.LOG_REGISTER_PREFIX);
    ModTabs.registerModTabs();
  }
}
