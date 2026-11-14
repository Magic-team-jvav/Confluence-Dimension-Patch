package org.mesdag.confluence_dimension_patch.common.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;

import java.util.function.Consumer;

public class CDPRecipeProvider extends RecipeProvider {
    public CDPRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ConfluenceDimensionPatch.TERRARIUM)
                .define('A', Ingredient.of(Tags.Items.GLASS_PANES))
                .define('B', Ingredient.of(ItemTags.SAPLINGS))
                .define('C', Ingredient.of(ItemTags.PLANKS))
                .pattern(" A ")
                .pattern("ABA")
                .pattern("CCC")
                .unlockedBy("has_sapling", has(ItemTags.SAPLINGS))
                .save(writer);
    }
}
