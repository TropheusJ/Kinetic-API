package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.item.ItemDescription.Palette;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.common.ToolType;

public abstract class KineticBlock extends BeetrootsBlock implements IRotate {

	protected static final Palette color = Palette.Red;

	public KineticBlock(c properties) {
		super(properties);
	}

	@Override
	public ToolType getHarvestTool(PistonHandler state) {
		return null;
	}

	@Override
	public boolean canHarvestBlock(PistonHandler state, MobSpawnerLogic world, BlockPos pos, PlayerAbilities player) {
		for (ToolType toolType : player.dC().getToolTypes()) {
			if (isToolEffective(state, toolType))
				return true;
		}
		return super.canHarvestBlock(state, world, pos, player);
	}

	@Override
	public boolean isToolEffective(PistonHandler state, ToolType tool) {
		return tool == ToolType.AXE || tool == ToolType.PICKAXE;
	}

	@Override
	public void b(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		// onBlockAdded is useless for init, as sometimes the TE gets re-instantiated
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return false;
	}

	@Override
	public boolean hasIntegratedCogwheel(ItemConvertible world, BlockPos pos, PistonHandler state) {
		return false;
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public abstract BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world);

	// TODO 1.16 is this the right replacement for updateNeighbors?
	@Override
	@Deprecated
	public void a(PistonHandler stateIn, GrassColors worldIn, BlockPos pos, int flags, int count) {
		super.a(stateIn, worldIn, pos, flags, count);
		if (worldIn.s_())
			return;

		BeehiveBlockEntity tileEntity = worldIn.c(pos);
		if (!(tileEntity instanceof KineticTileEntity))
			return;

		// Remove previous information when block is added
		KineticTileEntity kte = (KineticTileEntity) tileEntity;
		kte.warnOfMovement();
		kte.clearKineticInformation();
		kte.updateSpeed = true;
	}

	@Override
	public void a(GameMode worldIn, BlockPos pos, PistonHandler state, SaddledComponent placer, ItemCooldownManager stack) {
		if (worldIn.v)
			return;

		BeehiveBlockEntity tileEntity = worldIn.c(pos);
		if (!(tileEntity instanceof KineticTileEntity))
			return;

		KineticTileEntity kte = (KineticTileEntity) tileEntity;
		kte.effects.queueRotationIndicators();
	}

	public float getParticleTargetRadius() {
		return .65f;
	}

	public float getParticleInitialRadius() {
		return .75f;
	}

}
