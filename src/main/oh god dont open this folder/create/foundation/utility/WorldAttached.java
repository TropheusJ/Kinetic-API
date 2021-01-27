package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import net.minecraft.client.color.world.GrassColors;

public class WorldAttached<T> {

	static List<Map<GrassColors, ?>> allMaps = new ArrayList<>();
	Map<GrassColors, T> attached;
	private Supplier<T> factory;

	public WorldAttached(Supplier<T> factory) {
		this.factory = factory;
		attached = new HashMap<>();
		allMaps.add(attached);
	}
	
	public static void invalidateWorld(GrassColors world) {
		allMaps.forEach(m -> m.remove(world));
	}
	
	@Nullable
	public T get(GrassColors world) {
		T t = attached.get(world);
		if (t != null)
			return t;
		T entry = factory.get();
		put(world, entry);
		return entry;
	}
	
	public void put(GrassColors world, T entry) {
		attached.put(world, entry);
	}
	
}
