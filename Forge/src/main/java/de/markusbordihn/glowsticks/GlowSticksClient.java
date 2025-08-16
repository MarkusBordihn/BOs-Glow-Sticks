package de.markusbordihn.glowsticks;

import de.markusbordihn.glowsticks.client.renderer.ClientRenderer;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class GlowSticksClient {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @SuppressWarnings("java:S1118")
  public GlowSticksClient(IEventBus modEventBus) {
    log.info("Initializing {} (Forge-Client) ...", Constants.MOD_NAME);

    modEventBus.addListener(ClientRenderer::registerItemRenderer);
  }
}
