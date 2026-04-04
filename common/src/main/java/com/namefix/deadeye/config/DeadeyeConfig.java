package com.namefix.deadeye.config;

import com.teamresourceful.resourcefulconfig.common.annotations.*;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import net.minecraft.client.Minecraft;

@Config(
		value = "deadeye"
)
public final class DeadeyeConfig {
	@Category(id = "Server", translation = "Server")
	public final static class Server {
		@ConfigButton(
				translation = "config.deadeye.reloadServerConfig",
				text = "config.deadeye.reload"
		)
		public static void reloadServerConfig() {
			Minecraft mc = Minecraft.getInstance();
			if(mc.level == null) return;
			if(mc.getSingleplayerServer() == null) return;
			SyncedConfigCache.reloadAndSyncIntegratedServer(mc.getSingleplayerServer());
		}

		@ConfigEntry(
				id = "deadeyeTickRate",
				type = EntryType.FLOAT,
				translation = "config.deadeye.deadeyeTickRate.name"
		)
		@Comment(value = "deadeyeTickRateDesc", translation = "config.deadeye.deadeyeTickRate.desc")
		public static float deadeyeTickRate = 5.0f;

		@ConfigEntry(
				id = "forceTickRate",
				type = EntryType.BOOLEAN,
				translation = "config.deadeye.forceTickRate.name"
		)
		@Comment(value = "forceTickRateDesc", translation = "config.deadeye.forceTickRate.desc")
		public static boolean forceTickRate = false;

		@ConfigEntry(
				id = "bowPullCompensation",
				type = EntryType.BOOLEAN,
				translation = "config.deadeye.bowPullCompensation.name"
		)
		@Comment(value = "bowPullCompensationDesc", translation = "config.deadeye.bowPullCompensation.desc")
		public static boolean bowPullCompensation = true;

		@ConfigEntry(
				id = "instantGunReload",
				type = EntryType.BOOLEAN,
				translation = "config.deadeye.instantGunReload.name"
		)
		@Comment(value = "instantGunReloadDesc", translation = "config.deadeye.instantGunReload.desc")
		public static boolean instantGunReload = true;

		@ConfigEntry(
				id = "naturalDeadeyeRegeneration",
				type = EntryType.FLOAT,
				translation = "config.deadeye.naturalDeadeyeRegeneration.name"
		)
		@Comment(value = "naturalDeadeyeRegenerationDesc", translation = "config.deadeye.naturalDeadeyeRegeneration.desc")
		public static float naturalDeadeyeRegeneration = 0.0f;

		@ConfigEntry(
				id = "deadeyeInvulnerability",
				type = EntryType.BOOLEAN,
				translation = "config.deadeye.deadeyeInvulnerability.name"
		)
		@Comment(value = "deadeyeInvulnerabilityDesc", translation = "config.deadeye.deadeyeInvulnerability.desc")
		public static boolean deadeyeInvulnerability = false;
	}

	@Category(id = "Client", translation = "Client")
	public final static class Client {
		@ConfigEntry(
				id = "enableShaders",
				type = EntryType.BOOLEAN,
				translation = "config.deadeye.enableShaders"
		)
		@Comment(value = "enableShadersDesc", translation = "config.deadeye.enableShaders.desc")
		public static boolean enableShaders = true;

		@ConfigEntry(
				id = "enableLightLeak",
				type = EntryType.BOOLEAN,
				translation = "config.deadeye.enableLightLeak"
		)
		@Comment(value = "enableLightLeakDesc", translation = "config.deadeye.enableLightLeak.desc")
		public static boolean enableLightLeak = true;

		@ConfigEntry(
				id = "pitchShift",
				type = EntryType.BOOLEAN,
				translation = "config.deadeye.pitchShift.name"
		)
		@Comment(value = "pitchShiftDesc", translation = "config.deadeye.pitchShift.desc")
		public static boolean pitchShift = false;

		@ConfigEntry(
				id = "targetMarkSize",
				type = EntryType.FLOAT,
				translation = "config.deadeye.targetMarkSize.name"
		)
		@Comment(value = "targetMarkSizeDesc", translation = "config.deadeye.targetMarkSize.desc")
		public static float targetMarkSize = 1f;

		@ConfigEntry(
				id = "preventMovement",
				type = EntryType.BOOLEAN,
				translation = "config.deadeye.preventMovement.name"
		)
		@Comment(value = "preventMovement", translation = "config.deadeye.preventMovement.desc")
		public static boolean preventMovement = false;
	}

	@Category(id = "HUD", translation = "HUD")
	public final static class HUD {
		public enum HudPosition {
			TOP_LEFT,
			TOP_RIGHT,
			BOTTOM_LEFT,
			BOTTOM_RIGHT,
			NEAR_HOTBAR,
			CUSTOM,
			DISABLED
		}

		@ConfigEntry(
				id = "hudPosition",
				type = EntryType.ENUM,
				translation = "config.deadeye.hudPosition.name"
		)
		public static HudPosition hudPosition = HudPosition.NEAR_HOTBAR;

		@ConfigEntry(
				id = "hudScale",
				type = EntryType.FLOAT,
				translation = "config.deadeye.hudScale.name"
		)
		public static float hudScale = 1f;

		@ConfigEntry(
				id = "hudCustomX",
				type = EntryType.INTEGER,
				translation = "config.deadeye.hudCustomX.name"
		)
		@Comment(value = "hudCustomXDesc", translation = "config.deadeye.hudCustom.desc")
		public static int hudCustomX = 100;

		@ConfigEntry(
				id = "hudCustomY",
				type = EntryType.INTEGER,
				translation = "config.deadeye.hudCustomY.name"
		)
		@Comment(value = "hudCustomXDesc", translation = "config.deadeye.hudCustom.desc")
		public static int hudCustomY = 100;

		@ConfigEntry(
				id = "enableInfoToast",
				type = EntryType.BOOLEAN,
				translation = "config.deadeye.enableInfoToast.name"
		)
		public static boolean enableInfoToast = true;
	}
}
