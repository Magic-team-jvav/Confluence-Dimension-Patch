package org.mesdag.confluence_dimension_patch;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.mod.common.init.ModTabs;
import org.mesdag.confluence_dimension_patch.common.CDPCommonConfigs;
import org.mesdag.confluence_dimension_patch.common.OnlyInOtherworldPlacementFilter;
import org.mesdag.confluence_dimension_patch.common.OnlyInOtherworldStructurePlacement;
import org.mesdag.confluence_dimension_patch.common.TerrariumItem;

@Mod(ConfluenceDimensionPatch.MODID)
public class ConfluenceDimensionPatch {
    public static final String MODID = "confluence_dimension_patch";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<TerrariumItem> TERRARIUM = ITEMS.register("terrarium", TerrariumItem::new);

    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<OnlyInOtherworldPlacementFilter>> ONLY_IN_OTHERWORLD_PLACEMENT_FILTER = PLACEMENT_MODIFIER_TYPES.register("only_in_otherworld", () -> () -> OnlyInOtherworldPlacementFilter.CODEC);

    private static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, MODID);
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<OnlyInOtherworldStructurePlacement>> ONLY_IN_OTHERWORLD_STRUCTURE_PLACEMENT = STRUCTURE_PLACEMENT_TYPES.register("only_in_otherworld", () -> () -> OnlyInOtherworldStructurePlacement.CODEC);

    public ConfluenceDimensionPatch(IEventBus eventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CDPCommonConfigs.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
        ITEMS.register(eventBus);
        PLACEMENT_MODIFIER_TYPES.register(eventBus);
        STRUCTURE_PLACEMENT_TYPES.register(eventBus);
        eventBus.addListener(this::buildCreativeModeTabContents);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModTabs.TOOLS.getKey()) {
            event.accept(TERRARIUM);
        }
    }
}
