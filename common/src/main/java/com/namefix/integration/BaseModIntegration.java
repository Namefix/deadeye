package com.namefix.integration;

import com.namefix.DeadeyeMod;
import com.namefix.platform.ModPlatform;

public abstract class BaseModIntegration implements DeadeyeIntegration {
	private final String modId;
	private boolean initialized;
	private boolean loaded;

	protected BaseModIntegration(String modId) {
		this.modId = modId;
	}

	@Override
	public String id() {
		return modId;
	}

	@Override
	public final void initialize() {
		if (initialized) return;
		initialized = true;
		loaded = ModPlatform.isModLoaded(modId);
		if (loaded) {
			DeadeyeMod.LOGGER.info("Loaded integration for {}", modId);
		}
		onInitialize();
	}

	@Override
	public final boolean isReady() {
		return initialized && loaded;
	}

	protected void onInitialize() {
	}
}
