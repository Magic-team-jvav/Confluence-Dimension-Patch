package org.mesdag.confluence_dimension_patch.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.init.ModTabs;
import org.confluence.mod.util.ModUtils;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;

@EventBusSubscriber(modid = ConfluenceDimensionPatch.MODID)
public final class CDPCommonEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mobSpawn$PositionCheck(MobSpawnEvent.PositionCheck event) {
        Mob mob = event.getEntity();
        if (mob.level().dimension() != OtherWorld.LEVEL && ModUtils.isFromConfluence(BuiltInRegistries.ENTITY_TYPE, mob.getType())) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    @SubscribeEvent
    public static void advancement$earn(AdvancementEvent.AdvancementEarnEvent event) {
        if (CDPCommonConfigs.INITIAL_TERRARIUM.get()) {
            ResourceLocation id = event.getAdvancement().id();
            if (id.getNamespace().equals(Confluence.MODID) && id.getPath().endsWith("new_world")) {
                event.getEntity().addItem(ConfluenceDimensionPatch.TERRARIUM.toStack());
            }
        }
    }

    @SubscribeEvent
    public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModTabs.TOOLS.getKey()) {
            event.accept(ConfluenceDimensionPatch.TERRARIUM);
        }
    }
}
