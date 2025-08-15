package com.namefix.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.namefix.DeadeyeMod;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class UniformManager {
	private final Map<String, Integer> uniformLocation = new HashMap<>();
	private int shaderProgram;

	public UniformManager(int shaderProgram) {
		this.shaderProgram = shaderProgram;
	}

	public void cacheUniformLocation(String uniformName) {
		int location = GL20.glGetUniformLocation(shaderProgram, uniformName);
		if(location == -1) DeadeyeMod.LOGGER.debug("Uniform {} not found in shader program", uniformName);
		uniformLocation.put(uniformName, location);
	}

	public void setUniform1f(String name, float value) {
		Integer location = uniformLocation.get(name);
		if(location != null && location != -1) {
			GL20.glUniform1f(location, value);
		}
	}

	public void setUniform2f(String name, float x, float y) {
		Integer location = uniformLocation.get(name);
		if(location != null && location != -1) {
			GL20.glUniform2f(location, x, y);
		}
	}

	public void setUniform3f(String name, float x, float y, float z) {
		Integer location = uniformLocation.get(name);
		if(location != null && location != -1) {
			GL20.glUniform3f(location, x, y, z);
		}
	}

	public void setUniform4f(String name, float x, float y, float z, float w) {
		Integer location = uniformLocation.get(name);
		if(location != null && location != -1) {
			GL20.glUniform4f(location, x, y, z, w);
		}
	}

	public void setUniform1i(String name, int value) {
		Integer location = uniformLocation.get(name);
		if (location != null && location != -1) {
			GL20.glUniform1i(location, value);
		}
	}

	public void setUniformMatrix4f(String name, FloatBuffer matrix) {
		Integer location = uniformLocation.get(name);
		if (location != null && location != -1) {
			GL20.glUniformMatrix4fv(location, false, matrix);
		}
	}

}
