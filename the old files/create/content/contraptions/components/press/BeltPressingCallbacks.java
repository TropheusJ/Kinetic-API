package com.simibubi.kinetic_api.content.contraptions.components.press;

import static com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.ItemCooldownManager;
import com.simibubi.kinetic_api.content.contraptions.components.press.MechanicalPressTileEntity.Mode;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;

public class BeltPressingCallbacks {

	static ProcessingResult onItemReceived(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler, MechanicalPressTileEntity press) {
		if (press.getSpeed() == 0 || press.running)
			return PASS;
		if (!press.getRecipe(transported.stack)
			.isPresent())
			return PASS;

		press.start(Mode.BELT);
		return HOLD;
	}

	static ProcessingResult whenItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler,
		MechanicalPressTileEntity pressTe) {

		if (pressTe.getSpeed() == 0)
			return PASS;
		if (!pressTe.running)
			return PASS;
		if (pressTe.runningTicks != MechanicalPressTileEntity.CYCLE / 2)
			return HOLD;

		Optional<PressingRecipe> recipe = pressTe.getRecipe(transported.stack);
		pressTe.pressedItems.clear();
		pressTe.pressedItems.add(transported.stack);

		if (!recipe.isPresent())
			return PASS;

		ItemCooldownManager out = recipe.get()
			.c()
			.i();
		List<ItemCooldownManager> multipliedOutput = ItemHelper.multipliedOutput(transported.stack, out);
		if (multipliedOutput.isEmpty())
			transported.stack = ItemCooldownManager.tick;
		transported.stack = multipliedOutput.get(0);
		pressTe.sendData();
		return HOLD;
	}

}
