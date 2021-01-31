package com.simibubi.kinetic_api.content.schematics.client.tools;

import java.util.Arrays;
import java.util.List;
import com.simibubi.kinetic_api.AllKeys;
import com.simibubi.kinetic_api.AllSpecialTextures;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.content.schematics.client.SchematicHandler;
import com.simibubi.kinetic_api.content.schematics.client.SchematicTransformation;
import com.simibubi.kinetic_api.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.kinetic_api.foundation.utility.RaycastHelper;
import com.simibubi.kinetic_api.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import com.simibubi.kinetic_api.foundation.utility.outliner.AABBOutline;
import dcg;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box.a;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;

public abstract class SchematicToolBase implements ISchematicTool {

	protected SchematicHandler schematicHandler;

	protected BlockPos selectedPos;
	protected EntityHitResult chasingSelectedPos;
	protected EntityHitResult lastChasingSelectedPos;

	protected boolean selectIgnoreBlocks;
	protected int selectionRange;
	protected boolean schematicSelected;
	protected boolean renderSelectedFace;
	protected Direction selectedFace;

	protected final List<String> mirrors = Arrays.asList("none", "leftRight", "frontBack");
	protected final List<String> rotations = Arrays.asList("none", "cw90", "cw180", "cw270");

	@Override
	public void init() {
		schematicHandler = CreateClient.schematicHandler;
		selectedPos = null;
		selectedFace = null;
		schematicSelected = false;
		chasingSelectedPos = EntityHitResult.a;
		lastChasingSelectedPos = EntityHitResult.a;
	}

	@Override
	public void updateSelection() {
		updateTargetPos();

		if (selectedPos == null)
			return;
		lastChasingSelectedPos = chasingSelectedPos;
		EntityHitResult target = EntityHitResult.b(selectedPos);
		if (target.f(chasingSelectedPos) < 1 / 512f) {
			chasingSelectedPos = target;
			return;
		}

		chasingSelectedPos = chasingSelectedPos.e(target.d(chasingSelectedPos)
			.a(1 / 2f));
	}

	public void updateTargetPos() {
		FishingParticle player = KeyBinding.B().s;

		// Select Blueprint
		if (schematicHandler.isDeployed()) {
			SchematicTransformation transformation = schematicHandler.getTransformation();
			Timer localBounds = schematicHandler.getBounds();

			EntityHitResult traceOrigin = RaycastHelper.getTraceOrigin(player);
			EntityHitResult start = transformation.toLocalSpace(traceOrigin);
			EntityHitResult end = transformation.toLocalSpace(RaycastHelper.getTraceTarget(player, 70, traceOrigin));
			PredicateTraceResult result =
				RaycastHelper.rayTraceUntil(start, end, pos -> localBounds.d(VecHelper.getCenterOf(pos)));

			schematicSelected = !result.missed();
			selectedFace = schematicSelected ? result.getFacing() : null;
		}

		boolean snap = this.selectedPos == null;

		// Select location at distance
		if (selectIgnoreBlocks) {
			float pt = KeyBinding.B()
				.ai();
			selectedPos = new BlockPos(player.j(pt)
				.e(player.bg()
					.a(selectionRange)));
			if (snap)
				lastChasingSelectedPos = chasingSelectedPos = EntityHitResult.b(selectedPos);
			return;
		}

		// Select targeted Block
		selectedPos = null;
		dcg trace = RaycastHelper.rayTraceRange(player.l, player, 75);
		if (trace == null || trace.c() != a.b)
			return;

		BlockPos hit = new BlockPos(trace.e());
		boolean replaceable = player.l.d_(hit)
			.c()
			.e();
		if (trace.b()
			.getAxis()
			.isVertical() && !replaceable)
			hit = hit.offset(trace.b());
		selectedPos = hit;
		if (snap)
			lastChasingSelectedPos = chasingSelectedPos = EntityHitResult.b(selectedPos);
	}

	@Override
	public void renderTool(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {}

	@Override
	public void renderOverlay(BufferVertexConsumer ms, BackgroundRenderer buffer) {}

	@Override
	public void renderOnSchematic(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		if (!schematicHandler.isDeployed())
			return;

		ms.a();
		AABBOutline outline = schematicHandler.getOutline();
		if (renderSelectedFace) {
			outline.getParams()
				.highlightFace(selectedFace)
				.withFaceTextures(AllSpecialTextures.CHECKERED,
					AllKeys.ctrlDown() ? AllSpecialTextures.HIGHLIGHT_CHECKERED : AllSpecialTextures.CHECKERED);
		}
		outline.getParams()
			.colored(0x6886c5)
			.withFaceTexture(AllSpecialTextures.CHECKERED)
			.lineWidth(1 / 16f);
		outline.render(ms, buffer);
		outline.getParams()
			.clearTextures();
		ms.b();
	}

}
