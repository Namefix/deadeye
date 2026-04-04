package com.namefix.deadeye.platform.forge;

import com.namefix.deadeye.platform.RenderEvents;

public class PlatformsImpl {
	public static RenderEvents getRenderEvents() {
		return new RenderEventsImpl();
	}
}
