package com.simibubi.kinetic_api;

import java.util.HashMap;

import javax.annotation.Nullable;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.Identifier;
import com.simibubi.kinetic_api.content.contraptions.components.actors.BellMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.actors.CampfireMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.actors.dispenser.DispenserMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.actors.dispenser.DropperMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementBehaviour;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

public class AllMovementBehaviours {
	private static final HashMap<Identifier, MovementBehaviour> movementBehaviours = new HashMap<>();

	public static void addMovementBehaviour(Identifier resourceLocation, MovementBehaviour movementBehaviour) {
		if (movementBehaviours.containsKey(resourceLocation))
			Create.logger.warn("Movement behaviour for " + resourceLocation.toString() + " was overridden");
		movementBehaviours.put(resourceLocation, movementBehaviour);
	}

	public static void addMovementBehaviour(BeetrootsBlock block, MovementBehaviour movementBehaviour) {
		addMovementBehaviour(block.getRegistryName(), movementBehaviour);
	}

	@Nullable
	public static MovementBehaviour of(Identifier resourceLocation) {
		return movementBehaviours.getOrDefault(resourceLocation, null);
	}

	@Nullable
	public static MovementBehaviour of(BeetrootsBlock block) {
		return of(block.getRegistryName());
	}
	
	@Nullable
	public static MovementBehaviour of(PistonHandler state) {
		return of(state.b());
	}

	public static boolean contains(BeetrootsBlock block) {
		return movementBehaviours.containsKey(block.getRegistryName());
	}

	public static <B extends BeetrootsBlock> NonNullConsumer<? super B> addMovementBehaviour(
		MovementBehaviour movementBehaviour) {
		return b -> addMovementBehaviour(b.getRegistryName(), movementBehaviour);
	}

	static void register() {
		addMovementBehaviour(BellBlock.mb, new BellMovementBehaviour());
		addMovementBehaviour(BellBlock.me, new CampfireMovementBehaviour());

		DispenserMovementBehaviour.gatherMovedDispenseItemBehaviours();
		addMovementBehaviour(BellBlock.as, new DispenserMovementBehaviour());
		addMovementBehaviour(BellBlock.fE, new DropperMovementBehaviour());
	}
}
