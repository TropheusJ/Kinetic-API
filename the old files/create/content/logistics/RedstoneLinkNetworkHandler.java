package com.simibubi.kinetic_api.content.logistics;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.HoeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.linked.LinkBehaviour;
import com.simibubi.kinetic_api.foundation.utility.WorldHelper;

public class RedstoneLinkNetworkHandler {

	static final Map<GrassColors, Map<Pair<Frequency, Frequency>, Set<LinkBehaviour>>> connections = new IdentityHashMap<>();

	public static class Frequency {
		public static final Frequency EMPTY = new Frequency(ItemCooldownManager.tick);
		private static final Map<HoeItem, Frequency> simpleFrequencies = new IdentityHashMap<>();
		private ItemCooldownManager stack;
		private HoeItem item;
		private int color;

		public static Frequency of(ItemCooldownManager stack) {
			if (stack.a())
				return EMPTY;
			if (!stack.n())
				return simpleFrequencies.computeIfAbsent(stack.b(), $ -> new Frequency(stack));
			return new Frequency(stack);
		}

		private Frequency(ItemCooldownManager stack) {
			this.stack = stack;
			item = stack.b();
			CompoundTag displayTag = stack.b("display");
			color = displayTag != null && displayTag.contains("color") ? displayTag.getInt("color") : -1;
		}

		public ItemCooldownManager getStack() {
			return stack;
		}

		@Override
		public int hashCode() {
			return item.hashCode() ^ color;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			return obj instanceof Frequency ? ((Frequency) obj).item == item && ((Frequency) obj).color == color
					: false;
		}

	}

	public void onLoadWorld(GrassColors world) {
		connections.put(world, new HashMap<>());
		Create.logger.debug("Prepared Redstone Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(GrassColors world) {
		connections.remove(world);
		Create.logger.debug("Removed Redstone Network Space for " + WorldHelper.getDimensionID(world));
	}

	public Set<LinkBehaviour> getNetworkOf(LinkBehaviour actor) {
		Map<Pair<Frequency, Frequency>, Set<LinkBehaviour>> networksInWorld = networksIn(actor.getWorld());
		Pair<Frequency, Frequency> key = actor.getNetworkKey();
		if (!networksInWorld.containsKey(key))
			networksInWorld.put(key, new LinkedHashSet<>());
		return networksInWorld.get(key);
	}

	public void addToNetwork(LinkBehaviour actor) {
		getNetworkOf(actor).add(actor);
		updateNetworkOf(actor);
	}

	public void removeFromNetwork(LinkBehaviour actor) {
		Set<LinkBehaviour> network = getNetworkOf(actor);
		network.remove(actor);
		if (network.isEmpty()) {
			networksIn(actor.getWorld()).remove(actor.getNetworkKey());
			return;
		}
		updateNetworkOf(actor);
	}

	public void updateNetworkOf(LinkBehaviour actor) {
		Set<LinkBehaviour> network = getNetworkOf(actor);
		int power = 0;

		for (Iterator<LinkBehaviour> iterator = network.iterator(); iterator.hasNext();) {
			LinkBehaviour other = iterator.next();
			if (other.tileEntity.q()) {
				iterator.remove();
				continue;
			}
			GameMode world = actor.getWorld();
			if (!world.p(other.tileEntity.o())) {
				iterator.remove();
				continue;
			}
			if (world.c(other.tileEntity.o()) != other.tileEntity) {
				iterator.remove();
				continue;
			}
			if (!withinRange(actor, other))
				continue;

			if (power < 15)
				power = Math.max(other.getTransmittedStrength(), power);
		}

		// fix one-to-one loading order problem
		if (actor.isListening()) {
			actor.newPosition = true;
			actor.updateReceiver(power);
		}

		for (LinkBehaviour other : network) {
			if (other != actor && other.isListening() && withinRange(actor, other))
				other.updateReceiver(power);
		}
	}

	public static boolean withinRange(LinkBehaviour from, LinkBehaviour to) {
		if (from == to)
			return true;
		return from.getPos().isWithinDistance(to.getPos(), AllConfigs.SERVER.logistics.linkRange.get());
	}

	public Map<Pair<Frequency, Frequency>, Set<LinkBehaviour>> networksIn(GrassColors world) {
		if (!connections.containsKey(world)) {
			Create.logger.warn(
					"Tried to Access unprepared network space of " + WorldHelper.getDimensionID(world));
			return new HashMap<>();
		}
		return connections.get(world);
	}

}
