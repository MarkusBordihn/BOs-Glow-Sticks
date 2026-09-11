package de.markusbordihn.glowsticks;

import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.component.ModDataComponents;
import de.markusbordihn.glowsticks.config.Config;
import de.markusbordihn.glowsticks.crafting.CreativeRecipeCondition;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(Constants.MOD_ID)
public class GlowSticks {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @SuppressWarnings({"java:S1118", "java:S2440"})
  public GlowSticks(IEventBus modEventBus) {
    log.info("Initializing {} (NeoForge) ...", Constants.MOD_NAME);

    log.info("{} Constants ...", Constants.LOG_REGISTER_PREFIX);
    Constants.GAME_DIR = FMLPaths.GAMEDIR.get();
    Constants.CONFIG_DIR = FMLPaths.CONFIGDIR.get();
    Constants.IS_NEOFORGE = true;

    log.info("{} Config ...", Constants.LOG_REGISTER_PREFIX);
    Config.register();

    log.info("{} Data Components ...", Constants.LOG_REGISTER_PREFIX);
    ModDataComponents.DATA_COMPONENTS.register(modEventBus);

    log.info("{} Recipe Conditions ...", Constants.LOG_REGISTER_PREFIX);
    CreativeRecipeCondition.register(modEventBus);

    log.info("{} Entities ...", Constants.LOG_REGISTER_PREFIX);
    ModEntity.ENTITIES.register(modEventBus);

    log.info("{} Items ...", Constants.LOG_REGISTER_PREFIX);
    ModItems.ITEMS.register(modEventBus);

    log.info("{} Blocks ...", Constants.LOG_REGISTER_PREFIX);
    ModBlocks.BLOCKS.register(modEventBus);
  }
}
