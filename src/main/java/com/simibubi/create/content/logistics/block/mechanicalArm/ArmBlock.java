package com.simibubi.create.content.logistics.block.mechanicalArm;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity.Phase;
import com.simibubi.create.foundation.block.ITE;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class ArmBlock extends KineticBlock implements ITE<ArmTileEntity> {

	public static final BedPart CEILING = BedPart.a("ceiling");

	public ArmBlock(c properties) {
		super(properties);
		j(n().a(CEILING, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> p_206840_1_) {
		super.a(p_206840_1_.a(CEILING));
	}

	@Override
	public PistonHandler a(PotionUtil ctx) {
		return n().a(CEILING, ctx.j() == Direction.DOWN);
	}

	@Override
	public boolean hasIntegratedCogwheel(ItemConvertible world, BlockPos pos, PistonHandler state) {
		return true;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return state.c(CEILING) ? AllShapes.MECHANICAL_ARM_CEILING : AllShapes.MECHANICAL_ARM;
	}
	
	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		super.b(state, world, pos, oldState, isMoving);
		withTileEntityDo(world, pos, ArmTileEntity::redstoneUpdate);
	}
	
	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, BeetrootsBlock p_220069_4_,
		BlockPos p_220069_5_, boolean p_220069_6_) {
		withTileEntityDo(world, pos, ArmTileEntity::redstoneUpdate);
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return Axis.Y;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.MECHANICAL_ARM.create();
	}

	@Override
	public Class<ArmTileEntity> getTileEntityClass() {
		return ArmTileEntity.class;
	}

	@Override
	public void a(PistonHandler p_196243_1_, GameMode world, BlockPos pos, PistonHandler p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity()
			&& (p_196243_1_.b() != p_196243_4_.b() || !p_196243_4_.hasTileEntity())) {
			withTileEntityDo(world, pos, te -> {
				if (!te.heldItem.a())
					Inventory.a(world, pos.getX(), pos.getY(), pos.getZ(), te.heldItem);
			});
			world.o(pos);
		}
	}

	@Override
	public Difficulty a(PistonHandler p_225533_1_, GameMode world, BlockPos pos, PlayerAbilities player,
		ItemScatterer p_225533_5_, dcg p_225533_6_) {
		MutableBoolean success = new MutableBoolean(false);
		withTileEntityDo(world, pos, te -> {
			if (te.heldItem.a())
				return;
			success.setTrue();
			if (world.v)
				return;
			player.bm.a(world, te.heldItem);
			te.heldItem = ItemCooldownManager.tick;
			te.phase = Phase.SEARCH_INPUTS;
			te.X_();
			te.sendData();
		});
		
		return success.booleanValue() ? Difficulty.SUCCESS : Difficulty.PASS;
	}

}
