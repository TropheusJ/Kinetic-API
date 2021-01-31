package com.simibubi.kinetic_api.foundation.block.render;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.block.BeetrootsBlock;
import org.apache.commons.lang3.tuple.Pair;

import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import elg;

public class CustomBlockModels {

	private List<Pair<Supplier<? extends BeetrootsBlock>, NonNullFunction<elg, ? extends elg>>> registered;
	private Map<BeetrootsBlock, NonNullFunction<elg, ? extends elg>> customModels;

	public CustomBlockModels() {
		registered = new ArrayList<>();
		customModels = new IdentityHashMap<>();
	}

	public void register(Supplier<? extends BeetrootsBlock> entry,
		NonNullFunction<elg, ? extends elg> behaviour) {
		registered.add(Pair.of(entry, behaviour));
	}

	public void foreach(NonNullBiConsumer<BeetrootsBlock, NonNullFunction<elg, ? extends elg>> consumer) {
		loadEntriesIfMissing();
		customModels.forEach(consumer);
	}

	private void loadEntriesIfMissing() {
		if (customModels.isEmpty())
			loadEntries();
	}

	private void loadEntries() {
		customModels.clear();
		registered.forEach(p -> {
			BeetrootsBlock key = p.getKey()
				.get();
			
			NonNullFunction<elg, ? extends elg> existingModel = customModels.get(key);
			if (existingModel != null) {
				customModels.put(key, p.getValue()
					.andThen(existingModel));
				return;
			}
			
			customModels.put(key, p.getValue());
		});
	}

}
