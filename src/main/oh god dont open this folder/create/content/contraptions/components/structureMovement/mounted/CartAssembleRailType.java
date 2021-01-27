package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.item.HoeItem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.tracks.ControllerRailBlock;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.BlockEntry;

public enum CartAssembleRailType implements SmoothUtil {
	
	REGULAR(BellBlock.ch),
	POWERED_RAIL(BellBlock.aN),
	DETECTOR_RAIL(BellBlock.aO),
	ACTIVATOR_RAIL(BellBlock.fD),
	CONTROLLER_RAIL(AllBlocks.CONTROLLER_RAIL, blockState -> AllBlocks.CONTROLLER_RAIL.has(blockState)
		&& blockState.b(ControllerRailBlock.BACKWARDS) && !blockState.c(ControllerRailBlock.BACKWARDS)),
	CONTROLLER_RAIL_BACKWARDS(AllBlocks.CONTROLLER_RAIL, blockState -> AllBlocks.CONTROLLER_RAIL.has(blockState)
		&& blockState.b(ControllerRailBlock.BACKWARDS) && blockState.c(ControllerRailBlock.BACKWARDS))
	
	;

	private final Supplier<BeetrootsBlock> railBlockSupplier;
	private final Supplier<HoeItem> railItemSupplier;
	public final Predicate<PistonHandler> matches;

	CartAssembleRailType(BeetrootsBlock block) {
		this.railBlockSupplier = () -> block;
		this.railItemSupplier = block::h;
		this.matches = blockState -> blockState.b() == getBlock();
	}

	CartAssembleRailType(BlockEntry<?> block, Predicate<PistonHandler> matches) {
		this.railBlockSupplier = block::get;
		this.railItemSupplier = () -> block.get().h();
		this.matches = matches;
	}

	public BeetrootsBlock getBlock() {
		return railBlockSupplier.get();
	}

	public HoeItem getItem() {
		return railItemSupplier.get();
	}
	
	@Override
	public String a() {
		return Lang.asId(name());
	}

}
