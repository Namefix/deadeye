package com.namefix.shader;

import com.namefix.DeadeyeMod;
import com.namefix.platform.Platforms;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ShaderManager {
	private static final Map<String, ShaderEffect> LOADED_SHADERS = new HashMap<>();
	private static final Map<String, Boolean> ACTIVE_SHADERS = new HashMap<>();
	public static final Map<String, UniformManager> UNIFORM_MANAGERS = new HashMap<>();

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
		ShaderManager.registerShader("rdr2_deadeye", ResourceLocation.withDefaultNamespace("shaders/post/rdr2_deadeye.json.json"));
	}

	public static void registerShader(String name, ResourceLocation shaderLocation) {
		ACTIVE_SHADERS.put(name, false);
	}

	public static void renderActiveShaders(float partialTicks) {
		for(ShaderEffect shader : LOADED_SHADERS.values()) {
			if(shader.isActive()) {
				updateCommonUniforms(shader.getName());
				shader.render(partialTicks);
			}
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

			if(UNIFORM_MANAGERS.containsKey(name)) {
				initalizeUniforms(name, shader);
			}
		}
	}

	private static void initalizeUniforms(String shaderName, ShaderEffect shader) {
		int programId = getShaderProgramId(shader);
		UniformManager uniformManager = new UniformManager(programId);

		uniformManager.cacheUniformLocation("Time");

		UNIFORM_MANAGERS.put(shaderName, uniformManager);
	}

	public static UniformManager getUniformManager(String shaderName) {
		return UNIFORM_MANAGERS.get(shaderName);
	}

	public static void setUniform(String shaderName, String uniformName, Object value) {
		UniformManager manager = UNIFORM_MANAGERS.get(shaderName);
		if (manager == null) return;

		if (value instanceof Float) {
			manager.setUniform1f(uniformName, (Float) value);
		} else if (value instanceof Integer) {
			manager.setUniform1i(uniformName, (Integer) value);
		} else if (value instanceof Vector2f v) {
			manager.setUniform2f(uniformName, v.x, v.y);
		} else if (value instanceof Vector3f v) {
			manager.setUniform3f(uniformName, v.x, v.y, v.z);
		} else if (value instanceof Vector4f v) {
			manager.setUniform4f(uniformName, v.x, v.y, v.z, v.w);
		}
	}

	public static void updateCommonUniforms(String shaderName) {
		UniformManager manager = UNIFORM_MANAGERS.get(shaderName);
		if (manager == null) return;

		Minecraft mc = Minecraft.getInstance();

		// Time
		float time = (System.currentTimeMillis() % 1000000) / 1000.0f;
		manager.setUniform1f("Time", time);
	}

	private static int getShaderProgramId(ShaderEffect shader) {
		try {
			Field programField = shader.getClass().getDeclaredField("program"); // Adjust field name
			programField.setAccessible(true);
			return (Integer) programField.get(shader);
		} catch (Exception e) {
			DeadeyeMod.LOGGER.debug("Failed to get shader program ID: {}", e.getMessage());
			return -1;
		}
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
