package com.namefix.deadeye.sound;

import com.namefix.deadeye.client.DeadeyeClient;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class DeadeyeLoopingSound extends AbstractTickableSoundInstance {
	boolean shouldStop = false;
	boolean pitchShift;

	public DeadeyeLoopingSound(SoundEvent soundEvent, SoundSource soundSource, RandomSource randomSource, float volume, boolean pitchShift) {
		super(soundEvent, soundSource, randomSource);
		this.looping = true;
		this.delay = 0;
		this.volume = volume;
		this.pitch = 1.0F;
		this.attenuation = Attenuation.NONE;
		this.relative = true;
		this.pitchShift = pitchShift;
	}

	@Override
	public void tick() {
		if(shouldStop)
			this.stop();

		if(pitchShift)
			this.pitch = 1f + DeadeyeClient.DEADEYE_ENDING;
	}

	public void stopLooping() {
		shouldStop = true;
	}
}
