package com.namefix.deadeye.platform.fabric;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.platform.RenderEvents;
import com.namefix.deadeye.shader.ShaderManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class RenderEventsImpl implements RenderEvents {
	@Override
	public void registerRenderLevelLastEvent() {
		WorldRenderEvents.END.register(context -> {
			DeadeyeClient.render();
			ShaderManager.renderActiveShaders(context.tickCounter().getRealtimeDeltaTicks());
		});
	}
}
