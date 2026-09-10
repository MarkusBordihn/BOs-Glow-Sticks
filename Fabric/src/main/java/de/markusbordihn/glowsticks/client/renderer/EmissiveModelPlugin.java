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

package de.markusbordihn.glowsticks.client.renderer;

import de.markusbordihn.glowsticks.Constants;
import java.util.Set;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;

public class EmissiveModelPlugin implements ModelLoadingPlugin {

  private static final String BLOCK_MODEL_PREFIX = "block/glow_stick/";
  private static final Set<String> EMISSIVE_ITEM_MODELS =
      Set.of(
          "item/glow_stick/glow_stick_step_4",
          "item/glow_stick/glow_stick_step_5",
          "item/glow_stick/glow_stick_step_6",
          "item/glow_stick/glow_stick_activated");

  private static boolean isEmissiveModel(ResourceLocation resourceLocation) {
    if (resourceLocation == null || !Constants.MOD_ID.equals(resourceLocation.getNamespace())) {
      return false;
    }

    String path = resourceLocation.getPath();
    return path.startsWith(BLOCK_MODEL_PREFIX) || EMISSIVE_ITEM_MODELS.contains(path);
  }

  @Override
  public void onInitializeModelLoader(Context pluginContext) {
    pluginContext
        .modifyModelAfterBake()
        .register(
            (model, context) -> {
              if (model != null && isEmissiveModel(context.id())) {
                return new EmissiveBakedModel(model);
              }
              return model;
            });
  }
}
