package de.markusbordihn.glowsticks;

import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.component.ModDataComponents;
import de.markusbordihn.glowsticks.config.Config;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.item.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(Constants.MOD_ID)
public class GlowSticks {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @SuppressWarnings({"java:S1118", "java:S2440"})
  public GlowSticks(FMLJavaModLoadingContext context) {
    final IEventBus modEventBus = context.getModEventBus();

    log.info("Initializing {} (Forge) ...", Constants.MOD_NAME);

    log.info("{} Constants ...", Constants.LOG_REGISTER_PREFIX);
    Constants.GAME_DIR = FMLPaths.GAMEDIR.get();
    Constants.CONFIG_DIR = FMLPaths.CONFIGDIR.get();
    Constants.IS_FORGE = true;

    log.info("{} Config ...", Constants.LOG_REGISTER_PREFIX);
    Config.register();

    log.info("{} Data Components ...", Constants.LOG_REGISTER_PREFIX);
    ModDataComponents.DATA_COMPONENTS.register(modEventBus);

    log.info("{} Entities ...", Constants.LOG_REGISTER_PREFIX);
    ModEntity.ENTITIES.register(modEventBus);

    log.info("{} Items ...", Constants.LOG_REGISTER_PREFIX);
    ModItems.ITEMS.register(modEventBus);

    log.info("{} Blocks ...", Constants.LOG_REGISTER_PREFIX);
    ModBlocks.BLOCKS.register(modEventBus);

    // Initialize the client mod initializer
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> new GlowSticksClient(modEventBus));
  }
}
