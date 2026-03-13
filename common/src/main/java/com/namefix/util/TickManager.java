package com.namefix.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

public final class TickManager {
	private static final float DEFAULT_TICK_RATE = 20.0f;
	private static final float MAX_REALTIME_DELTA_TICKS = 7.0f;
	private static final float CLAMPED_REALTIME_DELTA_TICKS = 0.5f;
	private static final Map<MinecraftServer, Float> SERVER_TICK_RATE_CACHE = new WeakHashMap<>();

	private TickManager() {
	}

	public static float getTickRate(Level level) {
		if(level == null) return DEFAULT_TICK_RATE;
		MinecraftServer server = level.getServer();
		if(server == null) return DEFAULT_TICK_RATE;
		return getTickRate(server);
	}

	public static float getTickRate(MinecraftServer server) {
		if(server == null) return DEFAULT_TICK_RATE;
		synchronized (SERVER_TICK_RATE_CACHE) {
			return SERVER_TICK_RATE_CACHE.getOrDefault(server, DEFAULT_TICK_RATE);
		}
	}

	public static long getMillisecondsPerTick(MinecraftServer server) {
		float tickRate = getTickRate(server);
		if(tickRate <= 0.0f) return Math.round(1000.0f / DEFAULT_TICK_RATE);
		return Math.max(1L, Math.round(1000.0f / tickRate));
	}

	public static void setTickRate(Level level, float tickRate) {
		if(level == null) return;
		MinecraftServer server = level.getServer();
		if(server == null) return;

		float sanitizedTickRate = sanitizeTickRate(tickRate);
		synchronized (SERVER_TICK_RATE_CACHE) {
			if(sanitizedTickRate == DEFAULT_TICK_RATE) {
				SERVER_TICK_RATE_CACHE.remove(server);
			} else {
				SERVER_TICK_RATE_CACHE.put(server, sanitizedTickRate);
			}
		}
	}

	public static float getRealtimeDeltaTicks(Object minecraftInstance) {
		if(minecraftInstance == null) return 0.0f;

		Object timer = invokeNoArgObject(minecraftInstance, "getTimer");
		Float realtimeDelta = invokeNoArgFloat(timer, "getRealtimeDeltaTicks");
		if(realtimeDelta != null) {
			return realtimeDelta;
		}

		Float legacyDelta = invokeNoArgFloat(minecraftInstance, "getDeltaFrameTime");
		if(legacyDelta == null) return 0.0f;
		if(legacyDelta > MAX_REALTIME_DELTA_TICKS) return CLAMPED_REALTIME_DELTA_TICKS;
		return legacyDelta;
	}

	public static float getGameTimeDeltaPartialTick(Object minecraftInstance, boolean runsNormally) {
		if(minecraftInstance == null) return 0.0f;

		Object timer = invokeNoArgObject(minecraftInstance, "getTimer");
		Float partialTick = invokeBooleanArgFloat(timer, "getGameTimeDeltaPartialTick", runsNormally);
		if(partialTick != null) {
			return partialTick;
		}

		Float legacyPartialTick = invokeNoArgFloat(minecraftInstance, "getFrameTime");
		return legacyPartialTick != null ? legacyPartialTick : 0.0f;
	}

	private static float sanitizeTickRate(float tickRate) {
		if(!Float.isFinite(tickRate) || tickRate <= 0.0f) {
			return DEFAULT_TICK_RATE;
		}
		return tickRate;
	}

	private static Object invokeNoArgObject(Object instance, String methodName) {
		if(instance == null) return null;
		try {
			Method method = instance.getClass().getMethod(methodName);
			return method.invoke(instance);
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}

	private static Float invokeNoArgFloat(Object instance, String methodName) {
		if(instance == null) return null;
		try {
			Method method = instance.getClass().getMethod(methodName);
			Object result = method.invoke(instance);
			if(result instanceof Float value) return value;
			return null;
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}

	private static Float invokeBooleanArgFloat(Object instance, String methodName, boolean argument) {
		if(instance == null) return null;
		try {
			Method method = instance.getClass().getMethod(methodName, boolean.class);
			Object result = method.invoke(instance, argument);
			if(result instanceof Float value) return value;
			return null;
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}
}
