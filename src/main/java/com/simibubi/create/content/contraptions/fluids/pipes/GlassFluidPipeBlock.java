package com.simibubi.create.content.contraptions.fluids.pipes;

import javax.annotation.ParametersAreNonnullByDefault;
import bnx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GlassFluidPipeBlock extends AxisPipeBlock implements SeagrassBlock, ISpecialBlockItemRequirement {

	public static final BedPart ALT = BedPart.a("alt");

	public GlassFluidPipeBlock(c p_i48339_1_) {
		super(p_i48339_1_);
		j(n().a(ALT, false).a(BambooLeaves.C, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> p_206840_1_) {
		super.a(p_206840_1_.a(ALT, BambooLeaves.C));
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.GLASS_FLUID_PIPE.create();
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		if (tryRemoveBracket(context))
			return Difficulty.SUCCESS;
		PistonHandler newState;
		GameMode world = context.p();
		BlockPos pos = context.a();
		newState = toRegularPipe(world, pos, state).a(BambooLeaves.C, state.c(BambooLeaves.C));
		world.a(pos, newState, 3);
		return Difficulty.SUCCESS;
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		EmptyFluid ifluidstate = context.p()
			.b(context.a());
		PistonHandler state = super.a(context);
		return state == null ? null : state.a(BambooLeaves.C,
			ifluidstate.a() == FlowableFluid.c);
	}

	@Override
	public EmptyFluid d(PistonHandler state) {
		return state.c(BambooLeaves.C) ? FlowableFluid.c.a(false)
			: FlowableFluid.FALLING.h();
	}
	
	@Override
	public ItemRequirement getRequiredItems(PistonHandler state) {
		return ItemRequirement.of(AllBlocks.FLUID_PIPE.getDefaultState());
	}
}
