package com.simibubi.create.content.contraptions.relays.encased;

import bnx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MobSpawnerLogic;

public class EncasedShaftBlock extends AbstractEncasedShaftBlock implements ISpecialBlockItemRequirement {

	private BlockEntry<CasingBlock> casing;

	public static EncasedShaftBlock andesite(c properties) {
		return new EncasedShaftBlock(properties, AllBlocks.ANDESITE_CASING);
	}
	
	public static EncasedShaftBlock brass(c properties) {
		return new EncasedShaftBlock(properties, AllBlocks.BRASS_CASING);
	}
	
	protected EncasedShaftBlock(c properties, BlockEntry<CasingBlock> casing) {
		super(properties);
		this.casing = casing;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ENCASED_SHAFT.create();
	}
	
	public BlockEntry<CasingBlock> getCasing() {
		return casing;
	}

	@Override
	public Difficulty onSneakWrenched(PistonHandler state, bnx context) {
		if (context.p().v)
			return Difficulty.SUCCESS;
		context.p().syncWorldEvent(2001, context.a(), BeetrootsBlock.i(state));
		KineticTileEntity.switchToBlockState(context.p(), context.a(), AllBlocks.SHAFT.getDefaultState().a(AXIS, state.c(AXIS)));
		return Difficulty.SUCCESS;
	}
	
	@Override
	public ItemRequirement getRequiredItems(PistonHandler state) {
		return ItemRequirement.of(AllBlocks.SHAFT.getDefaultState());
	}

}
