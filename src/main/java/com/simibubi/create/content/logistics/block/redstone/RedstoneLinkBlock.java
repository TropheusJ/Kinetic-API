package com.simibubi.create.content.logistics.block.redstone;

import bnx;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class RedstoneLinkBlock extends ProperDirectionalBlock implements ITE<RedstoneLinkTileEntity> {

	public static final BedPart POWERED = BambooLeaves.w;
	public static final BedPart RECEIVER = BedPart.a("receiver");

	public RedstoneLinkBlock(c properties) {
		super(properties);
		j(n().a(POWERED, false)
			.a(RECEIVER, false));
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
		boolean isMoving) {
		Direction blockFacing = state.c(SHAPE);

		if (fromPos.equals(pos.offset(blockFacing.getOpposite()))) {
			if (!a(state, worldIn, pos)) {
				worldIn.b(pos, true);
				return;
			}
		}

		updateTransmittedSignal(state, worldIn, pos, blockFacing);
	}

	@Override
	public void b(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		updateTransmittedSignal(state, worldIn, pos, state.c(SHAPE));
	}

	private void updateTransmittedSignal(PistonHandler state, GameMode worldIn, BlockPos pos, Direction blockFacing) {
		if (worldIn.v)
			return;
		if (state.c(RECEIVER))
			return;

		int power = getPower(worldIn, pos);

		boolean previouslyPowered = state.c(POWERED);
		if (previouslyPowered != power > 0)
			worldIn.a(pos, state.a(POWERED), 2);

		int transmit = power;
		withTileEntityDo(worldIn, pos, te -> te.transmit(transmit));
	}

	private int getPower(GameMode worldIn, BlockPos pos) {
		int power = 0;
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.b(pos.offset(direction), direction), power);
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.b(pos.offset(direction), Direction.UP), power);
		return power;
	}

	@Override
	public boolean b_(PistonHandler state) {
		return state.c(POWERED) && state.c(RECEIVER);
	}

	@Override
	public int b(PistonHandler blockState, MobSpawnerLogic blockAccess, BlockPos pos, Direction side) {
		if (side != blockState.c(SHAPE))
			return 0;
		return a(blockState, blockAccess, pos, side);
	}

	@Override
	public int a(PistonHandler state, MobSpawnerLogic blockAccess, BlockPos pos, Direction side) {
		if (!state.c(RECEIVER))
			return 0;
		try {
			RedstoneLinkTileEntity tileEntity = getTileEntity(blockAccess, pos);
			return tileEntity.getReceivedSignal();
		} catch (TileEntityException e) {
		}
		return 0;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(POWERED, RECEIVER);
		super.a(builder);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.REDSTONE_LINK.create();
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		if (player.bt())
			return toggleMode(state, worldIn, pos);
		return Difficulty.PASS;
	}

	public Difficulty toggleMode(PistonHandler state, GameMode worldIn, BlockPos pos) {
		if (worldIn.v)
			return Difficulty.SUCCESS;
		try {
			RedstoneLinkTileEntity te = getTileEntity(worldIn, pos);
			Boolean wasReceiver = state.c(RECEIVER);
			boolean blockPowered = worldIn.r(pos);
			worldIn.a(pos, state.a(RECEIVER)
				.a(POWERED, blockPowered), 3);
			te.transmit(wasReceiver ? 0 : getPower(worldIn, pos));
			return Difficulty.SUCCESS;
		} catch (TileEntityException e) {
		}
		return Difficulty.PASS;
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		if (toggleMode(state, context.p(), context.a()) == Difficulty.SUCCESS)
			return Difficulty.SUCCESS;
		return super.onWrenched(state, context);
	}

	@Override
	public PistonHandler getRotatedBlockState(PistonHandler originalState, Direction _targetedFace) {
		return originalState;
	}

	@Override
	public boolean canConnectRedstone(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction side) {
		return side != null;
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		BlockPos neighbourPos = pos.offset(state.c(SHAPE)
			.getOpposite());
		PistonHandler neighbour = worldIn.d_(neighbourPos);
		if (FunnelBlock.isFunnel(neighbour))
			return true;
		return BlockHelper.hasBlockSolidSide(neighbour, worldIn, neighbourPos, state.c(SHAPE));
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		PistonHandler state = n();
		state = state.a(SHAPE, context.j());
		return state;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.REDSTONE_BRIDGE.get(state.c(SHAPE));
	}

	@Override
	public Class<RedstoneLinkTileEntity> getTileEntityClass() {
		return RedstoneLinkTileEntity.class;
	}

}
