package com.simibubi.create.foundation;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.profiler.DummyProfiler;

public class ResourceReloadHandler extends SynchronousResourceReloadListener<Object> {

	@Override
	protected Object b(ReloadableResourceManager resourceManagerIn, DummyProfiler profilerIn) {
		return new Object();
	}

	@Override
	protected void a(Object $, ReloadableResourceManager resourceManagerIn, DummyProfiler profilerIn) {
		SpriteShifter.reloadUVs();
		CreateClient.bufferCache.invalidate();
	}

}
