package com.namefix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class DeadeyeCommandRegistry {
	private static final List<DeadeyeCommand> COMMANDS = new ArrayList<>();
	private static final List<Consumer<LiteralArgumentBuilder<CommandSourceStack>>> ROOT_MUTATORS = new ArrayList<>();
	private static boolean initialized;

	private DeadeyeCommandRegistry() {}

	public static void initialize() {
		if (initialized) return;
		initialized = true;
		registerInternal();
		CommandRegistrationEvent.EVENT.register(DeadeyeCommandRegistry::registerAll);
	}

	public static void register(DeadeyeCommand command) {
		COMMANDS.add(command);
	}

	public static List<DeadeyeCommand> allCommands() {
		return Collections.unmodifiableList(COMMANDS);
	}

	public static void addSubcommand(Consumer<LiteralArgumentBuilder<CommandSourceStack>> mutator) {
		ROOT_MUTATORS.add(mutator);
	}

	private static void registerInternal() {
		register(new ToggleDeadeyeCommand());
		register(new DataDeadeyeCommand());
	}

	private static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection environment) {
		ROOT_MUTATORS.clear();
		for (DeadeyeCommand command : COMMANDS) {
			command.register(dispatcher, context, environment);
		}
		dispatcher.register(buildRoot());
	}

	private static LiteralArgumentBuilder<CommandSourceStack> buildRoot() {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("deadeye")
			.requires(stack -> stack.hasPermission(2));
		for (Consumer<LiteralArgumentBuilder<CommandSourceStack>> mutator : ROOT_MUTATORS) {
			mutator.accept(root);
		}
		return root;
	}
}
