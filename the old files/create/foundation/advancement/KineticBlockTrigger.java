package com.simibubi.create.foundation.advancement;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.simibubi.create.Create;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class KineticBlockTrigger extends CriterionTriggerBase<KineticBlockTrigger.Instance> {

	private static final Identifier ID = new Identifier(Create.ID, "kinetic_block");

	public KineticBlockTrigger(String id) {
		super(id);
	}

	public com.simibubi.create.foundation.advancement.KineticBlockTrigger.Instance forBlock(Block block) {
		return new com.simibubi.create.foundation.advancement.KineticBlockTrigger.Instance(block);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public com.simibubi.create.foundation.advancement.KineticBlockTrigger.Instance conditionsFromJson(JsonObject json, AdvancementEntityPredicateDeserializer context) {
		Block block = null;
		if (json.has("block")) {
			Identifier resourcelocation = new Identifier(JsonHelper.getString(json, "block"));
			block = Registry.BLOCK.getOrEmpty(resourcelocation).orElseThrow(() -> {
				return new JsonSyntaxException("Unknown block type '" + resourcelocation + "'");
			});
		}

		return new com.simibubi.create.foundation.advancement.KineticBlockTrigger.Instance(block);
	}

	public void trigger(ServerPlayerEntity player, BlockState state) {
		trigger(player, Arrays.asList(() -> state.getBlock()));
	}

	public static class Instance extends CriterionTriggerBase.Instance {
		private final Block block;

		public Instance(Block block) {
			super(KineticBlockTrigger.ID, EntityPredicate.Extended.EMPTY); // FIXME: Is this right?
			this.block = block;
		}
		
		@Override
		protected boolean test(List<Supplier<Object>> suppliers) {
			if (suppliers.isEmpty())
				return false;
			return block == suppliers.get(0).get();
		}

		/* FIXME: Does this need serialization?
		@Override
		@SuppressWarnings("deprecation")
		public JsonElement serialize() {
			JsonObject jsonobject = new JsonObject();
			if (this.block != null)
				jsonobject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
			return jsonobject;
		}*/
	}

	
}
