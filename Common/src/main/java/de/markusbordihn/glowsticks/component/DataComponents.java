/*
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

package de.markusbordihn.glowsticks.component;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.data.GlowStickData;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataComponents {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public static DataComponentType<GlowStickData> GLOW_STICK_DATA;

  private DataComponents() {}

  public static void registerGlowStickData() {
    log.info("{} Data Components ...", Constants.MOD_NAME);
    GLOW_STICK_DATA =
        Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, GlowStickData.ID),
            DataComponentType.<GlowStickData>builder()
                .persistent(GlowStickData.CODEC)
                .networkSynchronized(GlowStickData.STREAM_CODEC)
                .build());
  }

  public static void registerGlowStickData(Supplier<DataComponentType<GlowStickData>> supplier) {
    log.info("{} Glow Stick Data Component {} ...", Constants.MOD_NAME, supplier.get());
    GLOW_STICK_DATA = supplier.get();
  }
}
