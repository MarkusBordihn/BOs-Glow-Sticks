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

package de.markusbordihn.glowsticks.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.glowsticks.block.glowstick.BlockStateManager;
import de.markusbordihn.glowsticks.block.glowstick.RedstoneCapable;
import de.markusbordihn.glowsticks.item.GlowStickColor;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GlowStickBlockStateTest {

  private static GlowStickBlock glowStickBlock;
  private static CreativeGlowStickBlock creativeGlowStickBlock;

  @BeforeAll
  static void createGlowStickBlocks() throws ReflectiveOperationException {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
    reopenBlockRegistryForUnregisteredBlocks();
    glowStickBlock =
      new GlowStickBlock(
        GlowStickBlockProperties.createGlowStickBlockProperties("glow_stick_white"),
        GlowStickColor.WHITE);
    creativeGlowStickBlock =
      new CreativeGlowStickBlock(
        GlowStickBlockProperties.createCreativeGlowStickBlockProperties(
          "creative_glow_stick_white"),
        GlowStickColor.WHITE);
  }

  private static void reopenBlockRegistryForUnregisteredBlocks()
    throws ReflectiveOperationException {
    setBlockRegistryField("frozen", false);
    setBlockRegistryField("unregisteredIntrusiveHolders", new IdentityHashMap<>());
  }

  private static void setBlockRegistryField(String fieldName, Object value)
    throws ReflectiveOperationException {
    Field field = MappedRegistry.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(BuiltInRegistries.BLOCK, value);
  }

  private static BlockState levelState(int level) {
    return glowStickBlock.defaultBlockState().setValue(GlowStickBlock.LEVEL, level);
  }

  @Test
  @DisplayName("A fresh glow stick starts uncontrolled at full brightness")
  void freshGlowStickIsUncontrolledAndBright() {
    BlockState freshState = glowStickBlock.defaultBlockState();

    assertEquals(0, freshState.getValue(GlowStickBlock.LEVEL));
    assertFalse(RedstoneCapable.isControlled(freshState));
    assertEquals(0, BlockStateManager.getAge(freshState));
    assertEquals(15, freshState.getLightEmission());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
  @DisplayName("Levels 0 to 15 are uncontrolled ageing stages")
  void uncontrolledLevelsAgeWithoutRedstone(int level) {
    BlockState blockState = levelState(level);

    assertFalse(RedstoneCapable.isControlled(blockState));
    assertFalse(RedstoneCapable.isPowered(blockState));
    assertEquals(0, RedstoneCapable.getPower(blockState));
    assertEquals(level, BlockStateManager.getAge(blockState));
    assertEquals(Math.max(1, 15 - level), blockState.getLightEmission());
  }

  @ParameterizedTest
  @ValueSource(ints = {16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31})
  @DisplayName("Levels 16 to 31 are redstone controlled and light up with their signal")
  void controlledLevelsFollowTheirSignal(int level) {
    BlockState blockState = levelState(level);
    int expectedPower = level - RedstoneCapable.CONTROLLED_LEVEL_OFFSET;

    assertTrue(RedstoneCapable.isControlled(blockState));
    assertEquals(expectedPower, RedstoneCapable.getPower(blockState));
    assertEquals(expectedPower > 0, RedstoneCapable.isPowered(blockState));
    assertEquals(expectedPower, blockState.getLightEmission());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
  @DisplayName("A controlled glow stick never ages")
  void controlledGlowStickKeepsItsSignalInsteadOfAgeing(int signal) {
    BlockState controlledState =
      RedstoneCapable.withControlValue(
        glowStickBlock.defaultBlockState(), RedstoneCapable.encodeControlState(true, signal));

    assertSame(controlledState, BlockStateManager.withAge(controlledState, 12));
    assertEquals(15 - signal, BlockStateManager.getAge(controlledState));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
  @DisplayName("Taking a glow stick under redstone control and releasing it keeps its brightness")
  void releasingRedstoneControlKeepsTheBrightness(int signal) {
    BlockState controlledState =
      RedstoneCapable.withControlValue(
        glowStickBlock.defaultBlockState(), RedstoneCapable.encodeControlState(true, signal));
    BlockState releasedState =
      RedstoneCapable.withControlValue(controlledState, RedstoneCapable.UNCONTROLLED);

    assertFalse(RedstoneCapable.isControlled(releasedState));
    assertEquals(15 - signal, releasedState.getValue(GlowStickBlock.LEVEL));
    assertEquals(Math.max(1, signal), releasedState.getLightEmission());
  }

  @Test
  @DisplayName("Releasing an already uncontrolled glow stick keeps its remaining lifetime")
  void releasingAnUncontrolledGlowStickChangesNothing() {
    BlockState agedState = BlockStateManager.withAge(glowStickBlock.defaultBlockState(), 4);

    assertSame(
      agedState, RedstoneCapable.withControlValue(agedState, RedstoneCapable.UNCONTROLLED));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16})
  @DisplayName("Every control value survives a write and read on both glow stick blocks")
  void controlValuesRoundTripOnBothBlocks(int controlValue) {
    assertEquals(
      controlValue,
      RedstoneCapable.getControlValue(
        RedstoneCapable.withControlValue(glowStickBlock.defaultBlockState(), controlValue)));
    assertEquals(
      controlValue,
      RedstoneCapable.getControlValue(
        RedstoneCapable.withControlValue(
          creativeGlowStickBlock.defaultBlockState(), controlValue)));
  }

  @Test
  @DisplayName("Signals outside the redstone range are clamped")
  void outOfRangeSignalsAreClamped() {
    assertEquals(RedstoneCapable.UNCONTROLLED, RedstoneCapable.encodeControlState(false, 15));
    assertEquals(1, RedstoneCapable.encodeControlState(true, -4));
    assertEquals(RedstoneCapable.MAX_CONTROL_VALUE, RedstoneCapable.encodeControlState(true, 4711));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16})
  @DisplayName("A creative glow stick stays fully lit until redstone dims it")
  void creativeGlowStickFollowsRedstoneOnly(int controlValue) {
    BlockState blockState =
      RedstoneCapable.withControlValue(creativeGlowStickBlock.defaultBlockState(), controlValue);

    assertEquals(
      controlValue == RedstoneCapable.UNCONTROLLED ? 15 : controlValue - 1,
      blockState.getLightEmission());
  }

  @Test
  @DisplayName("The glow stick blocks keep their known block state count")
  void blockStateCountStaysSmall() {
    assertEquals(256, glowStickBlock.getStateDefinition().getPossibleStates().size());
    assertEquals(2448, creativeGlowStickBlock.getStateDefinition().getPossibleStates().size());
  }
}
