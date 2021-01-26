package com.simibubi.create.content.curiosities.symmetry.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

public class TriplePlaneMirror extends SymmetryMirror {

	public TriplePlaneMirror(EntityHitResult pos) {
		super(pos);
		orientationIndex = 0;
	}

	@Override
	public Map<BlockPos, PistonHandler> process(BlockPos position, PistonHandler block) {
		Map<BlockPos, PistonHandler> result = new HashMap<>();

		result.put(flipX(position), flipX(block));
		result.put(flipZ(position), flipZ(block));
		result.put(flipX(flipZ(position)), flipX(flipZ(block)));

		result.put(flipD1(position), flipD1(block));
		result.put(flipD1(flipX(position)), flipD1(flipX(block)));
		result.put(flipD1(flipZ(position)), flipD1(flipZ(block)));
		result.put(flipD1(flipX(flipZ(position))), flipD1(flipX(flipZ(block))));

		return result;
	}

	@Override
	public String typeName() {
		return TRIPLE_PLANE;
	}

	@Override
	public AllBlockPartials getModel() {
		return AllBlockPartials.SYMMETRY_TRIPLEPLANE;
	}

	@Override
	protected void setOrientation() {
	}

	@Override
	public void setOrientation(int index) {
	}
	
	@Override
	public SmoothUtil getOrientation() {
		return CrossPlaneMirror.Align.Y;
	}
	
	@Override
	public List<Text> getAlignToolTips() {
		return ImmutableList.of(Lang.translate("orientation.horizontal"));
	}

}
