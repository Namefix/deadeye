package com.namefix.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TobaccoWaterItem extends Item {
	public TobaccoWaterItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(Component.translatable("item.deadeye.tobacco_water.tooltip").withStyle(ChatFormatting.GRAY));

		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
	}
}
