package com.simibubi.kinetic_api.content.contraptions.processing;

import java.util.Random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.utility.Pair;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.ForgeRegistries;

public class ProcessingOutput {

	public static final ProcessingOutput EMPTY = new ProcessingOutput(ItemCooldownManager.tick, 1);

	private static final Random r = new Random();
	private final ItemCooldownManager stack;
	private final float chance;

	private Pair<Identifier, Integer> compatDatagenOutput;

	public ProcessingOutput(ItemCooldownManager stack, float chance) {
		this.stack = stack;
		this.chance = chance;
	}

	public ProcessingOutput(Pair<Identifier, Integer> item, float chance) {
		this.stack = ItemCooldownManager.tick;
		this.compatDatagenOutput = item;
		this.chance = chance;
	}

	public ItemCooldownManager getStack() {
		return stack;
	}

	public float getChance() {
		return chance;
	}

	public ItemCooldownManager rollOutput() {
		int outputAmount = stack.E();
		for (int roll = 0; roll < stack.E(); roll++)
			if (r.nextFloat() > chance)
				outputAmount--;
		if (outputAmount == 0)
			return ItemCooldownManager.tick;
		ItemCooldownManager out = stack.i();
		out.e(outputAmount);
		return out;
	}

	public JsonElement serialize() {
		JsonObject json = new JsonObject();
		Identifier resourceLocation = compatDatagenOutput == null ? stack.b()
			.getRegistryName() : compatDatagenOutput.getFirst();
		json.addProperty("item", resourceLocation.toString());
		int count = compatDatagenOutput == null ? stack.E() : compatDatagenOutput.getSecond();
		if (count != 1)
			json.addProperty("count", count);
		if (stack.n())
			json.add("nbt", new JsonParser().parse(stack.o()
				.toString()));
		if (chance != 1)
			json.addProperty("chance", chance);
		return json;
	}

	public static ProcessingOutput deserialize(JsonElement je) {
		if (!je.isJsonObject())
			throw new JsonSyntaxException("ProcessingOutput must be a json object");

		JsonObject json = je.getAsJsonObject();
		String itemId = OrderedText.h(json, "item");
		int count = OrderedText.a(json, "count", 1);
		float chance = OrderedText.g(json, "chance") ? OrderedText.l(json, "chance") : 1;
		ItemCooldownManager itemstack = new ItemCooldownManager(ForgeRegistries.ITEMS.getValue(new Identifier(itemId)), count);

		if (OrderedText.g(json, "nbt")) {
			try {
				JsonElement element = json.get("nbt");
				itemstack.c(StringNbtReader.parse(
					element.isJsonObject() ? Create.GSON.toJson(element) : OrderedText.a(element, "nbt")));
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
		}

		return new ProcessingOutput(itemstack, chance);
	}

	public void write(PacketByteBuf buf) {
		buf.a(getStack());
		buf.writeFloat(getChance());
	}

	public static ProcessingOutput read(PacketByteBuf buf) {
		return new ProcessingOutput(buf.n(), buf.readFloat());
	}

}
