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

package de.markusbordihn.glowsticks.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.item.ModItems;

public class ModBlocks {

  protected ModBlocks() {

  }

  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

  // Glow Stick Block
  private static final BlockBehaviour.Properties glowStickBlockProperties() {
    return BlockBehaviour.Properties.of(Material.GLASS).sound(SoundType.SCAFFOLDING).noOcclusion()
        .lightLevel(GlowStickBlock::getLightLevel).randomTicks();
  }
  public static final RegistryObject<Block> GLOW_STICK_WHITE = BLOCKS.register("glow_stick_white",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_WHITE));
  public static final RegistryObject<Block> GLOW_STICK_ORANGE = BLOCKS.register("glow_stick_orange",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_ORANGE));
  public static final RegistryObject<Block> GLOW_STICK_MAGENTA =
      BLOCKS.register("glow_stick_magenta",
          () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_MAGENTA));
  public static final RegistryObject<Block> GLOW_STICK_LIGHT_BLUE =
      BLOCKS.register("glow_stick_light_blue",
          () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_LIGHT_BLUE));
  public static final RegistryObject<Block> GLOW_STICK_YELLOW = BLOCKS.register("glow_stick_yellow",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_YELLOW));
  public static final RegistryObject<Block> GLOW_STICK_LIME = BLOCKS.register("glow_stick_lime",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_LIME));
  public static final RegistryObject<Block> GLOW_STICK_PINK = BLOCKS.register("glow_stick_pink",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_PINK));
  public static final RegistryObject<Block> GLOW_STICK_GRAY = BLOCKS.register("glow_stick_gray",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_GRAY));
  public static final RegistryObject<Block> GLOW_STICK_LIGHT_GRAY =
      BLOCKS.register("glow_stick_light_gray",
          () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_LIGHT_GRAY));
  public static final RegistryObject<Block> GLOW_STICK_CYAN = BLOCKS.register("glow_stick_cyan",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_CYAN));
  public static final RegistryObject<Block> GLOW_STICK_PURPLE = BLOCKS.register("glow_stick_purple",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_PURPLE));
  public static final RegistryObject<Block> GLOW_STICK_BLUE = BLOCKS.register("glow_stick_blue",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_BLUE));
  public static final RegistryObject<Block> GLOW_STICK_BROWN = BLOCKS.register("glow_stick_brown",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_BROWN));
  public static final RegistryObject<Block> GLOW_STICK_GREEN = BLOCKS.register("glow_stick_green",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_GREEN));
  public static final RegistryObject<Block> GLOW_STICK_RED = BLOCKS.register("glow_stick_red",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_RED));
  public static final RegistryObject<Block> GLOW_STICK_BLACK = BLOCKS.register("glow_stick_black",
      () -> new GlowStickBlock(glowStickBlockProperties(), ModItems.GLOW_STICK_BLACK));

  // Light Block
  public static final RegistryObject<Block> GLOW_STICK_LIGHT =
      BLOCKS.register("glow_stick_light", () -> new GlowStickLightBlock(
          BlockBehaviour.Properties.of(Material.AIR).noCollission().lightLevel(blockState -> 15)));
  public static final RegistryObject<Block> GLOW_STICK_LIGHT_WATER =
      BLOCKS.register("glow_stick_light_water", () -> new GlowStickLightWaterBlock(
          BlockBehaviour.Properties.of(Material.AIR).noCollission().lightLevel(blockState -> 15)));

  public static final DeferredRegister<BlockEntityType<?>> ENTITIES =
      DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Constants.MOD_ID);

}
