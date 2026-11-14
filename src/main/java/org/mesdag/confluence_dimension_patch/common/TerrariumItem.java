package org.mesdag.confluence_dimension_patch.common;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
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
import net.minecraft.world.level.portal.DimensionTransition;
import org.confluence.lib.common.component.ModRarity;
import org.confluence.lib.common.item.CustomRarityItem;

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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            if (level.dimension() != Level.OVERWORLD && level.dimension() != OtherWorld.LEVEL) {
                serverPlayer.sendSystemMessage(Component.translatable("message.confluence_dimension_patch.unavailable").withStyle(ChatFormatting.GOLD), true);
                serverPlayer.getCooldowns().addCooldown(this, 20);
                return stack;
            }
            if (serverPlayer.getVehicle() != null) {
                serverPlayer.removeVehicle();
            }
            serverPlayer.getCooldowns().addCooldown(this, 20);
            ServerLevel serverlevel = serverPlayer.server.getLevel(level.dimension() == OtherWorld.LEVEL ? Level.OVERWORLD : OtherWorld.LEVEL);
            if (serverlevel != null) {
                serverPlayer.changeDimension(new DimensionTransition(
                        serverlevel,
                        serverPlayer.position().add(0, 0.1, 0),
                        serverPlayer.getDeltaMovement(),
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot(),
                        entity -> {
                            if (entity instanceof ServerPlayer serverplayer) {
                                serverplayer.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
                            }
                        }
                ));
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int dimension = context.level() == null ? 0 : context.level().dimension() == OtherWorld.LEVEL ? 1 : 0;
        tooltipComponents.add(Component.translatable("tooltip.item.confluence.terrarium.2"));
        tooltipComponents.add(Component.translatable("tooltip.item.confluence.terrarium." + dimension).withStyle(ChatFormatting.AQUA));
    }
}
