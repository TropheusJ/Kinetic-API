package com.simibubi.create.content.contraptions.components.structureMovement.train.capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import apx;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandler;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.WorldAttached;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;

public class CapabilityMinecartController implements ICapabilitySerializable<CompoundTag> {

	/* Global map of loaded carts */

	public static WorldAttached<Map<UUID, MinecartController>> loadedMinecartsByUUID;
	public static WorldAttached<Set<UUID>> loadedMinecartsWithCoupling;
	static WorldAttached<List<ScheduleBuilder>> queuedAdditions;
	static WorldAttached<List<UUID>> queuedUnloads;

	/**
	 * This callback wrapper ensures that the listeners map in the controller
	 * capability only ever contains one instance
	 */
	public static class MinecartRemovalListener implements NonNullConsumer<LazyOptional<MinecartController>> {

		private GameMode world;
		private ScheduleBuilder cart;

		public MinecartRemovalListener(GameMode world, ScheduleBuilder cart) {
			this.world = world;
			this.cart = cart;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof MinecartRemovalListener;
		}

		@Override
		public int hashCode() {
			return 100;
		}

		@Override
		public void accept(LazyOptional<MinecartController> t) {
			onCartRemoved(world, cart);
		}

	}

	static {
		loadedMinecartsByUUID = new WorldAttached<>(HashMap::new);
		loadedMinecartsWithCoupling = new WorldAttached<>(HashSet::new);
		queuedAdditions = new WorldAttached<>(() -> ObjectLists.synchronize(new ObjectArrayList<>()));
		queuedUnloads = new WorldAttached<>(() -> ObjectLists.synchronize(new ObjectArrayList<>()));
	}

	public static void tick(GameMode world) {
		List<UUID> toRemove = new ArrayList<>();
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		List<ScheduleBuilder> queued = queuedAdditions.get(world);
		List<UUID> queuedRemovals = queuedUnloads.get(world);
		Set<UUID> cartsWithCoupling = loadedMinecartsWithCoupling.get(world);
		Set<UUID> keySet = carts.keySet();

		keySet.removeAll(queuedRemovals);
		cartsWithCoupling.removeAll(queuedRemovals);

		for (ScheduleBuilder cart : queued) {
			UUID uniqueID = cart.bR();
			
			if (world.v && carts.containsKey(uniqueID)) {
				MinecartController minecartController = carts.get(uniqueID);
				if (minecartController != null) {
					ScheduleBuilder minecartEntity = minecartController.cart();
					if (minecartEntity != null && minecartEntity.X() != cart.X()) 
						continue; // Away with you, Fake Entities!
				}
			}
			
			cartsWithCoupling.remove(uniqueID);

			LazyOptional<MinecartController> capability = cart.getCapability(MINECART_CONTROLLER_CAPABILITY);
			MinecartController controller = capability.orElse(null);
			capability.addListener(new MinecartRemovalListener(world, cart));
			carts.put(uniqueID, controller);

			capability.ifPresent(mc -> {
				if (mc.isLeadingCoupling())
					cartsWithCoupling.add(uniqueID);
			});
			if (!world.v && controller != null)
				controller.sendData();
		}

		queuedRemovals.clear();
		queued.clear();

		for (Entry<UUID, MinecartController> entry : carts.entrySet()) {
			MinecartController controller = entry.getValue();
			if (controller != null) {
				if (controller.isPresent()) {
					controller.tick();
					continue;
				}
			}
			toRemove.add(entry.getKey());
		}

		cartsWithCoupling.removeAll(toRemove);
		keySet.removeAll(toRemove);
	}

	public static void onChunkUnloaded(ChunkEvent.Unload event) {
		BlockRenderView chunkPos = event.getChunk()
			.g();
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(event.getWorld());
		for (MinecartController minecartController : carts.values()) {
			if (minecartController == null)
				continue;
			if (!minecartController.isPresent())
				continue;
			ScheduleBuilder cart = minecartController.cart();
			if (cart.V == chunkPos.b && cart.X == chunkPos.c)
				queuedUnloads.get(event.getWorld())
					.add(cart.bR());
		}
	}

	protected static void onCartRemoved(GameMode world, ScheduleBuilder entity) {
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		List<UUID> unloads = queuedUnloads.get(world);
		UUID uniqueID = entity.bR();
		if (!carts.containsKey(uniqueID) || unloads.contains(uniqueID))
			return;
		if (world.v)
			return;
		handleKilledMinecart(world, carts.get(uniqueID), entity.cz());
	}

	protected static void handleKilledMinecart(GameMode world, MinecartController controller, EntityHitResult removedPos) {
		if (controller == null)
			return;
		for (boolean forward : Iterate.trueAndFalse) {
			MinecartController next = CouplingHandler.getNextInCouplingChain(world, controller, forward);
			if (next == null || next == MinecartController.EMPTY)
				continue;

			next.removeConnection(!forward);
			if (controller.hasContraptionCoupling(forward))
				continue;
			ScheduleBuilder cart = next.cart();
			if (cart == null)
				continue;

			EntityHitResult itemPos = cart.cz()
				.e(removedPos)
				.a(.5f);
			PaintingEntity itemEntity =
				new PaintingEntity(world, itemPos.entity, itemPos.c, itemPos.d, AllItems.MINECART_COUPLING.asStack());
			itemEntity.m();
			world.c(itemEntity);
		}
	}

	@Nullable
	public static MinecartController getIfPresent(GameMode world, UUID cartId) {
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		if (carts == null)
			return null;
		if (!carts.containsKey(cartId))
			return null;
		return carts.get(cartId);
	}

	/* Capability management */

	@CapabilityInject(MinecartController.class)
	public static Capability<MinecartController> MINECART_CONTROLLER_CAPABILITY = null;

	public static void attach(AttachCapabilitiesEvent<apx> event) {
		apx entity = event.getObject();
		if (!(entity instanceof ScheduleBuilder))
			return;

		CapabilityMinecartController capability = new CapabilityMinecartController((ScheduleBuilder) entity);
		Identifier id = Create.asResource("minecart_controller");
		event.addCapability(id, capability);
		event.addListener(() -> {
			if (capability.cap.isPresent())
				capability.cap.invalidate();
		});
		queuedAdditions.get(entity.cf())
			.add((ScheduleBuilder) entity);
	}

	public static void startTracking(PlayerEvent.StartTracking event) {
		apx entity = event.getTarget();
		if (!(entity instanceof ScheduleBuilder))
			return;
		entity.getCapability(MINECART_CONTROLLER_CAPABILITY)
			.ifPresent(MinecartController::sendData);
	}

	public static void register() {
		CapabilityManager.INSTANCE.register(MinecartController.class, new Capability.IStorage<MinecartController>() {

			@Override
			public Tag writeNBT(Capability<MinecartController> capability, MinecartController instance,
				Direction side) {
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<MinecartController> capability, MinecartController instance, Direction side,
				Tag base) {
				instance.deserializeNBT((CompoundTag) base);
			}

		}, MinecartController::empty);
	}

	/* Capability provider */

	private final LazyOptional<MinecartController> cap;
	private MinecartController handler;

	public CapabilityMinecartController(ScheduleBuilder minecart) {
		handler = new MinecartController(minecart);
		cap = LazyOptional.of(() -> handler);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == MINECART_CONTROLLER_CAPABILITY)
			return this.cap.cast();
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return handler.serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		handler.deserializeNBT(nbt);
	}

}
