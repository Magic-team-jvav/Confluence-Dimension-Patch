package org.mesdag.confluence_dimension_patch;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.confluence.lib.util.LibUtils;
import org.mesdag.confluence_dimension_patch.client.CDPClientEvents;
import org.mesdag.confluence_dimension_patch.common.*;
import org.mesdag.confluence_dimension_patch.common.worldgen.OtherWorldChunkGenerator;
import org.mesdag.confluence_dimension_patch.common.worldgen.TerrariaUnderworldGenerator;
import org.mesdag.portlib.client.gui.PortConfigurationScreen;
import org.mesdag.portlib.registries.PortDeferredItem;
import org.mesdag.portlib.registries.PortItemRegistration;
import org.mesdag.portlib.registries.PortRegisterHandler;

@Mod(ConfluenceDimensionPatch.MODID)
public class ConfluenceDimensionPatch {
    public static final String MODID = "confluence_dimension_patch";

    private static final PortItemRegistration ITEMS = PortRegisterHandler.item(MODID);
    public static final PortDeferredItem<TerrariumItem> TERRARIUM = ITEMS.register("terrarium", TerrariumItem::new);

    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);
    public static final RegistryObject<PlacementModifierType<OnlyInOtherworldPlacementFilter>> ONLY_IN_OTHERWORLD_PLACEMENT_FILTER = PLACEMENT_MODIFIER_TYPES.register("only_in_otherworld", () -> () -> OnlyInOtherworldPlacementFilter.CODEC);

    private static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, MODID);
    public static final RegistryObject<StructurePlacementType<OnlyInOtherworldStructurePlacement>> ONLY_IN_OTHERWORLD_STRUCTURE_PLACEMENT = STRUCTURE_PLACEMENT_TYPES.register("only_in_otherworld", () -> () -> OnlyInOtherworldStructurePlacement.CODEC);

    private static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATOR_TYPES = DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);
    public static final RegistryObject<Codec<OtherWorldChunkGenerator>> OTHERWORLD_CHUNK_GENERATOR = CHUNK_GENERATOR_TYPES.register("terraria", () -> OtherWorldChunkGenerator.CODEC);
    public static final RegistryObject<Codec<TerrariaUnderworldGenerator>> UNDERWORLD_CHUNK_GENERATOR = CHUNK_GENERATOR_TYPES.register("terraria_underworld", () -> TerrariaUnderworldGenerator.CODEC);

    public ConfluenceDimensionPatch(FMLJavaModLoadingContext context) {
        IEventBus eventBus = context.getModEventBus();
        ModContainer container = context.getContainer();
        CDPStartupConfigs.register();
        CDPCommonConfigs.register(context);
        CDPCommonEvents.init();
        if (LibUtils.isPhysicalClient()) {
            CDPClientEvents.init();
            container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new PortConfigurationScreen(container, screen)));
        }
        PLACEMENT_MODIFIER_TYPES.register(eventBus);
        STRUCTURE_PLACEMENT_TYPES.register(eventBus);
        CHUNK_GENERATOR_TYPES.register(eventBus);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
