package com.simibubi.kinetic_api.content.logistics.block.chute;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.content.logistics.block.chute.ChuteBlock.Shape;
import dco;
import ddb;

public class ChuteShapes {

	static Map<PistonHandler, VoxelShapes> cache = new HashMap<>();
	static Map<PistonHandler, VoxelShapes> collisionCache = new HashMap<>();

	public static final VoxelShapes INTERSECTION_MASK = BeetrootsBlock.a(0, -16, 0, 16, 16, 16);
	public static final VoxelShapes COLLISION_MASK = BeetrootsBlock.a(0, 0, 0, 16, 24, 16);

	public static VoxelShapes createShape(PistonHandler state) {
		Direction direction = state.c(ChuteBlock.FACING);
		Shape shape = state.c(ChuteBlock.SHAPE);

		boolean intersection = shape == Shape.INTERSECTION;
		if (direction == Direction.DOWN)
			return intersection ? ddb.b() : AllShapes.CHUTE;

		VoxelShapes combineWith = intersection ? ddb.b() : ddb.a();
		VoxelShapes result = ddb.a(combineWith, AllShapes.CHUTE_SLOPE.get(direction));
		if (intersection)
			result = ddb.b(INTERSECTION_MASK, result, dco.i);
		return result;
	}

	public static VoxelShapes getShape(PistonHandler state) {
		if (cache.containsKey(state))
			return cache.get(state);
		VoxelShapes createdShape = createShape(state);
		cache.put(state, createdShape);
		return createdShape;
	}

	public static VoxelShapes getCollisionShape(PistonHandler state) {
		if (collisionCache.containsKey(state))
			return collisionCache.get(state);
		VoxelShapes createdShape = ddb.b(COLLISION_MASK, getShape(state), dco.i);
		collisionCache.put(state, createdShape);
		return createdShape;
	}

	public static final VoxelShapes PANEL = BeetrootsBlock.a(1, -15, 0, 15, 4, 1);

	public static VoxelShapes createSlope() {
		VoxelShapes shape = ddb.a();
		for (int i = 0; i < 16; i++) {
			float offset = i / 16f;
			shape = ddb.a(shape, PANEL.a(0, offset, offset), dco.o);
		}
		return shape;
	}

}
