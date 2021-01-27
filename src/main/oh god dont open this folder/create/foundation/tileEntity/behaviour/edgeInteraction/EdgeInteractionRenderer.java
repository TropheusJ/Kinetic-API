package com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction;

import java.util.List;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.utility.VecHelper;
import dcg;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.timer.Timer;

public class EdgeInteractionRenderer {

	public static void tick() {
		KeyBinding mc = KeyBinding.B();
		Box target = mc.v;
		if (target == null || !(target instanceof dcg))
			return;

		dcg result = (dcg) target;
		DragonHeadEntityModel world = mc.r;
		BlockPos pos = result.a();
		PlayerAbilities player = mc.s;
		ItemCooldownManager heldItem = player.dC();

		if (player.bt())
			return;
		EdgeInteractionBehaviour behaviour = TileEntityBehaviour.get(world, pos, EdgeInteractionBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (behaviour.requiredItem.orElse(heldItem.b()) != heldItem.b())
			return;

		Direction face = result.b();
		List<Direction> connectiveSides = EdgeInteractionHandler.getConnectiveSides(world, pos, face, behaviour);
		if (connectiveSides.isEmpty())
			return;

		Direction closestEdge = connectiveSides.get(0);
		double bestDistance = Double.MAX_VALUE;
		EntityHitResult center = VecHelper.getCenterOf(pos);
		for (Direction direction : connectiveSides) {
			double distance = EntityHitResult.b(direction.getVector()).d(target.e()
				.d(center))
				.f();
			if (distance > bestDistance)
				continue;
			bestDistance = distance;
			closestEdge = direction;
		}

		Timer bb = EdgeInteractionHandler.getBB(pos, closestEdge);
		boolean hit = bb.d(target.e());

		ValueBox box = new ValueBox(LiteralText.EMPTY, bb.d(-pos.getX(), -pos.getY(), -pos.getZ()), pos);
		EntityHitResult textOffset = EntityHitResult.a;

		boolean positive = closestEdge.getDirection() == AxisDirection.POSITIVE;
		if (positive) {
			if (face.getAxis()
				.isHorizontal()) {
				if (closestEdge.getAxis()
					.isVertical())
					textOffset = textOffset.b(0, -128, 0);
				else
					textOffset = textOffset.b(-128, 0, 0);
			} else
				textOffset = textOffset.b(-128, 0, 0);
		}

		box.offsetLabel(textOffset)
			.withColors(0x7A6A2C, 0xB79D64)
			.passive(!hit);

		CreateClient.outliner.showValueBox("edge", box)
			.lineWidth(1 / 64f)
			.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
			.highlightFace(face);

	}

}
