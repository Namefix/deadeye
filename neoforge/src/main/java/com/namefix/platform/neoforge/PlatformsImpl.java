package com.namefix.platform.neoforge;

import com.namefix.platform.RenderEvents;

public class PlatformsImpl {
	public static RenderEvents getRenderEvents() {
		return new RenderEventsImpl();
	}
}
