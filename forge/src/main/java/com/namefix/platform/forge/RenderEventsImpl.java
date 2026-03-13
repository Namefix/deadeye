package com.namefix.platform.forge;

import com.namefix.client.DeadeyeClient;
import com.namefix.platform.RenderEvents;
import com.namefix.shader.ShaderManager;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderEventsImpl implements RenderEvents {
	@Override
	public void registerRenderLevelLastEvent() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRenderLevelLast(RenderLevelStageEvent event) {
		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
			DeadeyeClient.render();
			ShaderManager.renderActiveShaders(event.getPartialTick());
		}
	}
}
