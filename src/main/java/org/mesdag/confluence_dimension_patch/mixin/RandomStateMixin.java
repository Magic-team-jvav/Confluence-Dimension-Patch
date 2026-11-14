package org.mesdag.confluence_dimension_patch.mixin;

import net.minecraft.world.level.levelgen.RandomState;
import org.mesdag.confluence_dimension_patch.mixed.IDimensionAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RandomState.class)
public abstract class RandomStateMixin implements IDimensionAccessor {
    @Unique
    private boolean cdp$isOtherworld;

    @Override
    public void cdp$setIsOtherworld() {
        this.cdp$isOtherworld = true;
    }

    @Override
    public boolean cdp$isOtherworld() {
        return cdp$isOtherworld;
    }
}
