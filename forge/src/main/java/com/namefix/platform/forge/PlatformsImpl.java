package com.namefix.platform.forge;

import com.namefix.platform.RenderEvents;

public class PlatformsImpl {
	public static RenderEvents getRenderEvents() {
		return new RenderEventsImpl();
	}
}
