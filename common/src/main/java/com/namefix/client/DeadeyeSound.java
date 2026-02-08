package com.namefix.client;

import com.namefix.registry.SoundEventRegistry;
import com.namefix.sound.DeadeyeLoopingSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class DeadeyeSound {
	private static DeadeyeLoopingSound SOUND_BACKGROUND;
	private static DeadeyeLoopingSound SOUND_BACKGROUND2;

	private static SoundEvent SOUND_ENTER;
	private static SoundEvent SOUND_EXIT;
	private static SoundEvent SOUND_BACKGROUND2_EXIT;
	private static SoundEvent SOUND_HEARTBEAT_IN;
	private static SoundEvent SOUND_HEARTBEAT_OUT;
	private static SoundEvent SOUND_MARK;

	private static final float HEARTBEAT_IN_DELAY_TICKS = 23f;
	private static final float HEARTBEAT_OUT_DELAY_TICKS = 7f;
	private static float HEARTBEAT_PHASE_TIME = 0f;
	private static boolean WAITING_FOR_HEARTBEAT_IN = true;

	public static void initialize() {
		resetBackgroundSounds();

		SOUND_ENTER = SoundEventRegistry.DEADEYE_JOHN_ENTER.getOrNull();
		SOUND_EXIT = SoundEventRegistry.DEADEYE_JOHN_EXIT.getOrNull();
		SOUND_BACKGROUND2_EXIT = SoundEventRegistry.DEADEYE_JOHN_BACKGROUND2_EXIT.getOrNull();
		SOUND_HEARTBEAT_IN = SoundEventRegistry.DEADEYE_JOHN_HEARTBEAT_IN.getOrNull();
		SOUND_HEARTBEAT_OUT = SoundEventRegistry.DEADEYE_JOHN_HEARTBEAT_OUT.getOrNull();
		SOUND_MARK = SoundEventRegistry.DEADEYE_ARTHUR_MARK.getOrNull();
	}

	public static void resetBackgroundSounds() {
		SOUND_BACKGROUND = new DeadeyeLoopingSound(SoundEventRegistry.DEADEYE_JOHN_BACKGROUND.getOrNull(), SoundSource.PLAYERS, RandomSource.create(), 0.4f, true);
		SOUND_BACKGROUND2 = new DeadeyeLoopingSound(SoundEventRegistry.DEADEYE_JOHN_BACKGROUND2.getOrNull(), SoundSource.PLAYERS, RandomSource.create(), 0.1f, false);
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		if(!DeadeyeClient.DEADEYE_ENABLED || mc.isPaused()) return;
		Player player = mc.player;
		float delta = mc.getTimer().getRealtimeDeltaTicks();
		if(player == null) return;

		HEARTBEAT_PHASE_TIME += delta;
		float threshold = WAITING_FOR_HEARTBEAT_IN ? HEARTBEAT_IN_DELAY_TICKS-(HEARTBEAT_IN_DELAY_TICKS*(DeadeyeClient.DEADEYE_ENDING/1.5f)) : HEARTBEAT_OUT_DELAY_TICKS-(HEARTBEAT_OUT_DELAY_TICKS*(DeadeyeClient.DEADEYE_ENDING/1.5f));

		while(HEARTBEAT_PHASE_TIME >= threshold) {
			if(WAITING_FOR_HEARTBEAT_IN) {
				play2D(SOUND_HEARTBEAT_IN, 2.0f, 1.0f);
				WAITING_FOR_HEARTBEAT_IN = false;
			} else {
				play2D(SOUND_HEARTBEAT_OUT, 2.0f, 1.0f);
				WAITING_FOR_HEARTBEAT_IN = true;
			}

			HEARTBEAT_PHASE_TIME -= threshold;
			threshold = WAITING_FOR_HEARTBEAT_IN ? HEARTBEAT_IN_DELAY_TICKS : HEARTBEAT_OUT_DELAY_TICKS;
		}
	}

	public static void startBackgroundSounds() {
		Minecraft mc = Minecraft.getInstance();

		resetBackgroundSounds();
		mc.getSoundManager().play(SOUND_BACKGROUND);
		mc.getSoundManager().play(SOUND_BACKGROUND2);
	}

	public static void stopBackgroundSounds() {
		SOUND_BACKGROUND.stopLooping();
		SOUND_BACKGROUND2.stopLooping();
	}

	public static void playMarkSound() {
		play2D(SOUND_MARK, 1.0f, 1.0f);
	}

	public static void playEnterSound() {
		play2D(SOUND_ENTER, 1.0f, 1.0f);
	}

	public static void playExitSound() {
		Minecraft mc = Minecraft.getInstance();
		play2D(SOUND_EXIT, 1.0f, 1.0f);

		if(DeadeyeClient.DEADEYE_DATA.deadeyeCore == 0 && DeadeyeClient.DEADEYE_DATA.deadeyeMeter == 0)
			play2D(SOUND_BACKGROUND2_EXIT, 0.1f, 1.0f);
	}

	public static void playUIAppear() {
		play2D(SoundEventRegistry.UI_APPEAR.getOrNull(), 0.5f, 1.0f);
	}

	private static void play2D(SoundEvent sound, float volume, float pitch) {
		Minecraft mc = Minecraft.getInstance();
		if(sound == null || mc == null) return;
		mc.getSoundManager().play(new SimpleSoundInstance(
			sound.getLocation(),
			SoundSource.PLAYERS,
			volume,
			pitch,
			SoundInstance.createUnseededRandom(),
			false,
			0,
			SoundInstance.Attenuation.NONE,
			0.0,
			0.0,
			0.0,
			true
		));
	}
}
