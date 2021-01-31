package com.simibubi.kinetic_api.foundation.utility;

import ddb;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.VoxelShapes;
import org.apache.commons.lang3.mutable.MutableObject;

public class VoxelShaper {

	private Map<Direction, VoxelShapes> shapes = new HashMap<>();

	public VoxelShapes get(Direction direction) {
		return shapes.get(direction);
	}

	public VoxelShapes get(Axis axis) {
		return shapes.get(axisAsFace(axis));
	}

	public static VoxelShaper forHorizontal(VoxelShapes shape, Direction facing){
		return forDirectionsWithRotation(shape, facing, Direction.Type.HORIZONTAL, new HorizontalRotationValues());
	}

	public static VoxelShaper forHorizontalAxis(VoxelShapes shape, Axis along) {
		return forDirectionsWithRotation(shape, axisAsFace(along), Arrays.asList(Direction.SOUTH, Direction.EAST), new HorizontalRotationValues());
	}

	public static VoxelShaper forDirectional(VoxelShapes shape, Direction facing){
		return forDirectionsWithRotation(shape, facing, Arrays.asList(Iterate.directions), new DefaultRotationValues());
	}

	public static VoxelShaper forAxis(VoxelShapes shape, Axis along){
		return forDirectionsWithRotation(shape, axisAsFace(along), Arrays.asList(Direction.SOUTH, Direction.EAST, Direction.UP), new DefaultRotationValues());
	}

	public VoxelShaper withVerticalShapes(VoxelShapes upShape) {
		shapes.put(Direction.UP, upShape);
		shapes.put(Direction.DOWN, rotatedCopy(upShape, new EntityHitResult(180, 0, 0)));
		return this;
	}

	public VoxelShaper withShape(VoxelShapes shape, Direction facing){
		shapes.put(facing, shape);
		return this;
	}

	public static Direction axisAsFace(Axis axis) {
		return Direction.get(AxisDirection.POSITIVE, axis);
	}

	protected static float horizontalAngleFromDirection(Direction direction){
		return (float)((Math.max(direction.getHorizontal(), 0) & 3) * 90);
	}

	protected static VoxelShaper forDirectionsWithRotation(VoxelShapes shape, Direction facing, Iterable<Direction> directions, Function<Direction, EntityHitResult> rotationValues){
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Direction dir : directions) {
			voxelShaper.shapes.put(dir, rotate(shape, facing, dir, rotationValues));
		}
		return voxelShaper;
	}

	protected static VoxelShapes rotate(VoxelShapes shape, Direction from, Direction to, Function<Direction, EntityHitResult> usingValues){
		if (from == to)
			return shape;

		return rotatedCopy(shape, usingValues.apply(from).e().e(usingValues.apply(to)));
	}

	protected static VoxelShapes rotatedCopy(VoxelShapes shape, EntityHitResult rotation){
		if (rotation.equals(EntityHitResult.a))
			return shape;

		MutableObject<VoxelShapes> result = new MutableObject<>(ddb.a());
		EntityHitResult center = new EntityHitResult(8, 8, 8);

		shape.b((x1, y1, z1, x2, y2, z2) -> {
			EntityHitResult v1 = new EntityHitResult(x1, y1, z1).a(16).d(center);
			EntityHitResult v2 = new EntityHitResult(x2, y2, z2).a(16).d(center);

			v1 = VecHelper.rotate(v1, (float) rotation.entity, Axis.X);
			v1 = VecHelper.rotate(v1, (float) rotation.c, Axis.Y);
			v1 = VecHelper.rotate(v1, (float) rotation.d, Axis.Z).e(center);

			v2 = VecHelper.rotate(v2, (float) rotation.entity, Axis.X);
			v2 = VecHelper.rotate(v2, (float) rotation.c, Axis.Y);
			v2 = VecHelper.rotate(v2, (float) rotation.d, Axis.Z).e(center);

			VoxelShapes rotated = BeetrootsBlock.a(v1.entity, v1.c, v1.d, v2.entity, v2.c, v2.d);
			result.setValue(ddb.a(result.getValue(), rotated));
		});

		return result.getValue();
	}

	protected static class DefaultRotationValues implements Function<Direction, EntityHitResult> {
		//assume facing up as the default rotation
		@Override
		public EntityHitResult apply(Direction direction) {
			return new EntityHitResult(
					direction == Direction.UP ? 0 : (Direction.Type.VERTICAL.test(direction) ? 180 : 90),
					-horizontalAngleFromDirection(direction),
					0
			);
		}
	}

	protected static class HorizontalRotationValues implements Function<Direction, EntityHitResult> {
		@Override
		public EntityHitResult apply(Direction direction) {
			return new EntityHitResult(
					0,
					-horizontalAngleFromDirection(direction),
					0
			);
		}
	}

}
