package org.mesdag.confluence_dimension_patch.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;

import javax.annotation.ParametersAreNonnullByDefault;

@EventBusSubscriber(modid = ConfluenceDimensionPatch.MODID, value = Dist.CLIENT)
public final class CDPClientModEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
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
}
