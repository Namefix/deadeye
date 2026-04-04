package com.namefix.deadeye.mixin.integration;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class IntegrationMixinPluginBase implements IMixinConfigPlugin {
	private final Map<String, String> mixinToMod = new HashMap<>();

	protected final void registerIntegrationMixin(String mixinClassName, String modId) {
		mixinToMod.put(mixinClassName, modId);
	}

	protected abstract boolean isModLoaded(String modId);

	@Override
	public void onLoad(String mixinPackage) {
		registerMixins();
	}

	protected abstract void registerMixins();

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		String modId = mixinToMod.get(mixinClassName);
		if (modId == null) return true;
		return isModLoaded(modId);
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
