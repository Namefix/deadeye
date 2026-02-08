package com.namefix.item;

import com.namefix.client.DeadeyeSound;
import com.namefix.server.DeadeyeServer;
import com.namefix.shader.ShaderManager;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TonicItem extends Item {
	private final int tonicLevel;

	public TonicItem(Properties properties, int tonicLevel) {
		super(properties);
		this.tonicLevel = tonicLevel;
	}

	@Override
	public @NotNull ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		if(!(livingEntity instanceof Player player)) return ItemStack.EMPTY;

		if(!level.isClientSide) {
			CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, itemStack);
			DeadeyeServer.drinkTonic((ServerPlayer) player, tonicLevel);
		} else {
			ShaderManager.activateShader("tonic");
			ShaderManager.setTonicDuration(1.0f);
			DeadeyeSound.playConsumeTonic();
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		itemStack.consume(1, player);

		if(!player.hasInfiniteMaterials()) {
			if(itemStack.isEmpty()) {
				return new ItemStack(Items.GLASS_BOTTLE);
			}

			player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
		}

		player.gameEvent(GameEvent.DRINK);
		return itemStack;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		return ItemUtils.startUsingInstantly(level, player, interactionHand);
	}

	@Override
	public @NotNull UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.DRINK;
	}

	@Override
	public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
		return 32;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		switch (tonicLevel) {
			case 1 -> list.add(Component.translatable("item.deadeye.snake_oil.tooltip").withStyle(ChatFormatting.GRAY));
			case 2 -> list.add(Component.translatable("item.deadeye.potent_snake_oil.tooltip").withStyle(ChatFormatting.GRAY));
			case 3 -> list.add(Component.translatable("item.deadeye.special_snake_oil.tooltip").withStyle(ChatFormatting.GRAY));
		}

		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
	}
}
