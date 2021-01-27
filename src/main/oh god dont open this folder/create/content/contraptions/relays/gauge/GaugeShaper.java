package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.Arrays;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.utility.VoxelShaper;

public class GaugeShaper extends VoxelShaper {

	private VoxelShaper axisFalse, axisTrue;

	static GaugeShaper make(){
		GaugeShaper shaper = new GaugeShaper();
		shaper.axisFalse = forDirectional(AllShapes.GAUGE_SHAPE_UP, Direction.UP);
		shaper.axisTrue = forDirectional(rotatedCopy(AllShapes.GAUGE_SHAPE_UP, new EntityHitResult(0, 90, 0)), Direction.UP);
		//shapes for X axis need to be swapped
		Arrays.asList(Direction.EAST, Direction.WEST).forEach(direction -> {
			VoxelShapes mem = shaper.axisFalse.get(direction);
			shaper.axisFalse.withShape(shaper.axisTrue.get(direction), direction);
			shaper.axisTrue.withShape(mem, direction);
		});
		return shaper;
	}

	public VoxelShapes get(Direction direction, boolean axisAlong) {
		return (axisAlong ? axisTrue : axisFalse).get(direction);
	}
}