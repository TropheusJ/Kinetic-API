package com.simibubi.kinetic_api.foundation.utility.outliner;

import afj;
import com.simibubi.kinetic_api.foundation.renderState.SuperRenderTypeBuffer;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;

public class LineOutline extends Outline {

	protected EntityHitResult start = EntityHitResult.a;
	protected EntityHitResult end = EntityHitResult.a;

	public LineOutline set(EntityHitResult start, EntityHitResult end) {
		this.start = start;
		this.end = end;
		return this;
	}

	@Override
	public void render(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		renderCuboidLine(ms, buffer, start, end);
	}

	public static class EndChasingLineOutline extends LineOutline {

		float prevProgress = 0;
		float progress = 0;

		@Override
		public void tick() {
		}

		public EndChasingLineOutline setProgress(float progress) {
			prevProgress = this.progress;
			this.progress = progress;
			return this;
		}

		@Override
		public LineOutline set(EntityHitResult start, EntityHitResult end) {
			if (!end.equals(this.end))
				super.set(start, end);
			return this;
		}

		@Override
		public void render(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
			float pt = KeyBinding.B()
				.ai();
			float distanceToTarget = 1 - afj.g(pt, prevProgress, progress);
			EntityHitResult start = end.e(this.start.d(end)
				.a(distanceToTarget));
			renderCuboidLine(ms, buffer, start, end);
		}

	}

}
