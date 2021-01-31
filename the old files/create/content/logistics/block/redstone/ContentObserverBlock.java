package com.simibubi.kinetic_api.content.logistics.block.redstone;

import java.util.Random;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.wrench.IWrenchable;
import com.simibubi.kinetic_api.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.HayBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.items.CapabilityItemHandler;

public class ContentObserverBlock extends HayBlock implements ITE<ContentObserverTileEntity>, IWrenchable {

	public static final BedPart POWERED = BambooLeaves.w;

	public ContentObserverBlock(c properties) {
		super(properties);
		j(n().a(POWERED, false));
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return AllShapes.CONTENT_OBSERVER.get(state.c(aq));
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.CONTENT_OBSERVER.create();
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(POWERED, aq);
		super.a(builder);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		PistonHandler state = n();

		Direction preferredFacing = null;
		for (Direction face : Iterate.horizontalDirections) {
			BlockPos offsetPos = context.a()
				.offset(face);
			GameMode world = context.p();
			boolean canDetect = false;
			BeehiveBlockEntity tileEntity = world.c(offsetPos);

			if (TileEntityBehaviour.get(tileEntity, TransportedItemStackHandlerBehaviour.TYPE) != null)
				canDetect = true;
			else if (tileEntity != null && tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.isPresent())
				canDetect = true;
			else if (tileEntity instanceof FunnelTileEntity)
				canDetect = true;

			if (canDetect) {
				if (preferredFacing != null) {
					preferredFacing = null;
					break;
				}
				preferredFacing = face;
			}

		}

		if (preferredFacing != null)
			return state.a(aq, preferredFacing);
		return state.a(aq, context.f()
			.getOpposite());
	}

	@Override
	public boolean b_(PistonHandler state) {
		return state.c(POWERED);
	}

	@Override
	public int a(PistonHandler blockState, MobSpawnerLogic blockAccess, BlockPos pos, Direction side) {
		return b_(blockState) && (side == null || side != blockState.c(aq)
			.getOpposite()) ? 15 : 0;
	}

	@Override
	public void a(PistonHandler state, ServerWorld worldIn, BlockPos pos, Random random) {
		worldIn.a(pos, state.a(POWERED, false), 2);
		worldIn.b(pos, this);
	}

	@Override
	public boolean canConnectRedstone(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction side) {
		return side != state.c(aq)
			.getOpposite();
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (state.hasTileEntity() && state.b() != newState.b()) {
			TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
			worldIn.o(pos);
		}
	}

	public void onFunnelTransfer(GameMode world, BlockPos funnelPos, ItemCooldownManager transferred) {
		for (Direction direction : Iterate.horizontalDirections) {
			BlockPos detectorPos = funnelPos.offset(direction);
			PistonHandler detectorState = world.d_(detectorPos);
			if (!AllBlocks.CONTENT_OBSERVER.has(detectorState))
				continue;
			if (detectorState.c(aq) != direction.getOpposite())
				continue;
			withTileEntityDo(world, detectorPos, te -> {
				FilteringBehaviour filteringBehaviour = TileEntityBehaviour.get(te, FilteringBehaviour.TYPE);
				if (filteringBehaviour == null)
					return;
				if (!filteringBehaviour.test(transferred))
					return;
				te.activate(4);
			});
		}
	}

	@Override
	public Class<ContentObserverTileEntity> getTileEntityClass() {
		return ContentObserverTileEntity.class;
	}

}
