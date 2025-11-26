package com.namefix.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class DeadeyeLoopingSound extends AbstractTickableSoundInstance {
	boolean shouldStop = false;

	protected DeadeyeLoopingSound(SoundEvent soundEvent, SoundSource soundSource, RandomSource randomSource) {
		super(soundEvent, soundSource, randomSource);
		this.looping = true;
		this.delay = 0;
		this.volume = 1.0F;
		this.pitch = 1.0F;
		this.attenuation = Attenuation.NONE;
		this.relative = true;
	}

	@Override
	public void tick() {
		if(shouldStop)
			this.stop();
	}

	public void stopLooping() {
		shouldStop = true;
	}
}
