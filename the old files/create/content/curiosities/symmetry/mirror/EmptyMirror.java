package com.simibubi.kinetic_api.content.curiosities.symmetry.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.simibubi.kinetic_api.AllBlockPartials;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

public class EmptyMirror extends SymmetryMirror {

	public static enum Align implements SmoothUtil {
		None("none");
		
		private final String name;
		private Align(String name) { this.name = name; }
		@Override public String a() { return name; }
		@Override public String toString() { return name; }
	}
	
	public EmptyMirror(EntityHitResult pos) {
		super(pos);
		orientation = Align.None;
	}

	@Override
	protected void setOrientation() {
	}

	@Override
	public void setOrientation(int index) {
		this.orientation = Align.values()[index];
		orientationIndex = index;
	}

	@Override
	public Map<BlockPos, PistonHandler> process(BlockPos position, PistonHandler block) {
		return new HashMap<>();
	}

	@Override
	public String typeName() {
		return EMPTY;
	}

	@Override
	public AllBlockPartials getModel() {
		return null;
	}
	
	@Override
	public List<Text> getAlignToolTips() {
		return ImmutableList.of();
	}

}
