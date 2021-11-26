/**
 * Copyright 2021 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.glowsticks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.item.ModItems;

@Mod(Constants.MOD_ID)
public class GlowSticks {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public GlowSticks() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::new);

    log.info("🧪 Register Glow Stick Entities ...");
    ModEntity.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

    log.info("🧪 Register Glow Stick Items ...");
    ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

    log.info("🧪 Register Glow Stick Blocks ...");
    ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());

    log.info("🧪 Register Glow Stick Blocks Entities ...");
    ModBlocks.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
  }

}
