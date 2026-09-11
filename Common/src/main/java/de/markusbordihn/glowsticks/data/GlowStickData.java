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

package de.markusbordihn.glowsticks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.markusbordihn.glowsticks.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record GlowStickData(boolean activated, int step, int age) {

  public static final String ID = "glow_stick_data";
  public static final String STEP_TAG = "step";
  public static final String ACTIVATED_TAG = "activated";
  public static final String AGE_TAG = "age";

  public static final GlowStickData DEFAULT = new GlowStickData(false, 0, 0);
  public static final ResourceLocation STEP_PREDICATE =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, STEP_TAG);
  public static final ResourceLocation ACTIVATED_PREDICATE =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, ACTIVATED_TAG);

  public static final Codec<GlowStickData> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.BOOL.fieldOf(ACTIVATED_TAG).forGetter(GlowStickData::activated),
                      Codec.INT.fieldOf(STEP_TAG).forGetter(GlowStickData::step),
                      Codec.INT.optionalFieldOf(AGE_TAG, 0).forGetter(GlowStickData::age))
                  .apply(instance, GlowStickData::new));

  public static final StreamCodec<ByteBuf, GlowStickData> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.BOOL,
          GlowStickData::activated,
          ByteBufCodecs.VAR_INT,
          GlowStickData::step,
          ByteBufCodecs.VAR_INT,
          GlowStickData::age,
          GlowStickData::new);

  public GlowStickData withActivated(boolean activated) {
    return new GlowStickData(activated, this.step, this.age);
  }

  public GlowStickData withStep(int step) {
    return new GlowStickData(this.activated, step, this.age);
  }

  public GlowStickData withIncrementedStep() {
    return new GlowStickData(this.activated, this.step + 1, this.age);
  }

  public GlowStickData withAge(int age) {
    return new GlowStickData(this.activated, this.step, age);
  }
}
