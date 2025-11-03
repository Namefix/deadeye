package com.namefix.platform.fabric;

import com.namefix.client.DeadeyeClient;
import com.namefix.platform.RenderEvents;
import com.namefix.shader.ShaderManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class RenderEventsImpl implements RenderEvents {
	@Override
	public void registerRenderLevelLastEvent() {
		WorldRenderEvents.END.register(context -> {
			DeadeyeClient.render(context.tickCounter().getRealtimeDeltaTicks());
			ShaderManager.renderActiveShaders(context.tickCounter().getRealtimeDeltaTicks());
		});
	}
}
