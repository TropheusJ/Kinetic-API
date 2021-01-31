package com.simibubi.kinetic_api.content.contraptions.relays.encased;

import java.util.Random;

import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.RotationPropagator;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.relays.gearbox.GearshiftTileEntity;
import com.simibubi.kinetic_api.foundation.block.ITE;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.gen.StructureAccessor;

public class GearshiftBlock extends AbstractEncasedShaftBlock implements ITE<GearshiftTileEntity> {

	public static final BedPart POWERED = BambooLeaves.w;

	public GearshiftBlock(c properties) {
		super(properties);
		j(n().a(POWERED, false));
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.GEARSHIFT.create();
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(POWERED);
		super.a(builder);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		return super.a(context).a(POWERED,
				context.p().r(context.a()));
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.v)
			return;

		boolean previouslyPowered = state.c(POWERED);
		if (previouslyPowered != worldIn.r(pos)) {
			detachKinetics(worldIn, pos, true);
			worldIn.a(pos, state.a(POWERED), 2);
		}
	}

	@Override
	public Class<GearshiftTileEntity> getTileEntityClass() {
		return GearshiftTileEntity.class;
	}

	public void detachKinetics(GameMode worldIn, BlockPos pos, boolean reAttachNextTick) {
		BeehiveBlockEntity te = worldIn.c(pos);
		if (te == null || !(te instanceof KineticTileEntity))
			return;
		RotationPropagator.handleRemoved(worldIn, pos, (KineticTileEntity) te);

		// Re-attach next tick
		if (reAttachNextTick)
			worldIn.I().a(pos, this, 0, StructureAccessor.world);
	}

	@Override
	public void a(PistonHandler state, ServerWorld worldIn, BlockPos pos, Random random) {
		BeehiveBlockEntity te = worldIn.c(pos);
		if (te == null || !(te instanceof KineticTileEntity))
			return;
		KineticTileEntity kte = (KineticTileEntity) te;
		RotationPropagator.handleAdded(worldIn, pos, kte);
	}
}
