package com.simibubi.kinetic_api.content.curiosities.symmetry.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class SymmetryMirror {

	public static final String EMPTY = "empty";
	public static final String PLANE = "plane";
	public static final String CROSS_PLANE = "cross_plane";
	public static final String TRIPLE_PLANE = "triple_plane";

	protected EntityHitResult position;
	protected SmoothUtil orientation;
	protected int orientationIndex;
	public boolean enable;

	public SymmetryMirror(EntityHitResult pos) {
		position = pos;
		enable = true;
		orientationIndex = 0;
	}

	public static List<Text> getMirrors() {
		return ImmutableList.of(Lang.translate("symmetry.mirror.plane"), Lang.translate("symmetry.mirror.doublePlane"),
			Lang.translate("symmetry.mirror.triplePlane"));
	}

	public SmoothUtil getOrientation() {
		return orientation;
	}

	public EntityHitResult getPosition() {
		return position;
	}

	public int getOrientationIndex() {
		return orientationIndex;
	}

	public void rotate(boolean forward) {
		orientationIndex += forward ? 1 : -1;
		setOrientation();
	}

	public void process(Map<BlockPos, PistonHandler> blocks) {
		Map<BlockPos, PistonHandler> result = new HashMap<>();
		for (BlockPos pos : blocks.keySet()) {
			result.putAll(process(pos, blocks.get(pos)));
		}
		blocks.putAll(result);
	}

	public abstract Map<BlockPos, PistonHandler> process(BlockPos position, PistonHandler block);

	protected abstract void setOrientation();

	public abstract void setOrientation(int index);

	public abstract String typeName();

	public abstract AllBlockPartials getModel();

	public void applyModelTransform(BufferVertexConsumer ms) {}

	private static final String $ORIENTATION = "direction";
	private static final String $POSITION = "pos";
	private static final String $TYPE = "type";
	private static final String $ENABLE = "enable";

	public CompoundTag writeToNbt() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt($ORIENTATION, orientationIndex);

		ListTag floatList = new ListTag();
		floatList.add(FloatTag.of((float) position.entity));
		floatList.add(FloatTag.of((float) position.c));
		floatList.add(FloatTag.of((float) position.d));
		nbt.put($POSITION, floatList);
		nbt.putString($TYPE, typeName());
		nbt.putBoolean($ENABLE, enable);

		return nbt;
	}

	public static SymmetryMirror fromNBT(CompoundTag nbt) {
		ListTag floatList = nbt.getList($POSITION, 5);
		EntityHitResult pos = new EntityHitResult(floatList.getFloat(0), floatList.getFloat(1), floatList.getFloat(2));
		SymmetryMirror element;

		switch (nbt.getString($TYPE)) {
		case PLANE:
			element = new PlaneMirror(pos);
			break;
		case CROSS_PLANE:
			element = new CrossPlaneMirror(pos);
			break;
		case TRIPLE_PLANE:
			element = new TriplePlaneMirror(pos);
			break;
		default:
			element = new EmptyMirror(pos);
			break;
		}

		element.setOrientation(nbt.getInt($ORIENTATION));
		element.enable = nbt.getBoolean($ENABLE);

		return element;
	}

	protected EntityHitResult getDiff(BlockPos position) {
		return this.position.a(-1)
			.b(position.getX(), position.getY(), position.getZ());
	}

	protected BlockPos getIDiff(BlockPos position) {
		EntityHitResult diff = getDiff(position);
		return new BlockPos((int) diff.entity, (int) diff.c, (int) diff.d);
	}

	protected PistonHandler flipX(PistonHandler in) {
		return in.a(LoomBlock.c);
	}

	protected PistonHandler flipY(PistonHandler in) {
		for (IntProperty<?> property : in.r()) {

			if (property == BambooLeaves.ab)
				return in.a(property);
			// Directional Blocks
			if (property instanceof BooleanProperty) {
				if (in.c(property) == Direction.DOWN) {
					return in.a((BooleanProperty) property, Direction.UP);
				} else if (in.c(property) == Direction.UP) {
					return in.a((BooleanProperty) property, Direction.DOWN);
				}
			}
		}
		return in;
	}

	protected PistonHandler flipZ(PistonHandler in) {
		return in.a(LoomBlock.b);
	}

	protected PistonHandler flipD1(PistonHandler in) {
		return in.a(RespawnAnchorBlock.d)
			.a(LoomBlock.c);
	}

	protected PistonHandler flipD2(PistonHandler in) {
		return in.a(RespawnAnchorBlock.d)
			.a(LoomBlock.b);
	}

	protected BlockPos flipX(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX() - 2 * diff.getX(), position.getY(), position.getZ());
	}

	protected BlockPos flipY(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX(), position.getY() - 2 * diff.getY(), position.getZ());
	}

	protected BlockPos flipZ(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX(), position.getY(), position.getZ() - 2 * diff.getZ());
	}

	protected BlockPos flipD2(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX() - diff.getX() + diff.getZ(), position.getY(),
			position.getZ() - diff.getZ() + diff.getX());
	}

	protected BlockPos flipD1(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX() - diff.getX() - diff.getZ(), position.getY(),
			position.getZ() - diff.getZ() - diff.getX());
	}

	public void setPosition(EntityHitResult pos3d) {
		this.position = pos3d;
	}

	public abstract List<Text> getAlignToolTips();

}
