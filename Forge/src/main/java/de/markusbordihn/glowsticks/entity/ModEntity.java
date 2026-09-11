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

package de.markusbordihn.glowsticks.entity;

import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.entity.projectile.GlowStickProjectile;
import de.markusbordihn.glowsticks.item.ModItems;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntity {

  public static final DeferredRegister<EntityType<?>> ENTITIES =
      DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MOD_ID);
  public static final RegistryObject<EntityType<GlowStickProjectile>> GLOW_STICK;
  protected static final Map<DyeColor, RegistryObject<EntityType<GlowStickProjectile>>>
      GLOW_STICK_ENTITIES = new EnumMap<>(DyeColor.class);

  static {
    for (DyeColor dyeColor : DyeColor.values()) {
      String colorName = dyeColor.getName();
      GLOW_STICK_ENTITIES.put(
          dyeColor,
          ENTITIES.register(
              "glow_stick_" + colorName,
              () ->
                  EntityType.Builder.<GlowStickProjectile>of(
                          (entityType, level) ->
                              createGlowStickProjectile(entityType, level, dyeColor),
                          MobCategory.MISC)
                      .sized(0.25F, 0.25F)
                      .clientTrackingRange(4)
                      .updateInterval(10)
                      .build(
                          ResourceLocation.fromNamespaceAndPath(
                                  Constants.MOD_ID, "glow_stick_" + colorName)
                              .toString())));
    }
    GLOW_STICK = GLOW_STICK_ENTITIES.get(DyeColor.WHITE);
  }

  protected ModEntity() {}

  public static RegistryObject<EntityType<GlowStickProjectile>> getGlowStickEntity(
      final DyeColor dyeColor) {
    return GLOW_STICK_ENTITIES.get(dyeColor);
  }

  private static GlowStickProjectile createGlowStickProjectile(
      final EntityType<? extends GlowStickProjectile> entityType,
      final Level level,
      final DyeColor dyeColor) {
    return new GlowStickProjectile(
        entityType,
        level,
        dyeColor,
        () -> ModBlocks.getGlowStickBlock(dyeColor).get(),
        () -> ModItems.getGlowStickItem(dyeColor).get(),
        ModBlocks.GLOW_STICK_LIGHT,
        ModBlocks.GLOW_STICK_LIGHT_WATER);
  }
}
