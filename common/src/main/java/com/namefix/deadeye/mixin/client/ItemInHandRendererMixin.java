package com.namefix.deadeye.mixin.client;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.data.PlayerDeadeyeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Shadow @Final private Minecraft minecraft;
	@Shadow private ItemStack mainHandItem;
	@Shadow private ItemStack offHandItem;

	@Inject(method = "itemUsed(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
	private void deadeye$cancelUseAnimation(InteractionHand hand, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player == null) return;
		if(!DeadeyeClient.DEADEYE_ENABLED) return;
		if(DeadeyeClient.DEADEYE_STATE.phase != PlayerDeadeyeState.Phase.SHOOTING) return;
		ci.cancel();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void deadeye$ignoreMinorItemDifferences(CallbackInfo ci) {
		if(this.minecraft.player == null) return;
		boolean inDeadeye = DeadeyeClient.DEADEYE_ENABLED && DeadeyeClient.DEADEYE_STATE.phase == PlayerDeadeyeState.Phase.SHOOTING;
		if(!inDeadeye) return;

		ItemStack currentMain = this.minecraft.player.getMainHandItem();
		if(!this.mainHandItem.isEmpty() && !currentMain.isEmpty() && this.mainHandItem.getItem() == currentMain.getItem()) {
			if(this.mainHandItem.getCount() == currentMain.getCount()) {
				this.mainHandItem = currentMain;
			}
		}

		ItemStack currentOff = this.minecraft.player.getOffhandItem();
		if(!this.offHandItem.isEmpty() && !currentOff.isEmpty() && this.offHandItem.getItem() == currentOff.getItem()) {
			if(this.offHandItem.getCount() == currentOff.getCount()) {
				this.offHandItem = currentOff;
			}
		}
	}
}
