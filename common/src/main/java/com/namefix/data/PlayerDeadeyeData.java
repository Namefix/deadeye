package com.namefix.data;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerDeadeyeData {
	public enum State {
		IDLE,
		MARKED,
		SHOOTING
	}

	public State state = State.IDLE;
	public List<DeadeyeTargetData> targets = new ArrayList<>();
	public ItemStack markItem;
}
