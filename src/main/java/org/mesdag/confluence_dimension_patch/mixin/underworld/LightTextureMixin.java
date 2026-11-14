package org.mesdag.confluence_dimension_patch.mixin.underworld;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.confluence.mod.util.OverworldUtils;
import org.mesdag.confluence_dimension_patch.CDPStartupConfigs;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// The Nether has an ambient light floor of 0.1. The otherworld dimension keeps the overworld
/// ambient light at the surface, but the underworld layer should use the Nether value.
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @ModifyExpressionValue(method = "getBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;ambientLight()F"))
    private static float cdp$netherAmbientLightInUnderworld(float original) {
        if (CDPStartupConfigs.enableTerrariaLikeWorldGeneration()) {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            double y = camera.getPosition().y;
            if (y < OverworldUtils.getCaveY() && camera.getEntity().level().dimension() == OtherWorld.LEVEL) {
                return Math.max(Mth.lerp(Mth.clamp(OverworldUtils.getCaveY() - (float) y, 0, 7) / 7, 0, 0.1F), original);
            }
        }
        return original;
    }
}
