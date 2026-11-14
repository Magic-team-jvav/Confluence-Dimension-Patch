package org.mesdag.confluence_dimension_patch.common;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.confluence.lib.common.component.ModRarity;
import org.confluence.lib.common.item.CustomRarityItem;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TerrariumItem extends CustomRarityItem {
    public TerrariumItem() {
        super(new Properties().stacksTo(1).fireResistant(), ModRarity.EXPERT);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return ItemUtils.startUsingInstantly(level, player, usedHand);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (living instanceof ServerPlayer player) {
            if (level.dimension() != Level.OVERWORLD && level.dimension() != OtherWorld.LEVEL) {
                player.sendSystemMessage(Component.translatable("message.confluence_dimension_patch.unavailable").withStyle(ChatFormatting.GOLD), true);
                player.getCooldowns().addCooldown(this, 20);
                return stack;
            }
            if (player.getVehicle() != null) {
                player.removeVehicle();
            }
            player.getCooldowns().addCooldown(this, 20);
            ServerLevel serverlevel = player.server.getLevel(level.dimension() == OtherWorld.LEVEL ? Level.OVERWORLD : OtherWorld.LEVEL);
            if (serverlevel != null) {
                player.teleportTo(serverlevel, player.getX(), player.getY() + 0.1, player.getZ(), player.getYRot(), player.getXRot());
            }
        }
        return stack;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int dimension = level == null ? 0 : level.dimension() == OtherWorld.LEVEL ? 1 : 0;
        tooltipComponents.add(Component.translatable("tooltip.item.confluence.terrarium.2"));
        tooltipComponents.add(Component.translatable("tooltip.item.confluence.terrarium." + dimension).withStyle(ChatFormatting.AQUA));
    }
}
