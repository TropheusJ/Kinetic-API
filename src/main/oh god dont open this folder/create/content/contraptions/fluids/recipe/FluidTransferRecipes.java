package com.simibubi.create.content.contraptions.fluids.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.HoeItem;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.profiler.DummyProfiler;

public class FluidTransferRecipes {

	public static List<ItemCooldownManager> POTION_ITEMS = new ArrayList<>();
	public static List<HoeItem> FILLED_BUCKETS = new ArrayList<>();

	
	
	public static final SynchronousResourceReloadListener<Object> LISTENER = new SynchronousResourceReloadListener<Object>() {

		@Override
		protected Object b(ReloadableResourceManager p_212854_1_, DummyProfiler p_212854_2_) {
			return new Object();
		}

		@Override
		protected void a(Object p_212853_1_, ReloadableResourceManager p_212853_2_, DummyProfiler p_212853_3_) {
			POTION_ITEMS.clear();
			FILLED_BUCKETS.clear();
		}

	};
}
