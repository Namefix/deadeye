package com.namefix.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CigarItem extends CoreTonicItem {
	public CigarItem(Properties properties, float coreAmount) {
		super(properties, coreAmount, -10f, false);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(Component.translatable("item.deadeye.cigar.tooltip").withStyle(ChatFormatting.GRAY));
		list.add(Component.translatable("item.deadeye.cigar.tooltip2").withStyle(ChatFormatting.GRAY));

		super.appendHoverText(itemStack, level, list, tooltipFlag);
	}
}
