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

import com.mojang.serialization.Codec;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringRepresentable;

public enum GlowStickColor implements StringRepresentable {
  WHITE("white", 0xFFFFFF),
  ORANGE("orange", 0xFF9900),
  MAGENTA("magenta", 0xFF0080),
  LIGHT_BLUE("light_blue", 0x0080FF),
  YELLOW("yellow", 0xFFDF00),
  LIME("lime", 0x80EA15),
  PINK("pink", 0xEA15EA),
  GRAY("gray", 0x323232),
  LIGHT_GRAY("light_gray", 0x808080),
  CYAN("cyan", 0x00BFBF),
  PURPLE("purple", 0x8015EA),
  BLUE("blue", 0x1515EA),
  BROWN("brown", 0x804100),
  GREEN("green", 0x15FF00),
  RED("red", 0xFF0004),
  BLACK("black", 0x000000),
  MAROON("maroon", 0x7B2713),
  ROSE("rose", 0xFF5E64),
  CORAL("coral", 0xDF7758),
  INDIGO("indigo", 0x331E57),
  NAVY("navy", 0x153D64),
  SLATE("slate", 0x4C5E86),
  OLIVE("olive", 0x8C8F2A),
  AMBER("amber", 0xD7AF00),
  BEIGE("beige", 0xE1D5A3),
  TEAL("teal", 0x2F7B67),
  MINT("mint", 0x38CE7D),
  AQUA("aqua", 0x5EF0CC),
  VERDANT("verdant", 0x255714),
  FOREST("forest", 0x32A326),
  GINGER("ginger", 0xCF6121),
  TAN("tan", 0xF49C5D);

  public static final Codec<GlowStickColor> CODEC =
    StringRepresentable.fromEnum(GlowStickColor::values);

  private final String name;
  private final int rgb;
  private final int opaqueArgb;
  private final float[] rgbComponents;

  GlowStickColor(String name, int rgb) {
    this.name = name;
    this.rgb = rgb;
    this.opaqueArgb = ARGB.opaque(rgb);
    this.rgbComponents =
      new float[]{
        ((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F
      };
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String getSerializedName() {
    return this.name;
  }

  public int getRgb() {
    return this.rgb;
  }

  public int getOpaqueArgb() {
    return this.opaqueArgb;
  }

  public float[] getRgbComponents() {
    return this.rgbComponents.clone();
  }
}
