package org.mesdag.confluence_dimension_patch.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public final class CDPArmPoses {
    public static void init() {}

    public static final HumanoidModel.ArmPose TERRARIUM = HumanoidModel.ArmPose.create("CONFLUENCE_DIMENSION_PATCH_TERRARIUM", false,
            (model, living, arm) -> {
                if (living.isUsingItem()) {
                    if (arm == HumanoidArm.RIGHT) {
                        model.rightArm.xRot = -Mth.PI * 0.6F;
                        model.rightArm.yRot = model.head.yRot - Mth.PI * 0.2F;
                    }
                }
            });
}
