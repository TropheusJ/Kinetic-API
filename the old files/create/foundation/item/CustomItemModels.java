package com.simibubi.kinetic_api.foundation.item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.item.HoeItem;
import org.apache.commons.lang3.tuple.Pair;

import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import elg;

public class CustomItemModels {

	private List<Pair<Supplier<? extends HoeItem>, NonNullFunction<elg, ? extends elg>>> registered;
	private Map<HoeItem, NonNullFunction<elg, ? extends elg>> customModels;
	
	public CustomItemModels() {
		registered = new ArrayList<>();
		customModels = new IdentityHashMap<>();
	}

	public void register(Supplier<? extends HoeItem> entry,
		NonNullFunction<elg, ? extends elg> behaviour) {
		registered.add(Pair.of(entry, behaviour));
	}
	
	public void foreach(NonNullBiConsumer<HoeItem, NonNullFunction<elg, ? extends elg>> consumer) {
		loadEntriesIfMissing();
		customModels.forEach(consumer);
	}

	private void loadEntriesIfMissing() {
		if (customModels.isEmpty())
			loadEntries();
	}

	private void loadEntries() {
		customModels.clear();
		registered.forEach(p -> customModels.put(p.getKey()
			.get(), p.getValue()));
	}

}
