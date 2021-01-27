package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class AdjustablePulleyBlock extends EncasedBeltBlock implements ITE<AdjustablePulleyTileEntity> {

	public static BedPart POWERED = BambooLeaves.w;

	public AdjustablePulleyBlock(c properties) {
		super(properties);
		j(n().a(POWERED, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(POWERED));
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ADJUSTABLE_PULLEY.create();
	}

	@Override
	public void b(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		if (oldState.b() == state.b())
			return;
		withTileEntityDo(worldIn, pos, AdjustablePulleyTileEntity::neighborChanged);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		return super.a(context).a(POWERED, context.p().r(context.a()));
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.v)
			return;

		withTileEntityDo(worldIn, pos, AdjustablePulleyTileEntity::neighborChanged);

		boolean previouslyPowered = state.c(POWERED);
		if (previouslyPowered != worldIn.r(pos))
			worldIn.a(pos, state.a(POWERED), 18);
	}

	@Override
	public Class<AdjustablePulleyTileEntity> getTileEntityClass() {
		return AdjustablePulleyTileEntity.class;
	}

}
