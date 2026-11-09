package org.mesdag.confluence_dimension_patch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.lib.util.LibUtils;
import org.mesdag.confluence_dimension_patch.common.CDPCommonConfigs;
import org.mesdag.confluence_dimension_patch.common.OnlyInOtherworldPlacementFilter;
import org.mesdag.confluence_dimension_patch.common.OnlyInOtherworldStructurePlacement;
import org.mesdag.confluence_dimension_patch.common.TerrariumItem;
import org.mesdag.confluence_dimension_patch.common.worldgen.OtherWorldChunkGenerator;
import org.mesdag.confluence_dimension_patch.common.worldgen.TerrariaUnderworldGenerator;

@Mod(ConfluenceDimensionPatch.MODID)
public class ConfluenceDimensionPatch {
    public static final String MODID = "confluence_dimension_patch";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<TerrariumItem> TERRARIUM = ITEMS.register("terrarium", TerrariumItem::new);

    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<OnlyInOtherworldPlacementFilter>> ONLY_IN_OTHERWORLD_PLACEMENT_FILTER = PLACEMENT_MODIFIER_TYPES.register("only_in_otherworld", () -> () -> OnlyInOtherworldPlacementFilter.CODEC);

    private static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, MODID);
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<OnlyInOtherworldStructurePlacement>> ONLY_IN_OTHERWORLD_STRUCTURE_PLACEMENT = STRUCTURE_PLACEMENT_TYPES.register("only_in_otherworld", () -> () -> OnlyInOtherworldStructurePlacement.CODEC);

    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATOR_TYPES = DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<OtherWorldChunkGenerator>> OTHERWORLD_CHUNK_GENERATOR = CHUNK_GENERATOR_TYPES.register("terraria", () -> OtherWorldChunkGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<TerrariaUnderworldGenerator>> UNDERWORLD_CHUNK_GENERATOR = CHUNK_GENERATOR_TYPES.register("terraria_underworld", () -> TerrariaUnderworldGenerator.CODEC);

    public ConfluenceDimensionPatch(IEventBus eventBus, ModContainer container) {
        CDPStartupConfigs.register(container);
        CDPCommonConfigs.register(container);
        if (LibUtils.isPhysicalClient()) {
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
        ITEMS.register(eventBus);
        PLACEMENT_MODIFIER_TYPES.register(eventBus);
        STRUCTURE_PLACEMENT_TYPES.register(eventBus);
        CHUNK_GENERATOR_TYPES.register(eventBus);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
