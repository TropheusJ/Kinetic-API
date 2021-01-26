package com.simibubi.create.content.contraptions.components.actors.dispenser;

import java.util.HashMap;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.HoeItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class DispenserMovementBehaviour extends DropperMovementBehaviour {
	private static final HashMap<HoeItem, IMovedDispenseItemBehaviour> MOVED_DISPENSE_ITEM_BEHAVIOURS = new HashMap<>();
	private static final HashMap<HoeItem, IMovedDispenseItemBehaviour> MOVED_PROJECTILE_DISPENSE_BEHAVIOURS = new HashMap<>();
	private static final DispenserLookup BEHAVIOUR_LOOKUP = new DispenserLookup();
	private static boolean spawneggsRegistered = false;

	public static void gatherMovedDispenseItemBehaviours() {
		IMovedDispenseItemBehaviour.init();
	}

	public static void registerMovedDispenseItemBehaviour(HoeItem item, IMovedDispenseItemBehaviour movedDispenseItemBehaviour) {
		MOVED_DISPENSE_ITEM_BEHAVIOURS.put(item, movedDispenseItemBehaviour);
	}

	@Override
	protected void activate(MovementContext context, BlockPos pos) {
		if (!spawneggsRegistered) {
			spawneggsRegistered = true;
			IMovedDispenseItemBehaviour.initSpawneggs();
		}
		
		DispenseItemLocation location = getDispenseLocation(context);
		if (location.isEmpty()) {
			context.world.syncWorldEvent(1001, pos, 0);
		} else {
			ItemCooldownManager itemstack = getItemStackAt(location, context);
			// Special dispense item behaviour for moving contraptions
			if (MOVED_DISPENSE_ITEM_BEHAVIOURS.containsKey(itemstack.b())) {
				setItemStackAt(location, MOVED_DISPENSE_ITEM_BEHAVIOURS.get(itemstack.b()).dispense(itemstack, context, pos), context);
				return;
			}

			ItemCooldownManager backup = itemstack.i();
			// If none is there, try vanilla registry
			try {
				if (MOVED_PROJECTILE_DISPENSE_BEHAVIOURS.containsKey(itemstack.b())) {
					setItemStackAt(location, MOVED_PROJECTILE_DISPENSE_BEHAVIOURS.get(itemstack.b()).dispense(itemstack, context, pos), context);
					return;
				}

				DispenserBehavior idispenseitembehavior = BEHAVIOUR_LOOKUP.a(itemstack);
				if (idispenseitembehavior instanceof ProjectileDispenserBehavior) { // Projectile behaviours can be converted most of the time
					IMovedDispenseItemBehaviour iMovedDispenseItemBehaviour = MovedProjectileDispenserBehaviour.of((ProjectileDispenserBehavior) idispenseitembehavior);
					setItemStackAt(location, iMovedDispenseItemBehaviour.dispense(itemstack, context, pos), context);
					MOVED_PROJECTILE_DISPENSE_BEHAVIOURS.put(itemstack.b(), iMovedDispenseItemBehaviour); // buffer conversion if successful
					return;
				}

				EntityHitResult facingVec = EntityHitResult.b(context.state.c(DetectorRailBlock.a).getVector());
				facingVec = context.rotation.apply(facingVec);
				facingVec.d();
				Direction clostestFacing = Direction.getFacing(facingVec.entity, facingVec.c, facingVec.d);
				ContraptionBlockSource blockSource = new ContraptionBlockSource(context, pos, clostestFacing);

				if (idispenseitembehavior.getClass() != ItemDispenserBehavior.class) { // There is a dispense item behaviour registered for the vanilla dispenser
					setItemStackAt(location, idispenseitembehavior.dispense(blockSource, itemstack), context);
					return;
				}
			} catch (NullPointerException ignored) {
				itemstack = backup; // Something went wrong with the TE being null in ContraptionBlockSource, reset the stack
			}

			setItemStackAt(location, defaultBehaviour.dispense(itemstack, context, pos), context);  // the default: launch the item
		}
	}

	@ParametersAreNonnullByDefault
	@MethodsReturnNonnullByDefault
	private static class DispenserLookup extends DetectorRailBlock {
		protected DispenserLookup() {
			super(BeetrootsBlock.Properties.a(BellBlock.as));
		}

		public DispenserBehavior a(ItemCooldownManager itemStack) {
			return super.a(itemStack);
		}
	}
}
