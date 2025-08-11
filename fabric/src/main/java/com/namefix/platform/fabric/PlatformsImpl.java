package com.namefix.platform.fabric;

import com.namefix.platform.RenderEvents;

public class PlatformsImpl {
	public static RenderEvents getRenderEvents() {
		return new RenderEventsImpl();
	}
}
