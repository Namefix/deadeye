package com.namefix.deadeye.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import java.util.List;

public class ChewingTobaccoItem extends CoreTonicItem {
	public ChewingTobaccoItem(Properties properties, float coreAmount) {
		super(properties, coreAmount, true);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(Component.translatable("item.deadeye.chewing_tobacco.tooltip").withStyle(ChatFormatting.GRAY));

		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
	}
}
