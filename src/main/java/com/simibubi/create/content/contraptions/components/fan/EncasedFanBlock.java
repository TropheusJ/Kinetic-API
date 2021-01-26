package com.simibubi.create.content.contraptions.components.fan;

import bnx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class EncasedFanBlock extends DirectionalKineticBlock implements ITE<EncasedFanTileEntity> {

	public EncasedFanBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ENCASED_FAN.create();
	}

	@Override
	public void b(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		blockUpdate(state, worldIn, pos);
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler p_196243_4_, boolean p_196243_5_) {
		if (state.hasTileEntity() && (state.b() != p_196243_4_.b() || !p_196243_4_.hasTileEntity())) {
			withTileEntityDo(world, pos, EncasedFanTileEntity::updateChute);
			world.o(pos);
		}
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
		boolean isMoving) {
		blockUpdate(state, worldIn, pos);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		GameMode world = context.p();
		BlockPos pos = context.a();
		Direction face = context.j();

		PistonHandler placedOn = world.d_(pos.offset(face.getOpposite()));
		PistonHandler placedOnOpposite = world.d_(pos.offset(face));
		if (AllBlocks.CHUTE.has(placedOn))
			return n().a(FACING, face.getOpposite());
		if (AllBlocks.CHUTE.has(placedOnOpposite))
			return n().a(FACING, face);

		Direction preferredFacing = getPreferredFacing(context);
		if (preferredFacing == null)
			preferredFacing = context.d();
		return n().a(FACING, context.n() != null && context.n()
			.bt() ? preferredFacing : preferredFacing.getOpposite());
	}

	protected void blockUpdate(PistonHandler state, GameMode worldIn, BlockPos pos) {
		if (worldIn instanceof WrappedWorld)
			return;
		notifyFanTile(worldIn, pos);
		if (worldIn.v)
			return;
		withTileEntityDo(worldIn, pos, te -> te.updateGenerator(state.c(FACING)));
	}

	protected void notifyFanTile(GrassColors world, BlockPos pos) {
		withTileEntityDo(world, pos, EncasedFanTileEntity::blockInFrontChanged);
	}

	@Override
	public PistonHandler updateAfterWrenched(PistonHandler newState, bnx context) {
		blockUpdate(newState, context.p(), context.a());
		return newState;
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(FACING)
			.getAxis();
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face == state.c(FACING)
			.getOpposite();
	}

	@Override
	public boolean showCapacityWithAnnotation() {
		return true;
	}

	@Override
	public Class<EncasedFanTileEntity> getTileEntityClass() {
		return EncasedFanTileEntity.class;
	}

}
