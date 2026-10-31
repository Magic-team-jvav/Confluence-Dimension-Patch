package org.mesdag.confluence_dimension_patch.common.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ConfluenceDimensionPatch.MODID)
public final class CDPDataGenerator {
    private static final RegistrySetBuilder DATA_BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, OtherWorld::dimensionType)
            .add(Registries.LEVEL_STEM, OtherWorld::levelStem);

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        boolean client = event.includeClient();
        generator.addProvider(client, new CDPItemModelProvider(output, helper));
        generator.addProvider(client, new CDPLanguageProvider(output, true));
        generator.addProvider(client, new CDPLanguageProvider(output, false));

        boolean server = event.includeServer();
        lookup = generator.addProvider(server, new DatapackBuiltinEntriesProvider(output, lookup, DATA_BUILDER, Set.of(ConfluenceDimensionPatch.MODID))).getRegistryProvider();
        generator.addProvider(server, new CDPRecipeProvider(output, lookup));
    }
}
