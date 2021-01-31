package com.simibubi.kinetic_api.foundation.utility.recipe;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import net.minecraft.recipe.Ingredient;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.world.GameMode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Utility for searching through a world's recipe collection. Non-dynamic
 * conditions can be split off into an initial search for caching intermediate
 * results.
 * 
 * @author simibubi
 *
 */
public class RecipeFinder {
	
	private static Cache<Object, List<Ingredient<?>>> cachedSearches = CacheBuilder.newBuilder().build();

	/**
	 * Find all IRecipes matching the condition predicate. If this search is made
	 * more than once, using the same object instance as the cacheKey will retrieve
	 * the cached result from the first time.
	 * 
	 * @param cacheKey   (can be null to prevent the caching)
	 * @param world
	 * @param conditions
	 * @return A started search to continue with more specific conditions.
	 */
	public static List<Ingredient<?>> get(@Nullable Object cacheKey, GameMode world, Predicate<Ingredient<?>> conditions) {
		if (cacheKey == null)
			return startSearch(world, conditions);

		try {
			return cachedSearches.get(cacheKey, () -> startSearch(world, conditions));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

	private static List<Ingredient<?>> startSearch(GameMode world, Predicate<? super Ingredient<?>> conditions) {
		List<Ingredient<?>> list = world.o().b().stream().filter(conditions)
				.collect(Collectors.toList());
		return list;
	}


	public static final SynchronousResourceReloadListener<Object> LISTENER = new SynchronousResourceReloadListener<Object>() {
		
		@Override
		protected Object b(ReloadableResourceManager p_212854_1_, DummyProfiler p_212854_2_) {
			return new Object();
		}
		
		@Override
		protected void a(Object p_212853_1_, ReloadableResourceManager p_212853_2_, DummyProfiler p_212853_3_) {
			cachedSearches.invalidateAll();
		}
		
	};

}
