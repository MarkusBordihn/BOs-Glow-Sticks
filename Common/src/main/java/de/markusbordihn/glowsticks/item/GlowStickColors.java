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

package de.markusbordihn.glowsticks.item;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.DyeColor;

public class GlowStickColors {

  private static final Map<DyeColor, Integer> RGB = new EnumMap<>(DyeColor.class);

  static {
    RGB.put(DyeColor.WHITE, 0xFFFFFF);
    RGB.put(DyeColor.ORANGE, 0xFF9900);
    RGB.put(DyeColor.MAGENTA, 0xFF0080);
    RGB.put(DyeColor.LIGHT_BLUE, 0x0080FF);
    RGB.put(DyeColor.YELLOW, 0xFFDF00);
    RGB.put(DyeColor.LIME, 0x80EA15);
    RGB.put(DyeColor.PINK, 0xEA15EA);
    RGB.put(DyeColor.GRAY, 0x323232);
    RGB.put(DyeColor.LIGHT_GRAY, 0x808080);
    RGB.put(DyeColor.CYAN, 0x00BFBF);
    RGB.put(DyeColor.PURPLE, 0x8015EA);
    RGB.put(DyeColor.BLUE, 0x1515EA);
    RGB.put(DyeColor.BROWN, 0x804100);
    RGB.put(DyeColor.GREEN, 0x15FF00);
    RGB.put(DyeColor.RED, 0xFF0004);
    RGB.put(DyeColor.BLACK, 0x000000);
  }

  protected GlowStickColors() {}

  public static int getRgb(DyeColor dyeColor) {
    return RGB.getOrDefault(dyeColor, 0xFFFFFF);
  }

  public static float[] getRgbComponents(DyeColor dyeColor) {
    int rgb = getRgb(dyeColor);
    return new float[] {
      ((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F
    };
  }
}
