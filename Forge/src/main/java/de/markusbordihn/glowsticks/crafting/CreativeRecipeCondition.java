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

package de.markusbordihn.glowsticks.crafting;

import com.google.gson.JsonObject;
import de.markusbordihn.glowsticks.Constants;
import de.markusbordihn.glowsticks.config.GlowSticksConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class CreativeRecipeCondition implements ICondition {

  public static final ResourceLocation ID =
      new ResourceLocation(Constants.MOD_ID, "creative_recipe_enabled");
  private static final CreativeRecipeCondition INSTANCE = new CreativeRecipeCondition();

  public static void register() {
    CraftingHelper.register(new Serializer());
  }

  @Override
  public ResourceLocation getID() {
    return ID;
  }

  @Override
  public boolean test(IContext context) {
    return GlowSticksConfig.enableCreativeGlowStickRecipe;
  }

  public static class Serializer implements IConditionSerializer<CreativeRecipeCondition> {

    @Override
    public void write(JsonObject json, CreativeRecipeCondition condition) {}

    @Override
    public CreativeRecipeCondition read(JsonObject json) {
      return INSTANCE;
    }

    @Override
    public ResourceLocation getID() {
      return CreativeRecipeCondition.ID;
    }
  }
}
