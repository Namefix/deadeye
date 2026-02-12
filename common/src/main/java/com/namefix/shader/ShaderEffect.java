package com.namefix.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.namefix.DeadeyeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class ShaderEffect {
	private static final Field PASSES_FIELD;

	static {
		Field field = null;
		try {
			field = PostChain.class.getDeclaredField("passes");
			field.setAccessible(true);
		} catch (NoSuchFieldException e) {
			field = findPassesFieldFallback();
			if(field == null) {
				DeadeyeMod.LOGGER.error("Failed to access PostChain passes", e);
			}
		}
		PASSES_FIELD = field;
	}

	private static Field findPassesFieldFallback() {
		for(Field candidate : PostChain.class.getDeclaredFields()) {
			if(!List.class.isAssignableFrom(candidate.getType())) continue;
			candidate.setAccessible(true);
			return candidate;
		}
		return null;
	}

	private final String name;
	private PostChain postChain;
	private List<PostPass> cachedPasses = Collections.emptyList();
	private boolean active = false;

	public ShaderEffect(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean load() {
		try {
			Minecraft mc = Minecraft.getInstance();
			ResourceLocation shaderLocation = ResourceLocation.withDefaultNamespace("shaders/post/" + name + ".json");

			this.postChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), shaderLocation);
			this.postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
			this.cachePasses();
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

			Minecraft mc = Minecraft.getInstance();
			mc.getMainRenderTarget().unbindWrite();

			postChain.process(partialTicks);

			mc.getMainRenderTarget().bindWrite(false);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
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
		cachedPasses = Collections.emptyList();
		active = false;
	}

	public void setUniform(String uniformName, Object value) {
		if(postChain == null || cachedPasses.isEmpty() || uniformName == null || value == null) return;
		for(PostPass pass : cachedPasses) {
			if(pass == null) continue;
			EffectInstance shaderInstance = pass.getEffect();
			if(shaderInstance == null) continue;
			Uniform uniform = shaderInstance.getUniform(uniformName);
			if(uniform == null) continue;
			applyUniform(uniform, value);
		}
	}

	private void applyUniform(Uniform uniform, Object value) {
		if(value instanceof Float f) {
			uniform.set(f);
		} else if(value instanceof Integer i) {
			uniform.set(i.floatValue());
		} else if(value instanceof Vector2f v) {
			uniform.set(v.x, v.y);
		} else if(value instanceof Vector3f v) {
			uniform.set(v.x, v.y, v.z);
		} else if(value instanceof Vector4f v) {
			uniform.set(v.x, v.y, v.z, v.w);
		} else {
			return;
		}
		uniform.upload();
	}

	@SuppressWarnings("unchecked")
	private void cachePasses() {
		if(postChain == null || PASSES_FIELD == null) {
			cachedPasses = Collections.emptyList();
			return;
		}
		try {
			List<PostPass> passes = (List<PostPass>) PASSES_FIELD.get(postChain);
			if(passes == null || passes.isEmpty()) {
				cachedPasses = Collections.emptyList();
			} else {
				cachedPasses = List.copyOf(passes);
			}
		} catch (IllegalAccessException e) {
			DeadeyeMod.LOGGER.error("Failed to cache passes for shader {}", name, e);
			cachedPasses = Collections.emptyList();
		}
	}
}
