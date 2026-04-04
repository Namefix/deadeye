package com.namefix.deadeye.fabric.mixin.integration.pointblank;

import com.mojang.datafixers.util.Pair;
import com.namefix.deadeye.client.DeadeyeClient;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.vicmatskiv.pointblank.feature.FireModeFeature", remap = false)
public class PointBlankFireModeFeatureMixin {
	@Inject(method = "getPelletCountAndSpread", at = @At("HEAD"), cancellable = true)
	private static void deadeye$modifyPelletAmount(LivingEntity player, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Pair<Integer, Double>> cir) {
		if(!player.level().isClientSide || !DeadeyeClient.DEADEYE_ENABLED) return;

		Item var4 = itemStack.getItem();
		if (var4 instanceof GunItem gunItem) {
			FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
			cir.setReturnValue(fireModeInstance == null ? Pair.of(gunItem.getPelletCount(), 0.0) : Pair.of(fireModeInstance.getPelletCount(), 0.0));
		} else {
			cir.setReturnValue(Pair.of(0, 0.0));
		}
	}
}
