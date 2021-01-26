package com.simibubi.create.content.curiosities.zapper;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class ZapperLog {

	private GameMode activeWorld;
	private List<List<c>> log = new LinkedList<>();
//	private int redoIndex;

	/*
	 * Undo and redo operations applied by tools what information is necessary?
	 * 
	 * For survival mode: does undo have the required blocks
	 * 
	 * For creative mode: what data did removed TEs have
	 * 
	 * When undo: remove added blocks (added -> air) replace replaced blocks (added
	 * -> before) add removed blocks (air -> before)
	 * 
	 */

	public void record(GameMode world, List<BlockPos> positions) {
//		if (maxLogLength() == 0)
//			return;
		if (world != activeWorld)
			log.clear();
		activeWorld = world;

		List<c> blocks = positions.stream().map(pos -> {
			BeehiveBlockEntity tileEntity = world.c(pos);
			return new c(pos, world.d_(pos), tileEntity == null ? null : tileEntity.serializeNBT());
		}).collect(Collectors.toList());

		log.add(0, blocks);
//		redoIndex = 0;

//		if (maxLogLength() < log.size())
//			log.remove(log.size() - 1);
	}

//	protected Integer maxLogLength() {
//		return AllConfigs.SERVER.curiosities.zapperUndoLogLength.get();
//	}

	public void undo() {

	}

	public void redo() {

	}

}
