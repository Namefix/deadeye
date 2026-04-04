package com.namefix.deadeye.platform.neoforge;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.platform.RenderEvents;
import com.namefix.deadeye.shader.ShaderManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public class RenderEventsImpl implements RenderEvents {
	@Override
	public void registerRenderLevelLastEvent() {
		NeoForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRenderLevelLast(RenderLevelStageEvent event) {
		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
			DeadeyeClient.render();
			ShaderManager.renderActiveShaders(event.getPartialTick().getRealtimeDeltaTicks());
		}
	}
}
