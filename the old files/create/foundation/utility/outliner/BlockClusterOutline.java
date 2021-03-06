package com.simibubi.create.foundation.utility.outliner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.renderState.RenderTypes;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;

public class BlockClusterOutline extends Outline {

	private Cluster cluster;

	public BlockClusterOutline(Iterable<BlockPos> selection) {
		cluster = new Cluster();
		selection.forEach(cluster::include);
	}

	@Override
	public void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		for (MergeEntry edge : cluster.visibleEdges) {
			Vec3d start = Vec3d.of(edge.pos);
			Direction direction = Direction.get(AxisDirection.POSITIVE, edge.axis);
			renderAACuboidLine(ms, buffer, start, Vec3d.of(edge.pos.offset(direction)));
		}

		for (MergeEntry face : cluster.visibleFaces.keySet()) {
			AxisDirection axisDirection = cluster.visibleFaces.get(face);
			Direction direction = Direction.get(axisDirection, face.axis);
			BlockPos pos = face.pos;
			if (axisDirection == AxisDirection.POSITIVE)
				pos = pos.offset(direction.getOpposite());
			renderBlockFace(ms, buffer, pos, direction);
		}
	}

	protected void renderBlockFace(MatrixStack ms, SuperRenderTypeBuffer buffer, BlockPos pos, Direction face) {
		Optional<AllSpecialTextures> faceTexture = params.faceTexture;
		if (!faceTexture.isPresent())
			return;

		RenderLayer translucentType = RenderTypes.getOutlineTranslucent(faceTexture.get()
			.getLocation(), true);
		VertexConsumer builder = buffer.getLateBuffer(translucentType);

		Vec3d center = VecHelper.getCenterOf(pos);
		Vec3d offset = Vec3d.of(face.getVector());
		Vec3d plane = VecHelper.axisAlingedPlaneOf(offset);
		Axis axis = face.getAxis();

		offset = offset.multiply(1 / 2f + 1 / 64d);
		plane = plane.multiply(1 / 2f)
			.add(offset);

		int deg = face.getDirection()
			.offset() * 90;
		Vec3d a1 = plane.add(center);
		plane = VecHelper.rotate(plane, deg, axis);
		Vec3d a2 = plane.add(center);
		plane = VecHelper.rotate(plane, deg, axis);
		Vec3d a3 = plane.add(center);
		plane = VecHelper.rotate(plane, deg, axis);
		Vec3d a4 = plane.add(center);

		putQuad(ms, builder, a1, a2, a3, a4, face);
	}

	private static class Cluster {

		Map<MergeEntry, AxisDirection> visibleFaces;
		Set<MergeEntry> visibleEdges;

		public Cluster() {
			visibleEdges = new HashSet<>();
			visibleFaces = new HashMap<>();
		}

		public void include(BlockPos pos) {

			// 6 FACES
			for (Axis axis : Axis.values()) {
				Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
				for (int offset : new int[] { 0, 1 }) {
					MergeEntry entry = new MergeEntry(axis, pos.offset(direction, offset));
					if (visibleFaces.remove(entry) == null)
						visibleFaces.put(entry, offset == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
				}
			}

			// 12 EDGES
			for (Axis axis : Axis.values()) {
				for (Axis axis2 : Axis.values()) {
					if (axis == axis2)
						continue;
					for (Axis axis3 : Axis.values()) {
						if (axis == axis3)
							continue;
						if (axis2 == axis3)
							continue;

						Direction direction = Direction.get(AxisDirection.POSITIVE, axis2);
						Direction direction2 = Direction.get(AxisDirection.POSITIVE, axis3);

						for (int offset : new int[] { 0, 1 }) {
							BlockPos entryPos = pos.offset(direction, offset);
							for (int offset2 : new int[] { 0, 1 }) {
								entryPos = entryPos.offset(direction2, offset2);
								MergeEntry entry = new MergeEntry(axis, entryPos);
								if (!visibleEdges.remove(entry))
									visibleEdges.add(entry);
							}
						}
					}

					break;
				}
			}

		}

	}

	private static class MergeEntry {

		Axis axis;
		BlockPos pos;

		public MergeEntry(Axis axis, BlockPos pos) {
			this.axis = axis;
			this.pos = pos;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof MergeEntry))
				return false;

			MergeEntry other = (MergeEntry) o;
			return this.axis == other.axis && this.pos.equals(other.pos);
		}

		@Override
		public int hashCode() {
			return this.pos.hashCode() * 31 + axis.ordinal();
		}
	}

}
