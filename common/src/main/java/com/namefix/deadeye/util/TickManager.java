package com.namefix.deadeye.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

public final class TickManager {
	private static final float DEFAULT_TICK_RATE = 20.0f;
	private static final float MAX_REALTIME_DELTA_TICKS = 7.0f;
	private static final float CLAMPED_REALTIME_DELTA_TICKS = 0.5f;
	private static final float SERVER_RECOVERY_TICKRATE_STEP = 6.0f;
	private static final Map<MinecraftServer, Float> SERVER_TICK_RATE_CACHE = new WeakHashMap<>();
	private static final Map<MinecraftServer, Float> SERVER_APPLIED_TICK_RATE = new WeakHashMap<>();
	private static final Map<Object, RealtimeState> REALTIME_STATE_CACHE = new WeakHashMap<>();

	private static final class RealtimeState {
		long lastRealtimeNs;
		long frameMarkerNs;
		float cachedRealtimeDeltaTicks;
	}

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
		float targetTickRate = getTickRate(server);
		float appliedTickRate;
		synchronized (SERVER_APPLIED_TICK_RATE) {
			float previousApplied = SERVER_APPLIED_TICK_RATE.getOrDefault(server, targetTickRate);
			if(targetTickRate >= previousApplied) {
				appliedTickRate = Math.min(targetTickRate, previousApplied + SERVER_RECOVERY_TICKRATE_STEP);
			} else {
				appliedTickRate = targetTickRate;
			}

			if(Math.abs(appliedTickRate - DEFAULT_TICK_RATE) < 0.001f && Math.abs(targetTickRate - DEFAULT_TICK_RATE) < 0.001f) {
				SERVER_APPLIED_TICK_RATE.remove(server);
			} else {
				SERVER_APPLIED_TICK_RATE.put(server, appliedTickRate);
			}
		}

		if(appliedTickRate <= 0.0f) return Math.round(1000.0f / DEFAULT_TICK_RATE);
		return Math.max(1L, Math.round(1000.0f / appliedTickRate));
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

		synchronized (REALTIME_STATE_CACHE) {
			RealtimeState state = REALTIME_STATE_CACHE.computeIfAbsent(minecraftInstance, ignored -> new RealtimeState());
			if(state.frameMarkerNs > 0L) {
				return state.cachedRealtimeDeltaTicks;
			}
		}

		Float legacyDelta = invokeNoArgFloat(minecraftInstance, "getDeltaFrameTime");
		if(legacyDelta == null) return 0.0f;
		if(legacyDelta > MAX_REALTIME_DELTA_TICKS) return CLAMPED_REALTIME_DELTA_TICKS;
		return legacyDelta;
	}

	public static void beginClientFrame(Object minecraftInstance) {
		if(minecraftInstance == null) return;
		long nowNs = System.nanoTime();

		synchronized (REALTIME_STATE_CACHE) {
			RealtimeState state = REALTIME_STATE_CACHE.computeIfAbsent(minecraftInstance, ignored -> new RealtimeState());
			if(state.lastRealtimeNs > 0L) {
				float deltaTicks = (nowNs - state.lastRealtimeNs) / 50_000_000.0f;
				if(!Float.isFinite(deltaTicks) || deltaTicks < 0.0f) deltaTicks = 0.0f;
				if(deltaTicks > MAX_REALTIME_DELTA_TICKS) deltaTicks = CLAMPED_REALTIME_DELTA_TICKS;
				state.cachedRealtimeDeltaTicks = deltaTicks;
			} else {
				Float legacyDelta = invokeNoArgFloat(minecraftInstance, "getDeltaFrameTime");
				float initialDelta = legacyDelta != null ? legacyDelta : 0.0f;
				if(initialDelta > MAX_REALTIME_DELTA_TICKS) initialDelta = CLAMPED_REALTIME_DELTA_TICKS;
				state.cachedRealtimeDeltaTicks = initialDelta;
			}

			state.lastRealtimeNs = nowNs;
			state.frameMarkerNs = nowNs;
		}
	}

	public static void endClientFrame(Object minecraftInstance) {
		if(minecraftInstance == null) return;
		synchronized (REALTIME_STATE_CACHE) {
			RealtimeState state = REALTIME_STATE_CACHE.get(minecraftInstance);
			if(state != null) {
				state.frameMarkerNs = 0L;
			}
		}
	}

	public static float getGameTimeDeltaPartialTick(Object minecraftInstance, boolean runsNormally) {
		return getGameTimeDeltaPartialTick(minecraftInstance, runsNormally, 1.0f);
	}

	public static float getGameTimeDeltaPartialTick(Object minecraftInstance, boolean runsNormally, float tickScale) {
		if(minecraftInstance == null) return 0.0f;

		Object timer = invokeNoArgObject(minecraftInstance, "getTimer");
		Float partialTick = invokeBooleanArgFloat(timer, "getGameTimeDeltaPartialTick", runsNormally);
		if(partialTick != null) {
			return clampPartialTick(partialTick * sanitizeScale(tickScale));
		}

		Float legacyPartialTick = invokeNoArgFloat(minecraftInstance, "getFrameTime");
		return legacyPartialTick != null ? clampPartialTick(legacyPartialTick * sanitizeScale(tickScale)) : 0.0f;
	}

	public static float getTickScale(float currentTickRate, float previousTickRate) {
		float sanitizedCurrent = sanitizeTickRate(currentTickRate);
		float sanitizedPrevious = sanitizeTickRate(previousTickRate);
		if(sanitizedPrevious <= 0.0f) return 1.0f;
		return sanitizeScale(sanitizedCurrent / sanitizedPrevious);
	}

	private static float sanitizeTickRate(float tickRate) {
		if(!Float.isFinite(tickRate) || tickRate <= 0.0f) {
			return DEFAULT_TICK_RATE;
		}
		return tickRate;
	}

	private static float sanitizeScale(float scale) {
		if(!Float.isFinite(scale) || scale <= 0.0f) return 1.0f;
		return Math.min(scale, 4.0f);
	}

	private static float clampPartialTick(float partialTick) {
		if(!Float.isFinite(partialTick)) return 0.0f;
		if(partialTick < 0.0f) return 0.0f;
		return Math.min(partialTick, 1.0f);
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
