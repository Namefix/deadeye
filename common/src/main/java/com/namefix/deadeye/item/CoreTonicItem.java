package com.namefix.deadeye.item;

import com.namefix.deadeye.client.DeadeyeSound;
import com.namefix.deadeye.config.DeadeyeConfig;
import com.namefix.deadeye.data.PlayerSavedData;
import com.namefix.deadeye.shader.ShaderManager;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class CoreTonicItem extends Item {
	public final float coreAmount;
	public float meterAmount = 0f;
	public final boolean isEaten;

	public CoreTonicItem(Properties properties, float coreAmount, boolean isEaten) {
		super(properties);
		this.coreAmount = coreAmount;
		this.isEaten = isEaten;
	}

	public CoreTonicItem(Properties properties, float coreAmount, float meterAmount, boolean isEaten) {
		super(properties);
		this.coreAmount = coreAmount;
		this.meterAmount = meterAmount;
		this.isEaten = isEaten;
	}

	@Override
	public @NotNull ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		if(!(livingEntity instanceof Player player)) return ItemStack.EMPTY;

		if(!level.isClientSide) {
			CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, itemStack);
			PlayerSavedData.setDeadeyeCore((ServerPlayer) player, coreAmount);
			if(meterAmount != 0f) PlayerSavedData.addDeadeyeMeter((ServerPlayer) player, meterAmount, false);
		} else {
			if(DeadeyeConfig.Client.enableShaders) {
				ShaderManager.activateShader("tonic");
				ShaderManager.setTonicDuration(1.0f);
			}
			DeadeyeSound.playConsumeTonic();
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		itemStack.consume(1, player);

		player.gameEvent(isEaten ? GameEvent.EAT : GameEvent.DRINK);
		return itemStack;
	}

	@Override
	public @NotNull UseAnim getUseAnimation(ItemStack itemStack) {
		return isEaten ? UseAnim.EAT : UseAnim.DRINK;
	}

	@Override
	public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
		return 32;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		return ItemUtils.startUsingInstantly(level, player, interactionHand);
	}

}
