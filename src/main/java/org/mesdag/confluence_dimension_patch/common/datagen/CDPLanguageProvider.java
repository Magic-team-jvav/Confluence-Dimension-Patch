package org.mesdag.confluence_dimension_patch.common.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;
import org.mesdag.confluence_dimension_patch.ConfluenceDimensionPatch;

public class CDPLanguageProvider extends LanguageProvider {
    private final boolean isEn;

    public CDPLanguageProvider(PackOutput output, boolean isEn) {
        super(output, ConfluenceDimensionPatch.MODID, isEn ? "en_us" : "zh_cn");
        this.isEn = isEn;
    }

    @Override
    protected void addTranslations() {
        add(ConfluenceDimensionPatch.TERRARIUM.get(), "Terrarium", "盒中泰拉");
        add("tooltip.item.confluence.terrarium.0", "Minecraft", "我的世界");
        add("tooltip.item.confluence.terrarium.1", "Confluence: Otherworld", "汇流来世");
        add("tooltip.item.confluence.terrarium.2", "Currently In", "目前位于");
        add("message.confluence_dimension_patch.unavailable", "You can't use it in current dimension!", "你不能在当前维度使用它！");
        add("confluence_dimension_patch.configuration.initialTerrarium", "Initial Terrarium", "初始盒中泰拉");
        add("confluence_dimension_patch.configuration.initialTerrarium.tooltip", "After opening, the opening will give the player a Terrarium by default", "开启后开局将默认给予玩家一个盒中泰拉");
        add("confluence_dimension_patch.configuration.title", "Confluence Dimension Patch", "汇流来世维度补丁");
    }

    public void add(Item key, String en, String zh) {
        super.add(key, isEn ? en : zh);
    }

    public void add(String key, String en, String zh) {
        super.add(key, isEn ? en : zh);
    }
}
