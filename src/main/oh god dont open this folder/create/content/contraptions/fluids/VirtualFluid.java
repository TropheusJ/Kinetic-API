package com.simibubi.create.content.contraptions.fluids;

import cut;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class VirtualFluid extends ForgeFlowingFluid {

	public VirtualFluid(Properties properties) {
		super(properties);
	}

	@Override
	public cut e() {
		return super.e();
	}

	@Override
	public cut d() {
		return this;
	}

	@Override
	public HoeItem a() {
		return AliasedBlockItem.a;
	}

	@Override
	protected PistonHandler b(EmptyFluid state) {
		return BellBlock.FACING.n();
	}

	@Override
	public boolean c(EmptyFluid p_207193_1_) {
		return false;
	}

	@Override
	public int d(EmptyFluid p_207192_1_) {
		return 0;
	}

}
