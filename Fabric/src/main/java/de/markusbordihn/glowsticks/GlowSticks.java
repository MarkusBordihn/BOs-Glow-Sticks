package de.markusbordihn.glowsticks;

import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.component.DataComponents;
import de.markusbordihn.glowsticks.config.Config;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlowSticks implements ModInitializer {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @Override
  public void onInitialize() {
    log.info("Initializing {} (Fabric) ...", Constants.MOD_NAME);

    log.info("{} Constants ...", Constants.LOG_REGISTER_PREFIX);
    Constants.GAME_DIR = FabricLoader.getInstance().getGameDir();
    Constants.CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    Constants.IS_FABRIC = true;
    Constants.HAS_FABRIC_TOOLTIPFIX_MOD = FabricLoader.getInstance().isModLoaded("tooltipfix");

    log.info("{} Config ...", Constants.LOG_REGISTER_PREFIX);
    Config.register();

    log.info("{} Data Components ...", Constants.LOG_REGISTER_PREFIX);
    DataComponents.registerGlowStickData();

    log.info("{} Items ...", Constants.LOG_REGISTER_PREFIX);
    ModItems.registerItems();

    log.info("{} Blocks ...", Constants.LOG_REGISTER_PREFIX);
    ModBlocks.registerBlocks();

    log.info("{} Entities ...", Constants.LOG_REGISTER_PREFIX);
    ModEntity.registerEntities();
  }
}
