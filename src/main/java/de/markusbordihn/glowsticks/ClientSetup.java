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

package de.markusbordihn.glowsticks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import de.markusbordihn.glowsticks.block.ModBlocks;
import de.markusbordihn.glowsticks.entity.ModEntity;
import de.markusbordihn.glowsticks.item.GlowStickItem;
import de.markusbordihn.glowsticks.item.ModItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public ClientSetup(final FMLClientSetupEvent event) {
    log.info("{} Client Setup ...", Constants.LOG_REGISTER_PREFIX);

    event.enqueueWork(() -> {

      // Glow Stick (animation steps)
      ResourceLocation animationStep =
          new ResourceLocation(Constants.MOD_ID, GlowStickItem.TAG_STEP);
      ItemModelsProperties.register(ModItems.GLOW_STICK_WHITE.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_ORANGE.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_MAGENTA.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_LIGHT_BLUE.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_YELLOW.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_LIME.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_PINK.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_GRAY.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_LIGHT_GRAY.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_CYAN.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_PURPLE.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_BLUE.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_BROWN.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_GREEN.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_RED.get(), animationStep,
          ClientSetup::getStepFromTag);
      ItemModelsProperties.register(ModItems.GLOW_STICK_BLACK.get(), animationStep,
          ClientSetup::getStepFromTag);

      // Glow Stick Blocks (translucent)
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_WHITE.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_ORANGE.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_MAGENTA.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_LIGHT_BLUE.get(),
          RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_YELLOW.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_LIME.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_PINK.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_GRAY.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_LIGHT_GRAY.get(),
          RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_CYAN.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_PURPLE.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_BLUE.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_BROWN.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_GREEN.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_RED.get(), RenderType.translucent());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_BLACK.get(), RenderType.translucent());

      // Glow Stick Light Blocks (cutout mip)
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_LIGHT.get(), RenderType.cutoutMipped());
      RenderTypeLookup.setRenderLayer(ModBlocks.GLOW_STICK_LIGHT_WATER.get(),
          RenderType.cutoutMipped());
    });
  }

  @SubscribeEvent
  public static void registerModels(final ModelRegistryEvent event) {
    log.info("{} Client Models and Renderer ...", Constants.LOG_REGISTER_PREFIX);
    RenderingRegistry.registerEntityRenderingHandler(ModEntity.GLOW_STICK.get(),
        renderManager -> new SpriteRenderer<>(renderManager,
            Minecraft.getInstance().getItemRenderer()));
  }

  public static float getStepFromTag(ItemStack itemStack, ClientWorld world, LivingEntity living) {
    CompoundNBT compoundNBT = itemStack.getTag();
    return (living != null && living.isUsingItem() && living.getUseItem() == itemStack
        && compoundNBT != null) ? compoundNBT.getInt(GlowStickItem.TAG_STEP) : 0.0F;
  }
}
