package com.namefix.deadeye.shader;

import com.namefix.deadeye.DeadeyeMod;
import com.namefix.deadeye.platform.Platforms;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

public class ShaderManager {
	private static final Map<String, ShaderEffect> LOADED_SHADERS = new HashMap<>();
	private static final Map<String, Boolean> ACTIVE_SHADERS = new HashMap<>();
	private static float TONIC_DURATION = 0.0f;

	public static void initialize() {
		if(Platform.isFabric()) {
			ClientLifecycleEvent.CLIENT_STOPPING.register(minecraft -> {
				ShaderManager.cleanup();
			});
		}

		registerShaders();

		Platforms.getRenderEvents().registerRenderLevelLastEvent();
	}

	private static void registerShaders() {
		ShaderManager.registerShader("rdr2_deadeye", ResourceLocation.withDefaultNamespace("shaders/post/rdr2_deadeye.json"));
		ShaderManager.registerShader("tonic", ResourceLocation.withDefaultNamespace("shaders/post/tonic.json"));
	}

	public static void registerShader(String name, ResourceLocation shaderLocation) {
		ACTIVE_SHADERS.put(name, false);
	}

	public static void renderActiveShaders(float partialTicks) {
		updateTonicTimer(partialTicks);
		for(ShaderEffect shader : LOADED_SHADERS.values()) {
			if(shader.isActive()) {
				updateCommonUniforms(shader);
				shader.render(partialTicks);
			}
		}
	}

	public static void updateTonicTimer(float realTimeDeltaTicks) {
		TONIC_DURATION = Mth.clamp(TONIC_DURATION - (realTimeDeltaTicks / 20.0f), 0.0f, 1.0f);
		if(isShaderActive("tonic")) {
			setUniform("tonic", "TonicDuration", TONIC_DURATION);
		}
		if(TONIC_DURATION <= 0.0f) {
			deactivateShader("tonic");
		}
	}

	public static void setTonicDuration(float value) {
		TONIC_DURATION = Mth.clamp(value, 0.0f, 1.0f);
		if(isShaderActive("tonic")) {
			setUniform("tonic", "TonicDuration", TONIC_DURATION);
		}
	}

	public static void toggleShader(String name) {
		if (isShaderActive(name)) {
			deactivateShader(name);
		} else {
			activateShader(name);
		}
	}


	public static void activateShader(String name) {
		if(!ACTIVE_SHADERS.containsKey(name)) throw new IllegalArgumentException("Shader " + name + " is not registered!");
		if(!LOADED_SHADERS.containsKey(name)) loadShader(name);

		ShaderEffect shader = LOADED_SHADERS.get(name);
		if(shader != null) {
			var mc = Minecraft.getInstance().getWindow();

			shader.activate();
			shader.resize(mc.getWidth(), mc.getHeight());
			ACTIVE_SHADERS.put(name, true);
		}
	}

	public static void setUniform(String shaderName, String uniformName, Object value) {
		ShaderEffect shader = LOADED_SHADERS.get(shaderName);
		if(shader == null) return;
		shader.setUniform(uniformName, value);
	}

	private static void updateCommonUniforms(ShaderEffect shader) {
		if(shader == null) return;
		float time = (System.currentTimeMillis() % 1000000) / 1000.0f;
		shader.setUniform("Time", time);
	}

	public static void deactivateShader(String name) {
		if(ACTIVE_SHADERS.containsKey(name)) {
			ShaderEffect shader = LOADED_SHADERS.get(name);
			if(shader != null) shader.deactivate();

			ACTIVE_SHADERS.put(name, false);
		}
	}

	public static boolean isShaderActive(String name) {
		return ACTIVE_SHADERS.getOrDefault(name, false);
	}

	private static void loadShader(String name) {
		try {
			ShaderEffect shader = new ShaderEffect(name);
			if(shader.load()) LOADED_SHADERS.put(name, shader);
		} catch (Exception e) {
			DeadeyeMod.LOGGER.error("Failed to load shader: {}", name, e);
		}
	}

	public static void unloadShader(String name) {
		ShaderEffect shader = LOADED_SHADERS.remove(name);
		if(shader != null) shader.cleanup();
		ACTIVE_SHADERS.put(name, false);
	}

	public static void cleanup() {
		LOADED_SHADERS.values().forEach(ShaderEffect::cleanup);
		LOADED_SHADERS.clear();
		ACTIVE_SHADERS.clear();
	}
}
