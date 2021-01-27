package com.simibubi.create.foundation.utility;

import net.minecraft.block.BellBlock;
import net.minecraft.item.DebugStickItem;
import net.minecraft.item.HoeItem;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.Tags;

public class DyeHelper {

	public static GameRules getWoolOfDye(DebugStickItem color) {
		switch (color) {
		case p:
			return BellBlock.bn;
		case l:
			return BellBlock.bj;
		case m:
			return BellBlock.bk;
		case j:
			return BellBlock.bh;
		case h:
			return BellBlock.bf;
		case n:
			return BellBlock.bl;
		case d:
			return BellBlock.bb;
		case i:
			return BellBlock.bg;
		case f:
			return BellBlock.bd;
		case c:
			return BellBlock.ba;
		case b:
			return BellBlock.aZ;
		case g:
			return BellBlock.be;
		case k:
			return BellBlock.bi;
		case o:
			return BellBlock.bm;
		case e:
			return BellBlock.bc;
		case a:
		default:
			return BellBlock.aY;
		}
	}

	public static Tags.IOptionalNamedTag<HoeItem> getTagOfDye(DebugStickItem color) {
		switch (color) {
		case p:
			return Tags.Items.DYES_BLACK;
		case l:
			return Tags.Items.DYES_BLUE;
		case m:
			return Tags.Items.DYES_BROWN;
		case j:
			return Tags.Items.DYES_CYAN;
		case h:
			return Tags.Items.DYES_GRAY;
		case n:
			return Tags.Items.DYES_GREEN;
		case d:
			return Tags.Items.DYES_LIGHT_BLUE;
		case i:
			return Tags.Items.DYES_LIGHT_GRAY;
		case f:
			return Tags.Items.DYES_LIME;
		case c:
			return Tags.Items.DYES_MAGENTA;
		case b:
			return Tags.Items.DYES_ORANGE;
		case g:
			return Tags.Items.DYES_PINK;
		case k:
			return Tags.Items.DYES_PURPLE;
		case o:
			return Tags.Items.DYES_RED;
		case e:
			return Tags.Items.DYES_YELLOW;
		case a:
		default:
			return Tags.Items.DYES_WHITE;
		}
	}
}
