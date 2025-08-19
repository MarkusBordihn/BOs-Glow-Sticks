package de.markusbordihn.glowsticks;

import de.markusbordihn.glowsticks.client.renderer.ClientRenderer;
import de.markusbordihn.glowsticks.client.renderer.item.properties.ModItemProperties;
import de.markusbordihn.glowsticks.tabs.ModTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class GlowSticksClient {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @SuppressWarnings("java:S1118")
  public GlowSticksClient(IEventBus modEventBus) {
    log.info("Initializing {} (NeoForge-Client) ...", Constants.MOD_NAME);

    log.info("{} Registering client event handlers ...", Constants.LOG_REGISTER_PREFIX);
    modEventBus.addListener(ClientRenderer::registerItemRenderer);
    modEventBus.addListener(ClientRenderer::registerRenderers);
    ModTabs.CREATIVE_MODE_TABS.register(modEventBus);

    log.info("{} Item Properties ...", Constants.LOG_REGISTER_PREFIX);
    ModItemProperties.registerItemProperties();
  }
}
