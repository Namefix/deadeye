package com.namefix.deadeye.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChewingTobaccoItem extends CoreTonicItem {
	public ChewingTobaccoItem(Properties properties, float coreAmount) {
		super(properties, coreAmount, true);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(Component.translatable("item.deadeye.chewing_tobacco.tooltip").withStyle(ChatFormatting.GRAY));

		super.appendHoverText(itemStack, level, list, tooltipFlag);
	}
}
