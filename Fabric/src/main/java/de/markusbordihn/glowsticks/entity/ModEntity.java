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
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

public class ModEntity {

  protected static final Map<DyeColor, EntityType<GlowStickProjectile>> GLOW_STICK_ENTITIES =
      new EnumMap<>(DyeColor.class);

  protected ModEntity() {}

  public static void registerEntities() {
    // Register all glow stick entities for each dye color
    for (DyeColor dyeColor : DyeColor.values()) {
      String colorName = dyeColor.getName();

      EntityType<GlowStickProjectile> glowStickEntity =
          FabricEntityTypeBuilder.<GlowStickProjectile>create(
                  MobCategory.MISC,
                  (entityType, level) -> createGlowStickProjectile(entityType, level, dyeColor))
              .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
              .trackRangeChunks(4)
              .trackedUpdateRate(10)
              .build();

      GLOW_STICK_ENTITIES.put(dyeColor, glowStickEntity);
      Registry.register(
          Registry.ENTITY_TYPE,
          new ResourceLocation(Constants.MOD_ID, "glow_stick_" + colorName),
          glowStickEntity);
    }
  }

  public static EntityType<GlowStickProjectile> getGlowStickEntity(final DyeColor dyeColor) {
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
        () -> ModBlocks.getGlowStickBlock(dyeColor),
        () -> ModItems.getGlowStickItem(dyeColor),
        () -> ModBlocks.GLOW_STICK_LIGHT,
        () -> ModBlocks.GLOW_STICK_LIGHT_WATER);
  }
}
