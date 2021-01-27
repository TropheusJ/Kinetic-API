package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.Iterate;
import dcg;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.common.Tags;

public abstract class AbstractChassisBlock extends RepeaterBlock implements IWrenchable {

	public AbstractChassisBlock(c properties) {
		super(properties);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.CHASSIS.create();
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		if (!player.eJ())
			return Difficulty.PASS;

		ItemCooldownManager heldItem = player.b(handIn);
		boolean isSlimeBall = heldItem.b()
			.a(Tags.Items.SLIMEBALLS) || AllItems.SUPER_GLUE.isIn(heldItem);

		BedPart affectedSide = getGlueableSide(state, hit.b());
		if (affectedSide == null)
			return Difficulty.PASS;

		if (isSlimeBall && state.c(affectedSide)) {
			for (Direction face : Iterate.directions) {
				BedPart glueableSide = getGlueableSide(state, face);
				if (glueableSide != null && !state.c(glueableSide)) {
					if (worldIn.v) {
						EntityHitResult vec = hit.e();
						worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.entity, vec.c, vec.d, 0, 0, 0);
						return Difficulty.SUCCESS;
					}
					worldIn.a(null, pos, AllSoundEvents.SLIME_ADDED.get(), SoundEvent.e, .5f, 1);
					state = state.a(glueableSide, true);
				}
			}
			if (!worldIn.v)
				worldIn.a(pos, state);
			return Difficulty.SUCCESS;
		}

		if ((!heldItem.a() || !player.bt()) && !isSlimeBall)
			return Difficulty.PASS;
		if (state.c(affectedSide) == isSlimeBall)
			return Difficulty.PASS;
		if (worldIn.v) {
			EntityHitResult vec = hit.e();
			worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.entity, vec.c, vec.d, 0, 0, 0);
			return Difficulty.SUCCESS;
		}

		worldIn.a(null, pos, AllSoundEvents.SLIME_ADDED.get(), SoundEvent.e, .5f, 1);
		worldIn.a(pos, state.a(affectedSide, isSlimeBall));
		return Difficulty.SUCCESS;
	}

	@SuppressWarnings("deprecation")
	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rotation) {
		if (rotation == RespawnAnchorBlock.CHARGES)
			return state;

		PistonHandler rotated = super.a(state, rotation);
		for (Direction face : Iterate.directions) {
			BedPart glueableSide = getGlueableSide(rotated, face);
			if (glueableSide != null)
				rotated = rotated.a(glueableSide, false);
		}

		for (Direction face : Iterate.directions) {
			BedPart glueableSide = getGlueableSide(state, face);
			if (glueableSide == null || !state.c(glueableSide))
				continue;
			Direction rotatedFacing = rotation.a(face);
			BedPart rotatedGlueableSide = getGlueableSide(rotated, rotatedFacing);
			if (rotatedGlueableSide != null)
				rotated = rotated.a(rotatedGlueableSide, true);
		}

		return rotated;
	}

	@Override
	public PistonHandler a(PistonHandler state, LoomBlock mirrorIn) {
		if (mirrorIn == LoomBlock.TITLE)
			return state;

		PistonHandler mirrored = state;
		for (Direction face : Iterate.directions) {
			BedPart glueableSide = getGlueableSide(mirrored, face);
			if (glueableSide != null)
				mirrored = mirrored.a(glueableSide, false);
		}

		for (Direction face : Iterate.directions) {
			BedPart glueableSide = getGlueableSide(state, face);
			if (glueableSide == null || !state.c(glueableSide))
				continue;
			Direction mirroredFacing = mirrorIn.b(face);
			BedPart mirroredGlueableSide = getGlueableSide(mirrored, mirroredFacing);
			if (mirroredGlueableSide != null)
				mirrored = mirrored.a(mirroredGlueableSide, true);
		}

		return mirrored;
	}

	public abstract BedPart getGlueableSide(PistonHandler state, Direction face);

}
