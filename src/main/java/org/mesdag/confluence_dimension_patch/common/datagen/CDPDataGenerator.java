package org.mesdag.confluence_dimension_patch.common.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.confluence.mod.common.init.ModBiomes;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;
import org.mesdag.confluence_dimension_patch.common.OtherWorld;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = ConfluenceDimensionPatch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CDPDataGenerator {
    private static final RegistrySetBuilder DATA_BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, OtherWorld::dimensionType)
            .add(Registries.NOISE_SETTINGS, OtherWorld::noiseSettings)
            .add(Registries.LEVEL_STEM, OtherWorld::levelStem)
            .add(Registries.BIOME, CDPDataGenerator::biomePlaceHolder);

    private static void biomePlaceHolder(BootstapContext<Biome> context) {
        Biome placeHolder = new Biome.BiomeBuilder().temperature(0).downfall(0).specialEffects(new BiomeSpecialEffects.Builder().fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).foliageColorOverride(0).build()).mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(new BiomeGenerationSettings.Builder(context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER)).build()).build();
        context.register(ModBiomes.ASH_FOREST, placeHolder);
        context.register(ModBiomes.ASH_WASTELAND, placeHolder);
    }

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
        generator.addProvider(server, new CDPRecipeProvider(output));
    }
}
