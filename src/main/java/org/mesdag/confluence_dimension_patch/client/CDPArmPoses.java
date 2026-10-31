package org.mesdag.confluence_dimension_patch.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public final class CDPArmPoses {
    public static final EnumProxy<HumanoidModel.ArmPose> TERRARIUM = new EnumProxy<>(HumanoidModel.ArmPose.class, false, (IArmPoseTransformer)
            (model, living, arm) -> {
                if (living.isUsingItem()) {
                    if (arm == HumanoidArm.RIGHT) {
                        model.rightArm.xRot = -Mth.PI * 0.6F;
                        model.rightArm.yRot = model.head.yRot - Mth.PI * 0.2F;
                    }
                }
            });
}
