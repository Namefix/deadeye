package com.namefix.interactions;

import com.namefix.data.PlayerDeadeyeState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/*
	- Dead Eye Interactions -
	This class is made to simplify adding mod support and not altering the main code.
	There are "hooks" that provide a way to cancel marks/shots if the current held interaction is not ready for it.
	Example: An interaction for a weapon inside a gun mod, the ammo count in the current magazine should not pass the total mark count.

 */
public abstract class AbstractDeadeyeInteraction {
	protected final PlayerDeadeyeState state;
	protected final Player player;
	protected final ItemStack itemStack;

	public AbstractDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		this.state = state;
		this.player = player;
		this.itemStack = itemStack;
	}

	// Before mark request is processed. Cancellable.
	public abstract boolean preMark();
	// After the mark request is processed.
	public abstract void postMark();

	// Before the shooting request is processed. Cancellable.
	public abstract boolean preShot();
	// Process interaction shot
	public abstract void shoot();
	// After the shooting request, flag indicates whether more targets remain client-side.
	public abstract void postShot(boolean hasMoreTargets);
}
