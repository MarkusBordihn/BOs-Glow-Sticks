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

package de.markusbordihn.glowsticks.client.renderer.item.properties;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.client.renderer.item.properties.conditional.GlowStickActivated;
import de.markusbordihn.glowsticks.client.renderer.item.properties.numeric.GlowStickStep;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.resources.Identifier;

public class ModItemProperties {

  public static void registerItemProperties() {
    ConditionalItemModelProperties.ID_MAPPER.put(
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "activated"),
      GlowStickActivated.MAP_CODEC);

    RangeSelectItemModelProperties.ID_MAPPER.put(
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "step"), GlowStickStep.MAP_CODEC);
  }
}
