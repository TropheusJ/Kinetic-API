package com.tropheus_jay.kinetic_api.foundation.utility.outliner;

import com.tropheus_jay.kinetic_api.foundation.renderState.SuperRenderTypeBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class Outliner {

	final Map<Object, OutlineEntry> outlines;

	public Map<Object, OutlineEntry> getOutlines() {
		return Collections.unmodifiableMap(outlines);
	}

	// Facade
/* todo: ValueBox
	public Outline.OutlineParams showValueBox(Object slot, ValueBox box) {
		outlines.put(slot, new OutlineEntry(box));
		return box.getParams();
	}
//todo: lots of outline variants
	public Outline.OutlineParams showLine(Object slot, EntityHitResult start, EntityHitResult end) {
		if (!outlines.containsKey(slot)) {
			LineOutline outline = new LineOutline();
			outlines.put(slot, new OutlineEntry(outline));
		}
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		((LineOutline) entry.outline).set(start, end);
		return entry.outline.getParams();
	}

	public Outline.OutlineParams endChasingLine(Object slot, EntityHitResult start, EntityHitResult end, float chasingProgress) {
		if (!outlines.containsKey(slot)) {
			EndChasingLineOutline outline = new EndChasingLineOutline();
			outlines.put(slot, new OutlineEntry(outline));
		}
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		((EndChasingLineOutline) entry.outline).setProgress(chasingProgress)
			.set(start, end);
		return entry.outline.getParams();
	}

	public Outline.OutlineParams showAABB(Object slot, Timer bb) {
		createAABBOutlineIfMissing(slot, bb);
		ChasingAABBOutline outline = getAndRefreshAABB(slot);
		outline.prevBB = outline.targetBB = bb;
		return outline.getParams();
	}

	public Outline.OutlineParams chaseAABB(Object slot, Timer bb) {
		createAABBOutlineIfMissing(slot, bb);
		ChasingAABBOutline outline = getAndRefreshAABB(slot);
		outline.targetBB = bb;
		return outline.getParams();
	}

	public Outline.OutlineParams showCluster(Object slot, Iterable<BlockPos> selection) {
		BlockClusterOutline outline = new BlockClusterOutline(selection);
		OutlineEntry entry = new OutlineEntry(outline);
		outlines.put(slot, entry);
		return entry.getOutline()
			.getParams();
	}
*/
	public void keep(Object slot) {
		if (outlines.containsKey(slot))
			outlines.get(slot).ticksTillRemoval = 1;
	}

	public void remove(Object slot) {
		outlines.remove(slot);
	}

	public Optional<Outline.OutlineParams> edit(Object slot) {
		keep(slot);
		if (outlines.containsKey(slot))
			return Optional.of(outlines.get(slot)
				.getOutline()
				.getParams());
		return Optional.empty();
	}

	// Utility
/* todo: ChasingAABBOutline
	private void createAABBOutlineIfMissing(Object slot, Timer bb) {
		if (!outlines.containsKey(slot)) {
			ChasingAABBOutline outline = new ChasingAABBOutline(bb);
			outlines.put(slot, new OutlineEntry(outline));
		}
	}

	private ChasingAABBOutline getAndRefreshAABB(Object slot) {
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		return (ChasingAABBOutline) entry.getOutline();
	}
*/
	// Maintenance

	public Outliner() {
		outlines = Collections.synchronizedMap(new HashMap<>());
	}

	public void tickOutlines() {
		Set<Object> toClear = new HashSet<>();

		outlines.forEach((key, entry) -> {
			entry.ticksTillRemoval--;
			entry.getOutline()
				.tick();
			if (entry.isAlive())
				return;
			toClear.add(key);
		});

		toClear.forEach(outlines::remove);
	}

	public void renderOutlines(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		outlines.forEach((key, entry) -> {
			Outline outline = entry.getOutline();
			outline.params.alpha = 1;
			if (entry.ticksTillRemoval < 0) {

				int prevTicks = entry.ticksTillRemoval + 1;
				float fadeticks = OutlineEntry.fadeTicks;
				float lastAlpha = prevTicks >= 0 ? 1 : 1 + (prevTicks / fadeticks);
				float currentAlpha = 1 + (entry.ticksTillRemoval / fadeticks);
				float alpha = MathHelper.lerp(MinecraftClient.getInstance()
					.getTickDelta(), lastAlpha, currentAlpha);

				outline.params.alpha = alpha * alpha * alpha;
				if (outline.params.alpha < 1 / 8f)
					return;
			}
			outline.render(ms, buffer);
		});
	}

	public static class OutlineEntry {

		static final int fadeTicks = 8;
		private Outline outline;
		private int ticksTillRemoval;

		public OutlineEntry(Outline outline) {
			this.outline = outline;
			ticksTillRemoval = 1;
		}

		public boolean isAlive() {
			return ticksTillRemoval >= -fadeTicks;
		}

		public Outline getOutline() {
			return outline;
		}

	}

}
