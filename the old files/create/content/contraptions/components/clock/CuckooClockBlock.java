package com.simibubi.kinetic_api.content.contraptions.components.clock;

import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.HorizontalKineticBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.MobSpawnerLogic;

public class CuckooClockBlock extends HorizontalKineticBlock {

	private boolean mysterious;

	public static CuckooClockBlock regular(c properties) {
		return new CuckooClockBlock(false, properties);
	}
	
	public static CuckooClockBlock mysterious(c properties) {
		return new CuckooClockBlock(true, properties);
	}
	
	protected CuckooClockBlock(boolean mysterious, c properties) {
		super(properties);
		this.mysterious = mysterious;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.CUCKOO_CLOCK.create();
	}
	
	@Override
	public VoxelShapes b(PistonHandler p_220053_1_, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return AllShapes.CUCKOO_CLOCK;
	}

	@Override
	public void a(ChorusFruitItem group, DefaultedList<ItemCooldownManager> items) {
		if (!mysterious)
			super.a(group, items);
	}
	
	@Override
	public PistonHandler a(PotionUtil context) {
		Direction preferred = getPreferredHorizontalFacing(context);
		if (preferred != null)
			return n().a(HORIZONTAL_FACING, preferred.getOpposite());
		return this.n().a(HORIZONTAL_FACING, context.f().getOpposite());
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face == state.c(HORIZONTAL_FACING).getOpposite();
	}

	public static boolean containsSurprise(PistonHandler state) {
		BeetrootsBlock block = state.b();
		return block instanceof CuckooClockBlock && ((CuckooClockBlock) block).mysterious;
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(HORIZONTAL_FACING).getAxis();
	}

}
