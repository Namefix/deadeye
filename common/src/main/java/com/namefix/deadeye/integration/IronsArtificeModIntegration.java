package com.namefix.deadeye.integration;

import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.interactions.AbstractDeadeyeInteraction;
import com.namefix.deadeye.interactions.IronsArtificeDeadeyeInteraction;
import com.namefix.deadeye.platform.IronsArtificeIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class IronsArtificeModIntegration extends BaseModIntegration {
	public static final IronsArtificeModIntegration INSTANCE = new IronsArtificeModIntegration();

	private IronsArtificeModIntegration() {
		super("irons_artifice");
	}

	@Override
	public boolean matches(ItemStack itemStack) {
		return IronsArtificeIntegration.isGun(itemStack);
	}

	@Override
	public AbstractDeadeyeInteraction createInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		return new IronsArtificeDeadeyeInteraction(state, player, itemStack);
	}
}
