package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.chassis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.util.Pair;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllKeys;
import com.simibubi.kinetic_api.AllSpecialTextures;
import com.simibubi.kinetic_api.CreateClient;
import dcg;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;

public class ChassisRangeDisplay {

	private static final int DISPLAY_TIME = 200;
	private static GroupEntry lastHoveredGroup = null;

	private static class Entry {
		ChassisTileEntity te;
		int timer;

		public Entry(ChassisTileEntity te) {
			this.te = te;
			timer = DISPLAY_TIME;
			CreateClient.outliner.showCluster(getOutlineKey(), createSelection(te))
				.colored(0xFFFFFF)
				.disableNormals()
				.lineWidth(1 / 16f)
				.withFaceTexture(AllSpecialTextures.HIGHLIGHT_CHECKERED);
		}

		protected Object getOutlineKey() {
			return Pair.of(te.o(), 1);
		}

		protected Set<BlockPos> createSelection(ChassisTileEntity chassis) {
			Set<BlockPos> positions = new HashSet<>();
			List<BlockPos> includedBlockPositions = chassis.getIncludedBlockPositions(null, true);
			if (includedBlockPositions == null)
				return Collections.emptySet();
			positions.addAll(includedBlockPositions);
			return positions;
		}

	}

	private static class GroupEntry extends Entry {

		List<ChassisTileEntity> includedTEs;

		public GroupEntry(ChassisTileEntity te) {
			super(te);
		}

		@Override
		protected Object getOutlineKey() {
			return this;
		}

		@Override
		protected Set<BlockPos> createSelection(ChassisTileEntity chassis) {
			Set<BlockPos> list = new HashSet<>();
			includedTEs = te.collectChassisGroup();
			if (includedTEs == null)
				return list;
			for (ChassisTileEntity chassisTileEntity : includedTEs)
				list.addAll(super.createSelection(chassisTileEntity));
			return list;
		}

	}

	static Map<BlockPos, Entry> entries = new HashMap<>();
	static List<GroupEntry> groupEntries = new ArrayList<>();

	public static void tick() {
		PlayerAbilities player = KeyBinding.B().s;
		GameMode world = KeyBinding.B().r;
		boolean hasWrench = AllItems.WRENCH.isIn(player.dC());

		for (Iterator<BlockPos> iterator = entries.keySet()
			.iterator(); iterator.hasNext();) {
			BlockPos pos = iterator.next();
			Entry entry = entries.get(pos);
			if (tickEntry(entry, hasWrench))
				iterator.remove();
			CreateClient.outliner.keep(entry.getOutlineKey());
		}

		for (Iterator<GroupEntry> iterator = groupEntries.iterator(); iterator.hasNext();) {
			GroupEntry group = iterator.next();
			if (tickEntry(group, hasWrench)) {
				iterator.remove();
				if (group == lastHoveredGroup)
					lastHoveredGroup = null;
			}
			CreateClient.outliner.keep(group.getOutlineKey());
		}

		if (!hasWrench)
			return;

		Box over = KeyBinding.B().v;
		if (!(over instanceof dcg))
			return;
		dcg ray = (dcg) over;
		BlockPos pos = ray.a();
		BeehiveBlockEntity tileEntity = world.c(pos);
		if (tileEntity == null || tileEntity.q())
			return;
		if (!(tileEntity instanceof ChassisTileEntity))
			return;

		boolean ctrl = AllKeys.ctrlDown();
		ChassisTileEntity chassisTileEntity = (ChassisTileEntity) tileEntity;

		if (ctrl) {
			GroupEntry existingGroupForPos = getExistingGroupForPos(pos);
			if (existingGroupForPos != null) {
				for (ChassisTileEntity included : existingGroupForPos.includedTEs)
					entries.remove(included.o());
				existingGroupForPos.timer = DISPLAY_TIME;
				return;
			}
		}

		if (!entries.containsKey(pos) || ctrl)
			display(chassisTileEntity);
		else {
			if (!ctrl)
				entries.get(pos).timer = DISPLAY_TIME;
		}
	}

	private static boolean tickEntry(Entry entry, boolean hasWrench) {
		ChassisTileEntity chassisTileEntity = entry.te;
		GameMode teWorld = chassisTileEntity.v();
		GameMode world = KeyBinding.B().r;

		if (chassisTileEntity.q() || teWorld == null || teWorld != world
			|| !world.p(chassisTileEntity.o())) {
			return true;
		}

		if (!hasWrench && entry.timer > 20) {
			entry.timer = 20;
			return false;
		}

		entry.timer--;
		if (entry.timer == 0)
			return true;
		return false;
	}

	public static void display(ChassisTileEntity chassis) {

		// Display a group and kill any selections of its contained chassis blocks
		if (AllKeys.ctrlDown()) {
			GroupEntry hoveredGroup = new GroupEntry(chassis);

			for (ChassisTileEntity included : hoveredGroup.includedTEs)
				CreateClient.outliner.remove(included.o());

			groupEntries.forEach(entry -> CreateClient.outliner.remove(entry.getOutlineKey()));
			groupEntries.clear();
			entries.clear();
			groupEntries.add(hoveredGroup);
			return;
		}

		// Display an individual chassis and kill any group selections that contained it
		BlockPos pos = chassis.o();
		GroupEntry entry = getExistingGroupForPos(pos);
		if (entry != null)
			CreateClient.outliner.remove(entry.getOutlineKey());

		groupEntries.clear();
		entries.clear();
		entries.put(pos, new Entry(chassis));

	}

	private static GroupEntry getExistingGroupForPos(BlockPos pos) {
		for (GroupEntry groupEntry : groupEntries)
			for (ChassisTileEntity chassis : groupEntry.includedTEs)
				if (pos.equals(chassis.o()))
					return groupEntry;
		return null;
	}

}
