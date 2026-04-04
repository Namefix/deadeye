package com.namefix.deadeye.data;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerDeadeyeState {
	public enum Phase {
		IDLE,
		MARKED,
		SHOOTING
	}

	public Phase phase = Phase.IDLE;
	public List<DeadeyeTargetData> targets = new ArrayList<>();
	public ItemStack markItem;
}
