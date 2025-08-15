package com.namefix.platform.fabric;

import com.namefix.platform.RenderEvents;
import com.namefix.shader.ShaderManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class RenderEventsImpl implements RenderEvents {
	@Override
	public void registerRenderLevelLastEvent() {
		WorldRenderEvents.END.register(context -> {
			ShaderManager.renderActiveShaders(context.tickCounter().getRealtimeDeltaTicks());
		});
	}
}
