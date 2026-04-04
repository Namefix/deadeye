package com.namefix.deadeye.registry;

import com.namefix.deadeye.DeadeyeMod;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundEventRegistry {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(DeadeyeMod.MOD_ID, Registries.SOUND_EVENT);

	public static final RegistrySupplier<SoundEvent> DEADEYE_JOHN_BACKGROUND = registerDefaultSound("deadeye_john_background");
	public static final RegistrySupplier<SoundEvent> DEADEYE_JOHN_BACKGROUND2 = registerDefaultSound("deadeye_john_background2");
	public static final RegistrySupplier<SoundEvent> DEADEYE_JOHN_ENTER = registerDefaultSound("deadeye_john_enter");
	public static final RegistrySupplier<SoundEvent> DEADEYE_JOHN_EXIT = registerDefaultSound("deadeye_john_exit");
	public static final RegistrySupplier<SoundEvent> DEADEYE_JOHN_BACKGROUND2_EXIT = registerDefaultSound("deadeye_john_background2_exit");
	public static final RegistrySupplier<SoundEvent> DEADEYE_JOHN_HEARTBEAT_IN = registerDefaultSound("deadeye_john_heartbeat_in");
	public static final RegistrySupplier<SoundEvent> DEADEYE_JOHN_HEARTBEAT_OUT = registerDefaultSound("deadeye_john_heartbeat_out");

	public static final RegistrySupplier<SoundEvent> DEADEYE_ARTHUR_MARK = registerDefaultSound("deadeye_arthur_mark");

	public static final RegistrySupplier<SoundEvent> CONSUME_TONIC = registerDefaultSound("consume_tonic");
	public static final RegistrySupplier<SoundEvent> UI_APPEAR = registerDefaultSound("ui_appear");

	private static RegistrySupplier<SoundEvent> registerDefaultSound(String id) {
		return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(DeadeyeMod.MOD_ID, id)));
	}

	public static void register() {
		SOUNDS.register();
	}
}
