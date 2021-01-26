package com.simibubi.create.content.logistics.block.funnel;

import javax.annotation.Nullable;
import apx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public abstract class FunnelBlock extends ProperDirectionalBlock implements ITE<FunnelTileEntity> {

	public static final BedPart POWERED = BambooLeaves.w;

	public FunnelBlock(c p_i48415_1_) {
		super(p_i48415_1_);
		j(n().a(POWERED, false));
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction facing = context.n() == null || context.n()
			.bt() ? context.j()
				: context.d()
					.getOpposite();
		return n().a(SHAPE, facing)
			.a(POWERED, context.p()
				.r(context.a()));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(POWERED));
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.v)
			return;
		boolean previouslyPowered = state.c(POWERED);
		if (previouslyPowered != worldIn.r(pos))
			worldIn.a(pos, state.a(POWERED), 2);
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {

		ItemCooldownManager heldItem = player.b(handIn);
		boolean shouldntInsertItem = AllBlocks.MECHANICAL_ARM.isIn(heldItem) || !canInsertIntoFunnel(state);

		if (hit.b() == getFunnelFacing(state) && !shouldntInsertItem) {
			if (!worldIn.v)
				withTileEntityDo(worldIn, pos, te -> {
					ItemCooldownManager toInsert = heldItem.i();
					ItemCooldownManager remainder = tryInsert(worldIn, pos, toInsert, false);
					if (!ItemCooldownManager.b(remainder, toInsert))
						player.a(handIn, remainder);
				});
			return Difficulty.SUCCESS;
		}

		return Difficulty.PASS;
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, apx entityIn) {
		if (worldIn.v)
			return;
		if (!(entityIn instanceof PaintingEntity))
			return;
		if (!canInsertIntoFunnel(state))
			return;
		if (!entityIn.aW())
			return;
		PaintingEntity itemEntity = (PaintingEntity) entityIn;

		Direction direction = state.c(SHAPE);
		EntityHitResult diff = entityIn.cz()
			.d(VecHelper.getCenterOf(pos));
		double projectedDiff = direction.getAxis()
			.choose(diff.entity, diff.c, diff.d);
		if (projectedDiff < 0 == (direction.getDirection() == AxisDirection.POSITIVE))
			return;

		ItemCooldownManager toInsert = itemEntity.g();
		ItemCooldownManager remainder = tryInsert(worldIn, pos, toInsert, false);

		if (remainder.a())
			itemEntity.ac();
		if (remainder.E() < toInsert.E())
			itemEntity.b(remainder);
	}

	public static ItemCooldownManager tryInsert(GameMode worldIn, BlockPos pos, ItemCooldownManager toInsert, boolean simulate) {
		FilteringBehaviour filter = TileEntityBehaviour.get(worldIn, pos, FilteringBehaviour.TYPE);
		InvManipulationBehaviour inserter = TileEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
		if (inserter == null)
			return toInsert;
		if (filter != null && !filter.test(toInsert))
			return toInsert;
		if (simulate)
			inserter.simulate();
		ItemCooldownManager insert = inserter.insert(toInsert);

		if (!simulate && insert.E() != toInsert.E()) {
			BeehiveBlockEntity tileEntity = worldIn.c(pos);
			if (tileEntity instanceof FunnelTileEntity)
				((FunnelTileEntity) tileEntity).onTransfer(toInsert);
		}

		return insert;
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.FUNNEL.create();
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic world, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.FUNNEL.get(state.c(SHAPE));
	}

	@Override
	public VoxelShapes c(PistonHandler state, MobSpawnerLogic world, BlockPos pos, ArrayVoxelShape context) {
		if (context.getEntity() instanceof PaintingEntity)
			return AllShapes.FUNNEL_COLLISION.get(state.c(SHAPE));
		return b(state, world, pos, context);
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction direction, PistonHandler p_196271_3_, GrassColors world,
		BlockPos pos, BlockPos p_196271_6_) {
		Direction facing = state.c(SHAPE);
		if (facing.getAxis()
			.isHorizontal()) {
			if (direction == Direction.DOWN) {
				PistonHandler equivalentFunnel = getEquivalentBeltFunnel(null, null, state);
				if (BeltFunnelBlock.isOnValidBelt(equivalentFunnel, world, pos))
					return equivalentFunnel.a(BeltFunnelBlock.SHAPE,
						BeltFunnelBlock.getShapeForPosition(world, pos, facing));
			}
		}
		return state;
	}

	public abstract PistonHandler getEquivalentBeltFunnel(MobSpawnerLogic world, BlockPos pos, PistonHandler state);

	@Override
	public boolean a(PistonHandler state, ItemConvertible world, BlockPos pos) {
		BeetrootsBlock block = world.d_(pos.offset(state.c(SHAPE)
			.getOpposite()))
			.b();
		return !(block instanceof FunnelBlock) && !(block instanceof BeltFunnelBlock);
	}

	@Nullable
	public static Direction getFunnelFacing(PistonHandler state) {
		if (BlockHelper.hasBlockStateProperty(state, SHAPE))
			return state.c(SHAPE);
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.O))
			return state.c(BambooLeaves.O);
		return null;
	}

	@Override
	public void a(PistonHandler p_196243_1_, GameMode p_196243_2_, BlockPos p_196243_3_, PistonHandler p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity() && (p_196243_1_.b() != p_196243_4_.b() && !isFunnel(p_196243_4_)
			|| !p_196243_4_.hasTileEntity())) {
			TileEntityBehaviour.destroy(p_196243_2_, p_196243_3_, FilteringBehaviour.TYPE);
			p_196243_2_.o(p_196243_3_);
		}
	}

	protected boolean canInsertIntoFunnel(PistonHandler state) {
		return !state.c(POWERED);
	}

	@Nullable
	public static boolean isFunnel(PistonHandler state) {
		return state.b() instanceof FunnelBlock || state.b() instanceof BeltFunnelBlock;
	}

	@Override
	public Class<FunnelTileEntity> getTileEntityClass() {
		return FunnelTileEntity.class;
	}

}
