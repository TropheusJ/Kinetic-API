package com.simibubi.create.content.curiosities.symmetry.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

public class PlaneMirror extends SymmetryMirror {

	public static enum Align implements SmoothUtil {
		XY("xy"), YZ("yz");

		private final String name;

		private Align(String name) {
			this.name = name;
		}

		@Override
		public String a() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public PlaneMirror(EntityHitResult pos) {
		super(pos);
		orientation = Align.XY;
	}

	@Override
	protected void setOrientation() {
		if (orientationIndex < 0)
			orientationIndex += Align.values().length;
		if (orientationIndex >= Align.values().length)
			orientationIndex -= Align.values().length;
		orientation = Align.values()[orientationIndex];
	}

	@Override
	public void setOrientation(int index) {
		this.orientation = Align.values()[index];
		orientationIndex = index;
	}

	@Override
	public Map<BlockPos, PistonHandler> process(BlockPos position, PistonHandler block) {
		Map<BlockPos, PistonHandler> result = new HashMap<>();
		switch ((Align) orientation) {

		case XY:
			result.put(flipZ(position), flipZ(block));
			break;
		case YZ:
			result.put(flipX(position), flipX(block));
			break;
		default:
			break;

		}
		return result;
	}

	@Override
	public String typeName() {
		return PLANE;
	}

	@Override
	public AllBlockPartials getModel() {
		return AllBlockPartials.SYMMETRY_PLANE;
	}

	@Override
	public void applyModelTransform(BufferVertexConsumer ms) {
		super.applyModelTransform(ms);
		MatrixStacker.of(ms)
			.centre()
			.rotateY(((Align) orientation) == Align.XY ? 0 : 90)
			.unCentre();
	}

	@Override
	public List<Text> getAlignToolTips() {
		return ImmutableList.of(Lang.translate("orientation.alongZ"), Lang.translate("orientation.alongX"));
	}

}
