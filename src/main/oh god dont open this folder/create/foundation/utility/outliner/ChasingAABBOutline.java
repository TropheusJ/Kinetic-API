package com.simibubi.create.foundation.utility.outliner;

import afj;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.world.timer.Timer;

public class ChasingAABBOutline extends AABBOutline {

	Timer targetBB;
	Timer prevBB;

	public ChasingAABBOutline(Timer bb) {
		super(bb);
		prevBB = bb.g(0);
		targetBB = bb.g(0);
	}

	public void target(Timer target) {
		targetBB = target;
	}

	@Override
	public void tick() {
		prevBB = bb;
		setBounds(interpolateBBs(bb, targetBB, .5f));
	}

	@Override
	public void render(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		renderBB(ms, buffer, interpolateBBs(prevBB, bb, KeyBinding.B()
			.ai()));
	}

	private static Timer interpolateBBs(Timer current, Timer target, float pt) {
		return new Timer(afj.d(pt, current.LOGGER, target.LOGGER),
			afj.d(pt, current.callback, target.callback), afj.d(pt, current.events, target.events),
			afj.d(pt, current.eventCounter, target.eventCounter), afj.d(pt, current.eventsByName, target.eventsByName),
			afj.d(pt, current.f, target.f));
	}

}
