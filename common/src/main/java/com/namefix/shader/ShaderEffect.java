package com.namefix.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.namefix.DeadeyeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class ShaderEffect {
	private final String name;
	private PostChain postChain;
	private boolean active = false;

	public ShaderEffect(String name) {
		this.name = name;
	}

	public boolean load() {
		try {
			Minecraft mc = Minecraft.getInstance();
			ResourceLocation shaderLocation = ResourceLocation.withDefaultNamespace("shaders/post/" + name + ".json");

			this.postChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), shaderLocation);
			this.postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
			return true;
		} catch (Exception e) {
			DeadeyeMod.LOGGER.error("Failed to load shader effect: " + name, e);
			return false;
		}
	}

	public void activate() {
		if(postChain != null && !active) {
			active = true;
		}
	}

	public void deactivate() {
		active = false;
	}

	public void render(float partialTicks) {
		if(active && postChain != null) {
			RenderSystem.disableBlend();
			RenderSystem.disableDepthTest();
			RenderSystem.resetTextureMatrix();
			postChain.process(partialTicks);
			Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
		}
	}

	public boolean isActive() {
		return active;
	}

	public void resize(int width, int height) {
		if(postChain != null) {
			postChain.resize(width, height);
		}
	}

	public void cleanup() {
		if(postChain != null) {
			postChain.close();
			postChain = null;
		}
		active = false;
	}
}
