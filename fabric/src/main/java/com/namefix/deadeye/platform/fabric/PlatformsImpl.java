package com.namefix.deadeye.platform.fabric;

import com.namefix.deadeye.platform.RenderEvents;

public class PlatformsImpl {
	public static RenderEvents getRenderEvents() {
		return new RenderEventsImpl();
	}
}
