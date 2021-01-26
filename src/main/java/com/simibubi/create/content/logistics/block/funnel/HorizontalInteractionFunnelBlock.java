package com.simibubi.create.content.logistics.block.funnel;

import bnx;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.HayBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public abstract class HorizontalInteractionFunnelBlock extends HayBlock implements IWrenchable {

	public static final BedPart POWERED = BambooLeaves.w;
	public static final BedPart PUSHING = BedPart.a("pushing");
	private BlockEntry<? extends FunnelBlock> parent;

	public HorizontalInteractionFunnelBlock(BlockEntry<? extends FunnelBlock> parent, c p_i48377_1_) {
		super(p_i48377_1_);
		this.parent = parent;
		PistonHandler defaultState = n().a(PUSHING, true);
		if (hasPoweredProperty())
			defaultState = defaultState.a(POWERED, false);
		j(defaultState);
	}

	public abstract boolean hasPoweredProperty();

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.FUNNEL.create();
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> p_206840_1_) {
		if (hasPoweredProperty())
			p_206840_1_.a(POWERED);
		super.a(p_206840_1_.a(aq, PUSHING));
	}

	@Override
	public PistonHandler a(PotionUtil ctx) {
		PistonHandler stateForPlacement = super.a(ctx);
		if (hasPoweredProperty())
			stateForPlacement = stateForPlacement.a(POWERED, ctx.p()
				.r(ctx.a()));
		return stateForPlacement;
	}

	@Override
	public void a(PistonHandler p_196243_1_, GameMode p_196243_2_, BlockPos p_196243_3_, PistonHandler p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity()
			&& (p_196243_1_.b() != p_196243_4_.b() && !FunnelBlock.isFunnel(p_196243_4_)
				|| !p_196243_4_.hasTileEntity())) {
			p_196243_2_.o(p_196243_3_);
		}
	}

	@Override
	public ItemCooldownManager getPickBlock(PistonHandler state, Box target, MobSpawnerLogic world, BlockPos pos,
		PlayerAbilities player) {
		return parent.asStack();
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction direction, PistonHandler neighbour, GrassColors world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (!canStillInteract(state, world, pos)) {
			PistonHandler parentState = parent.getDefaultState();
			if (state.d(POWERED).orElse(false))
				parentState = parentState.a(POWERED, true);
			return parentState.a(FunnelBlock.SHAPE, state.c(aq));
		}
		return state;
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible world, BlockPos pos) {
		return !world.d_(pos.offset(state.c(aq)
			.getOpposite()))
			.j(world, pos)
			.b();
	}

	protected abstract boolean canStillInteract(PistonHandler state, ItemConvertible world, BlockPos pos);

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (!hasPoweredProperty())
			return;
		if (worldIn.v)
			return;
		boolean previouslyPowered = state.c(POWERED);
		if (previouslyPowered != worldIn.r(pos))
			worldIn.a(pos, state.a(POWERED), 2);
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		if (!context.p().v)
			context.p()
				.a(context.a(), state.a(PUSHING));
		return Difficulty.SUCCESS;
	}

}
