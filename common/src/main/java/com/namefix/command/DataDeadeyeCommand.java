package com.namefix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.namefix.data.PlayerSavedData;
import com.namefix.data.StateManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public class DataDeadeyeCommand implements DeadeyeCommand {
	@Override
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection environment) {
		DeadeyeCommandRegistry.addSubcommand(root -> {
			root.then(skillNode());
			root.then(levelNode());
			root.then(xpNode());
			root.then(meterNode());
			root.then(coreNode());
		});
	}

	private LiteralArgumentBuilder<CommandSourceStack> skillNode() {
		return Commands.literal("skill")
			.then(Commands.literal("get")
				.executes(ctx -> getSkill(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.argument("target", EntityArgument.player())
					.executes(ctx -> getSkill(ctx, EntityArgument.getPlayer(ctx, "target")))))
			.then(Commands.literal("set")
				.then(Commands.argument("value", IntegerArgumentType.integer(0))
					.executes(ctx -> setSkill(ctx, List.of(ctx.getSource().getPlayerOrException()), IntegerArgumentType.getInteger(ctx, "value")))
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(ctx -> setSkill(ctx, EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "value"))))));
	}

	private LiteralArgumentBuilder<CommandSourceStack> levelNode() {
		return Commands.literal("level")
			.then(Commands.literal("get")
				.executes(ctx -> getLevel(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.argument("target", EntityArgument.player())
					.executes(ctx -> getLevel(ctx, EntityArgument.getPlayer(ctx, "target")))))
			.then(Commands.literal("set")
				.then(Commands.argument("value", IntegerArgumentType.integer(0))
					.executes(ctx -> setLevel(ctx, List.of(ctx.getSource().getPlayerOrException()), IntegerArgumentType.getInteger(ctx, "value")))
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(ctx -> setLevel(ctx, EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "value"))))));
	}

	private LiteralArgumentBuilder<CommandSourceStack> xpNode() {
		return Commands.literal("xp")
			.then(Commands.literal("get")
				.executes(ctx -> getXp(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.argument("target", EntityArgument.player())
					.executes(ctx -> getXp(ctx, EntityArgument.getPlayer(ctx, "target")))))
			.then(Commands.literal("set")
				.then(Commands.argument("value", IntegerArgumentType.integer(0))
					.executes(ctx -> setXp(ctx, List.of(ctx.getSource().getPlayerOrException()), IntegerArgumentType.getInteger(ctx, "value")))
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(ctx -> setXp(ctx, EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "value"))))));
	}

	private LiteralArgumentBuilder<CommandSourceStack> meterNode() {
		return Commands.literal("meter")
			.then(Commands.literal("get")
				.executes(ctx -> getMeter(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.argument("target", EntityArgument.player())
					.executes(ctx -> getMeter(ctx, EntityArgument.getPlayer(ctx, "target")))))
			.then(Commands.literal("set")
				.then(Commands.argument("value", FloatArgumentType.floatArg(0f))
					.executes(ctx -> setMeter(ctx, List.of(ctx.getSource().getPlayerOrException()), FloatArgumentType.getFloat(ctx, "value")))
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(ctx -> setMeter(ctx, EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "value"))))));
	}

	private LiteralArgumentBuilder<CommandSourceStack> coreNode() {
		return Commands.literal("core")
			.then(Commands.literal("get")
				.executes(ctx -> getCore(ctx, ctx.getSource().getPlayerOrException()))
				.then(Commands.argument("target", EntityArgument.player())
					.executes(ctx -> getCore(ctx, EntityArgument.getPlayer(ctx, "target")))))
			.then(Commands.literal("set")
				.then(Commands.argument("value", FloatArgumentType.floatArg(0f))
					.executes(ctx -> setCore(ctx, List.of(ctx.getSource().getPlayerOrException()), FloatArgumentType.getFloat(ctx, "value")))
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(ctx -> setCore(ctx, EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "value"))))));
	}

	private int getSkill(CommandContext<CommandSourceStack> context, ServerPlayer target) {
		int value = StateManager.getPlayerState(target).deadeyeSkill;
		sendGetFeedback(context, "command.deadeye.data.skill", Component.literal(Integer.toString(value)), target);
		return value;
	}

	private int getLevel(CommandContext<CommandSourceStack> context, ServerPlayer target) {
		int value = StateManager.getPlayerState(target).deadeyeLevel;
		sendGetFeedback(context, "command.deadeye.data.level", Component.literal(Integer.toString(value)), target);
		return value;
	}

	private int getXp(CommandContext<CommandSourceStack> context, ServerPlayer target) {
		int value = StateManager.getPlayerState(target).deadeyeXp;
		sendGetFeedback(context, "command.deadeye.data.xp", Component.literal(Integer.toString(value)), target);
		return value;
	}

	private int getMeter(CommandContext<CommandSourceStack> context, ServerPlayer target) {
		float value = StateManager.getPlayerState(target).deadeyeMeter;
		sendGetFeedback(context, "command.deadeye.data.meter", Component.literal(Float.toString(value)), target);
		return 1;
	}

	private int getCore(CommandContext<CommandSourceStack> context, ServerPlayer target) {
		float value = StateManager.getPlayerState(target).deadeyeCore;
		sendGetFeedback(context, "command.deadeye.data.core", Component.literal(Float.toString(value)), target);
		return 1;
	}

	private int setSkill(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, int value) {
		targets.forEach(player -> {
			PlayerSavedData.setDeadeyeSkill(player, value);
			sendSetFeedback(context, "command.deadeye.data.skill", Component.literal(Integer.toString(StateManager.getPlayerState(player).deadeyeSkill)), player);
		});
		return targets.size();
	}

	private int setLevel(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, int value) {
		targets.forEach(player -> {
			PlayerSavedData.setDeadeyeLevel(player, value);
			sendSetFeedback(context, "command.deadeye.data.level", Component.literal(Integer.toString(StateManager.getPlayerState(player).deadeyeLevel)), player);
		});
		return targets.size();
	}

	private int setXp(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, int value) {
		targets.forEach(player -> {
			PlayerSavedData.setDeadeyeXP(player, value);
			sendSetFeedback(context, "command.deadeye.data.xp", Component.literal(Integer.toString(StateManager.getPlayerState(player).deadeyeXp)), player);
		});
		return targets.size();
	}

	private int setMeter(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, float value) {
		targets.forEach(player -> {
			PlayerSavedData.setDeadeyeMeter(player, value);
			sendSetFeedback(context, "command.deadeye.data.meter", Component.literal(Float.toString(StateManager.getPlayerState(player).deadeyeMeter)), player);
		});
		return targets.size();
	}

	private int setCore(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, float value) {
		targets.forEach(player -> {
			PlayerSavedData.setDeadeyeCore(player, value);
			sendSetFeedback(context, "command.deadeye.data.core", Component.literal(Float.toString(StateManager.getPlayerState(player).deadeyeCore)), player);
		});
		return targets.size();
	}

	private void sendGetFeedback(CommandContext<CommandSourceStack> context, String statKey, Component value, ServerPlayer player) {
		Component message = Component.translatable("command.deadeye.data.get", Component.translatable(statKey), value, player.getDisplayName());
		context.getSource().sendSuccess(() -> message, true);
	}

	private void sendSetFeedback(CommandContext<CommandSourceStack> context, String statKey, Component value, ServerPlayer player) {
		Component message = Component.translatable("command.deadeye.data.set", Component.translatable(statKey), value, player.getDisplayName());
		context.getSource().sendSuccess(() -> message, true);
	}
}
