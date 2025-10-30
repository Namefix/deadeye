package com.namefix.config;

import com.teamresourceful.resourcefulconfig.api.annotations.*;

@Config(
		value = "deadeye",
		categories = {
				DeadeyeConfig.Server.class,
				DeadeyeConfig.Client.class
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
	}

	@Category(value = "Client")
	public static class Client {
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
	}
}
