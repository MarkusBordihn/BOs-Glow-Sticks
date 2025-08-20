package de.markusbordihn.glowsticks;

import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.client.renderer.item.properties.ModItemProperties;
import de.markusbordihn.glowsticks.component.ModDataComponents;
import de.markusbordihn.glowsticks.config.Config;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.item.ModItems;
import de.markusbordihn.glowsticks.tabs.ModTabs;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MOD_ID)
public class GlowSticks {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @SuppressWarnings({"java:S1118", "java:S2440"})
  public GlowSticks(FMLJavaModLoadingContext context) {
    final BusGroup modBusGroup = context.getModBusGroup();

    log.info("Initializing {} (Forge) ...", Constants.MOD_NAME);

    log.info("{} Constants ...", Constants.LOG_REGISTER_PREFIX);
    Constants.GAME_DIR = FMLPaths.GAMEDIR.get();
    Constants.CONFIG_DIR = FMLPaths.CONFIGDIR.get();
    Constants.IS_FORGE = true;

    log.info("{} Config ...", Constants.LOG_REGISTER_PREFIX);
    Config.register();

    log.info("{} Data Components ...", Constants.LOG_REGISTER_PREFIX);
    ModDataComponents.DATA_COMPONENTS.register(modBusGroup);

    log.info("{} Entities ...", Constants.LOG_REGISTER_PREFIX);
    ModEntity.ENTITIES.register(modBusGroup);

    log.info("{} Items ...", Constants.LOG_REGISTER_PREFIX);
    ModItems.ITEMS.register(modBusGroup);

    log.info("{} Blocks ...", Constants.LOG_REGISTER_PREFIX);
    ModBlocks.BLOCKS.register(modBusGroup);

    log.info("{} Creative Tabs ...", Constants.LOG_REGISTER_PREFIX);
    ModTabs.CREATIVE_MODE_TABS.register(modBusGroup);

    if (FMLEnvironment.dist.isClient()) {
      ModItemProperties.registerItemProperties();
    }
  }
}
