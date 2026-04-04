package com.namefix.deadeye.platform.neoforge;

import com.namefix.deadeye.platform.RenderEvents;

public class PlatformsImpl {
	public static RenderEvents getRenderEvents() {
		return new RenderEventsImpl();
	}
}
