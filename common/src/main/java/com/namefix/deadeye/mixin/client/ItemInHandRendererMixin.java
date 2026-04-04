package com.namefix.deadeye.mixin.client;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.data.PlayerDeadeyeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Inject(method = "itemUsed(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
	private void deadeye$cancelUseAnimation(InteractionHand hand, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player == null) return;
		if(!DeadeyeClient.DEADEYE_ENABLED) return;
		if(DeadeyeClient.DEADEYE_STATE.phase != PlayerDeadeyeState.Phase.SHOOTING) return;
		ci.cancel();
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
			ordinal = 0
		)
	)
	private boolean deadeye$ignoreMinorItemDifferencesMain(ItemStack previous, ItemStack current) {
		return deadeye$shouldIgnoreMinorItemDifferences(previous, current);
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
			ordinal = 1
		)
	)
	private boolean deadeye$ignoreMinorItemDifferencesOff(ItemStack previous, ItemStack current) {
		return deadeye$shouldIgnoreMinorItemDifferences(previous, current);
	}

	@Unique
	private static boolean deadeye$shouldIgnoreMinorItemDifferences(ItemStack previous, ItemStack current) {
		boolean inDeadeye = DeadeyeClient.DEADEYE_ENABLED && DeadeyeClient.DEADEYE_STATE.phase == PlayerDeadeyeState.Phase.SHOOTING;
		if(inDeadeye && !previous.isEmpty() && !current.isEmpty() && previous.getItem() == current.getItem()) {
			return previous.getCount() == current.getCount();
		}
		return ItemStack.matches(previous, current);
	}
}
