package org.mesdag.confluence_dimension_patch.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.Event;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.init.ModTabs;
import org.confluence.mod.util.ModUtils;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;
import org.mesdag.portlib.event.PortEventHandler;
import org.mesdag.portlib.event.PortEventPriority;

public final class CDPCommonEvents {
    public static void init() {
        PortEventHandler.addListener(PortEventPriority.HIGHEST, CDPCommonEvents::mobSpawn$PositionCheck);
        PortEventHandler.addListener(CDPCommonEvents::advancement$earn);
        PortEventHandler.addListener(CDPCommonEvents::buildCreativeModeTabContents);
    }

    public static void mobSpawn$PositionCheck(MobSpawnEvent.PositionCheck event) {
        Mob mob = event.getEntity();
        if (mob.level().dimension() != OtherWorld.LEVEL && ModUtils.isFromConfluence(BuiltInRegistries.ENTITY_TYPE, mob.getType())) {
            event.setResult(Event.Result.DENY);
        }
    }

    public static void advancement$earn(AdvancementEvent.AdvancementEarnEvent event) {
        if (CDPCommonConfigs.INITIAL_TERRARIUM.get()) {
            ResourceLocation id = event.getAdvancement().getId();
            if (id.getNamespace().equals(Confluence.MODID) && id.getPath().endsWith("new_world")) {
                event.getEntity().addItem(ConfluenceDimensionPatch.TERRARIUM.toStack());
            }
        }
    }

    public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModTabs.TOOLS.getKey())) {
            event.accept(ConfluenceDimensionPatch.TERRARIUM.get());
        }
    }
}
