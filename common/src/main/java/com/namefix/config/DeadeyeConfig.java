package com.namefix.config;

import com.teamresourceful.resourcefulconfig.api.annotations.*;

@Config(
		value = "deadeye",
		categories = {
				DeadeyeConfig.Server.class,
				DeadeyeConfig.Client.class,
				DeadeyeConfig.HUD.class
		}
)
@ConfigInfo(
		titleTranslation = "config.deadeye.title",
		descriptionTranslation = "config.deadeye.description"
)
public class DeadeyeConfig {
	@Category(value = "Server")
	public static class Server {
		@ConfigEntry(
				id = "deadeyeTickRate",
				translation = "config.deadeye.deadeyeTickRate.name"
		)
		@Comment(value = "deadeyeTickRateDesc", translation = "config.deadeye.deadeyeTickRate.desc")
		public static float deadeyeTickRate = 5.0f;

		@ConfigEntry(
				id = "forceTickRate",
				translation = "config.deadeye.forceTickRate.name"
		)
		@Comment(value = "forceTickRateDesc", translation = "config.deadeye.forceTickRate.desc")
		public static boolean forceTickRate = false;

		@ConfigEntry(
				id = "bowPullCompensation",
				translation = "config.deadeye.bowPullCompensation.name"
		)
		@Comment(value = "bowPullCompensationDesc", translation = "config.deadeye.bowPullCompensation.desc")
		public static boolean bowPullCompensation = true;

		@ConfigEntry(
				id = "naturalDeadeyeRegeneration",
				translation = "config.deadeye.naturalDeadeyeRegeneration.name"
		)
		@Comment(value = "naturalDeadeyeRegenerationDesc", translation = "config.deadeye.naturalDeadeyeRegeneration.desc")
		public static float naturalDeadeyeRegeneration = 0.0f;

		@ConfigEntry(
				id = "deadeyeInvulnerability",
				translation = "config.deadeye.deadeyeInvulnerability.name"
		)
		@Comment(value = "deadeyeInvulnerabilityDesc", translation = "config.deadeye.deadeyeInvulnerability.desc")
		public static boolean deadeyeInvulnerability = false;
	}

	@Category(value = "Client")
	public static class Client {
		@ConfigEntry(
				id = "enableShaders",
				translation = "config.deadeye.enableShaders"
		)
		@Comment(value = "enableShadersDesc", translation = "config.deadeye.enableShaders.desc")
		public static boolean enableShaders = true;

		@ConfigEntry(
				id = "enableLightLeak",
				translation = "config.deadeye.enableLightLeak"
		)
		@Comment(value = "enableLightLeakDesc", translation = "config.deadeye.enableLightLeak.desc")
		public static boolean enableLightLeak = true;

		@ConfigEntry(
				id = "pitchShift",
				translation = "config.deadeye.pitchShift.name"
		)
		@Comment(value = "pitchShiftDesc", translation = "config.deadeye.pitchShift.desc")
		public static boolean pitchShift = false;

		@ConfigEntry(
				id = "targetMarkSize",
				translation = "config.deadeye.targetMarkSize.name"
		)
		@Comment(value = "targetMarkSizeDesc", translation = "config.deadeye.targetMarkSize.desc")
		public static float targetMarkSize = 1f;

		@ConfigEntry(
				id = "preventMovement",
				translation = "config.deadeye.preventMovement.name"
		)
		@Comment(value = "preventMovement", translation = "config.deadeye.preventMovement.desc")
		public static boolean preventMovement = false;
	}

	@Category(value = "HUD")
	public static class HUD {
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
				translation = "config.deadeye.hudPosition.name"
		)
		public static HudPosition hudPosition = HudPosition.NEAR_HOTBAR;

		@ConfigEntry(
				id = "hudScale",
				translation = "config.deadeye.hudScale.name"
		)
		public static float hudScale = 1f;

		@ConfigEntry(
				id = "hudCustomX",
				translation = "config.deadeye.hudCustomX.name"
		)
		@Comment(value = "hudCustomXDesc", translation = "config.deadeye.hudCustom.desc")
		public static int hudCustomX = 100;

		@ConfigEntry(
				id = "hudCustomY",
				translation = "config.deadeye.hudCustomY.name"
		)
		@Comment(value = "hudCustomXDesc", translation = "config.deadeye.hudCustom.desc")
		public static int hudCustomY = 100;

		@ConfigEntry(
				id = "enableInfoToast",
				translation = "config.deadeye.enableInfoToast.name"
		)
		public static boolean enableInfoToast = true;
	}
}
