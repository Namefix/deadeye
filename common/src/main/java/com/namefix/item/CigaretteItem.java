package com.namefix.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CigaretteItem extends CoreTonicItem {
	public CigaretteItem(Properties properties, float coreAmount) {
		super(properties, coreAmount, false);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(Component.translatable("item.deadeye.cigarette.tooltip").withStyle(ChatFormatting.GRAY));

		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
	}
}
