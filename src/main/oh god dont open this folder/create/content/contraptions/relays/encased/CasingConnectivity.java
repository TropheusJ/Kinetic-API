package com.simibubi.create.content.contraptions.relays.encased;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;

public class CasingConnectivity {

	private Map<BeetrootsBlock, Entry> entries;

	public CasingConnectivity() {
		entries = new IdentityHashMap<>();
	}

	public Entry get(PistonHandler blockState) {
		return entries.get(blockState.b());
	}

	public void makeCasing(BeetrootsBlock block, CTSpriteShiftEntry casing) {
		new Entry(block, casing, (s, f) -> true).register();
	}

	public void make(BeetrootsBlock block, CTSpriteShiftEntry casing) {
		new Entry(block, casing, (s, f) -> true).register();
	}

	public void make(BeetrootsBlock block, CTSpriteShiftEntry casing, BiPredicate<PistonHandler, Direction> predicate) {
		new Entry(block, casing, predicate).register();
	}

	public class Entry {

		private BeetrootsBlock block;
		private CTSpriteShiftEntry casing;
		private BiPredicate<PistonHandler, Direction> predicate;

		private Entry(BeetrootsBlock block, CTSpriteShiftEntry casing, BiPredicate<PistonHandler, Direction> predicate) {
			this.block = block;
			this.casing = casing;
			this.predicate = predicate;
		}

		public CTSpriteShiftEntry getCasing() {
			return casing;
		}

		public boolean isSideValid(PistonHandler state, Direction face) {
			return predicate.test(state, face);
		}

		public void register() {
			entries.put(block, this);
		}

	}
}
