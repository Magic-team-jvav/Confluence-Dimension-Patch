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
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.confluence.mod.util.OverworldUtils;
import org.joml.Matrix4f;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.mesdag.portlib.event.PortEventHandler;
import org.mesdag.portlib.event.client.extensions.common.PortRegisterClientExtensionsEvent;

import javax.annotation.ParametersAreNonnullByDefault;

public final class CDPClientEvents {
    public static void init() {
        PortEventHandler.addListener(CDPClientEvents::fmlClientSetup);
        PortEventHandler.addListener(CDPClientEvents::registerClientExtensions);
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()) {
            PortEventHandler.addListener(CDPClientEvents::viewport$ComputeFogColor);
            PortEventHandler.addListener(CDPClientEvents::viewport$RenderFog);
            PortEventHandler.addListener(CDPClientEvents::registerDimensionSpecialEffects);
        }
    }

    private static void fmlClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ConfluenceDimensionPatch.TERRARIUM.get(), ConfluenceDimensionPatch.asResource("dimension"),
                (stack, level, entity, seed) -> level == null ? 0 : level.dimension() == OtherWorld.LEVEL ? 1 : 0)
        );
    }

    private static void registerClientExtensions(PortRegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @ParametersAreNonnullByDefault
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return CDPArmPoses.TERRARIUM;
            }
        }, ConfluenceDimensionPatch.TERRARIUM);
    }

    private static void viewport$ComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (event.getCamera().getPosition().y < OverworldUtils.getCaveY() &&
                event.getCamera().getEntity().level().dimension() == OtherWorld.LEVEL
        ) {
            event.setRed(0.5F);
            event.setGreen(0.1F);
            event.setBlue(0.1F);
        }
    }

    private static void viewport$RenderFog(ViewportEvent.RenderFog event) {
        if (event.getMode() == FogRenderer.FogMode.FOG_TERRAIN &&
                event.getType() == FogType.NONE &&
                event.getCamera().getPosition().y < OverworldUtils.getCaveY() &&
                event.getCamera().getEntity().level().dimension() == OtherWorld.LEVEL
        ) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(128.0F);
            event.setFogShape(FogShape.SPHERE);
        }
    }

    private static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(OtherWorld.EFFECTS, new DimensionSpecialEffects.OverworldEffects() {

            @Override
            public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
                return level.dimension() == OtherWorld.LEVEL && camera.getPosition().y < OverworldUtils.getCaveY();
            }

            @Override
            public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
                return camY < OverworldUtils.getCaveY() && level.dimension() == OtherWorld.LEVEL;
            }

            @Override
            public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
                return camY < OverworldUtils.getCaveY() && level.dimension() == OtherWorld.LEVEL;
            }
        });
    }
}
