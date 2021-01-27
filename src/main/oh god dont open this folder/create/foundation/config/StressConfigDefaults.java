package com.simibubi.create.foundation.config;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.util.Identifier;
import com.simibubi.create.Create;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

public class StressConfigDefaults {

	/**
	 * Increment this number if all stress entries should be forced to update in the next release.
	 * Worlds from the previous version will overwrite potentially changed values
	 * with the new defaults.
	 */
	public static final int forcedUpdateVersion = 1;

	static Map<Identifier, Double> registeredDefaultImpacts = new HashMap<>();
	static Map<Identifier, Double> registeredDefaultCapacities = new HashMap<>();

	public static <B extends BeetrootsBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> setNoImpact() {
		return setImpact(0);
	}
	
	public static <B extends BeetrootsBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> setImpact(double impact) {
		return b -> {
			registeredDefaultImpacts.put(Create.asResource(b.getName()), impact);
			return b;
		};
	}
	
	public static <B extends BeetrootsBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> setCapacity(double capacity) {
		return b -> {
			registeredDefaultCapacities.put(Create.asResource(b.getName()), capacity);
			return b;
		};
	}
	
}
