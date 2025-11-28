package com.namefix.util;

import com.namefix.data.DeadeyeTargetData;

import static com.namefix.client.DeadeyeClient.DEADEYE_STATE;

public class ClientUtils {
	public static DeadeyeTargetData getNextValidTarget() {
		while(!DEADEYE_STATE.targets.isEmpty()) {
			DeadeyeTargetData candidate = DEADEYE_STATE.targets.getFirst();
			if(candidate != null && !candidate.isInvalid()) return candidate;
			DEADEYE_STATE.targets.removeFirst();
		}
		return null;
	}
}
