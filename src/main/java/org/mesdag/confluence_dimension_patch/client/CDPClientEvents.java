package org.mesdag.confluence_dimension_patch.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.confluence.mod.util.OverworldUtils;
import org.joml.Matrix4f;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;

import javax.annotation.ParametersAreNonnullByDefault;

@EventBusSubscriber(modid = ConfluenceDimensionPatch.MODID, value = Dist.CLIENT)
public final class CDPClientEvents {
    @SubscribeEvent
    public static void fmlClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ConfluenceDimensionPatch.TERRARIUM.get(), ConfluenceDimensionPatch.asResource("dimension"),
                (stack, level, entity, seed) -> level == null ? 0 : level.dimension() == OtherWorld.LEVEL ? 1 : 0)
        );
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @ParametersAreNonnullByDefault
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return CDPArmPoses.TERRARIUM.getValue();
            }
        }, ConfluenceDimensionPatch.TERRARIUM);
    }

    @SubscribeEvent
    public static void viewport$ComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration() &&
                event.getCamera().getPosition().y < OverworldUtils.getCaveY() &&
                event.getCamera().getEntity().level().dimension() == OtherWorld.LEVEL
        ) {
            event.setRed(0.5F);
            event.setGreen(0.1F);
            event.setBlue(0.1F);
        }
    }

    @SubscribeEvent
    public static void viewport$RenderFog(ViewportEvent.RenderFog event) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration() &&
                event.getMode() == FogRenderer.FogMode.FOG_TERRAIN &&
                event.getType() == FogType.NONE &&
                event.getCamera().getPosition().y < OverworldUtils.getCaveY() &&
                event.getCamera().getEntity().level().dimension() == OtherWorld.LEVEL
        ) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(128.0F);
            event.setFogShape(FogShape.SPHERE);
        }
    }

    @SubscribeEvent
    public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()) event.register(OtherWorld.EFFECTS, new DimensionSpecialEffects.OverworldEffects() {
            @Override
            public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
                return level.dimension() == OtherWorld.LEVEL && camera.getPosition().y < OverworldUtils.getCaveY();
            }

            @Override
            public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
                return camY < OverworldUtils.getCaveY() && level.dimension() == OtherWorld.LEVEL;
            }

            @Override
            public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
                return camY < OverworldUtils.getCaveY() && level.dimension() == OtherWorld.LEVEL;
            }
        });
    }
}
