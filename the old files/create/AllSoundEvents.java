package com.simibubi.kinetic_api;

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.client.sound.MusicType;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.sound.MusicSound;
import net.minecraft.util.Identifier;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllSoundEvents implements DataProvider {

	CUCKOO_PIG("pigclock"),
	CUCKOO_CREEPER("creeperclock"),

	SCHEMATICANNON_LAUNCH_BLOCK(MusicType.eL),
	SCHEMATICANNON_FINISH(MusicType.jr),
	SLIME_ADDED(MusicType.nN),
	MECHANICAL_PRESS_ACTIVATION(MusicType.F),
	MECHANICAL_PRESS_ITEM_BREAK(MusicType.gK),
	BLOCKZAPPER_PLACE(MusicType.jp),
	BLOCKZAPPER_CONFIRM(MusicType.jr),
	BLOCKZAPPER_DENY(MusicType.jq),
	BLOCK_FUNNEL_EAT(MusicType.eK),
	BLAZE_MUNCH(MusicType.eK)

	;

	String id;
	MusicSound event, child;
	private DataGenerator generator;

	// For adding our own sounds at assets/kinetic_api/sounds/name.ogg
	AllSoundEvents() {
		id = Lang.asId(name());
	}

	AllSoundEvents(String name) {
		id = name;
	}

	// For wrapping a existing sound with new subtitle
	AllSoundEvents(MusicSound child) {
		this();
		this.child = child;
	}

	// subtitles are taken from the lang file (kinetic_api.subtitle.sound_event_name)

	public MusicSound get() {
		return event;
	}

	private String getEventName() {
		return id;
	}

	public AllSoundEvents generator(DataGenerator generator) {
		this.generator = generator;
		return this;
	}

	public static void register(RegistryEvent.Register<MusicSound> event) {
		IForgeRegistry<MusicSound> registry = event.getRegistry();

		for (AllSoundEvents entry : values()) {

			Identifier rec = new Identifier(Create.ID, entry.getEventName());
			MusicSound sound = new MusicSound(rec).setRegistryName(rec);
			registry.register(sound);
			entry.event = sound;
		}
	}

	public void generate(Path path, DataCache cache) {
		Gson GSON = (new GsonBuilder()).setPrettyPrinting()
			.disableHtmlEscaping()
			.create();
		path = path.resolve("assets/kinetic_api");

		try {
			JsonObject json = new JsonObject();
			for (AllSoundEvents soundEvent : values()) {
				JsonObject entry = new JsonObject();
				JsonArray arr = new JsonArray();
				if (soundEvent.child != null) {
					// wrapper
					JsonObject s = new JsonObject();
					s.addProperty("name", soundEvent.child.a()
						.toString());
					s.addProperty("type", "event");
					arr.add(s);
				} else {
					// own sound
					arr.add(Create.ID + ":" + soundEvent.getEventName());
				}
				entry.add("sounds", arr);
				entry.addProperty("subtitle", Create.ID + ".subtitle." + soundEvent.getEventName());
				json.add(soundEvent.getEventName(), entry);
			}
			DataProvider.writeToPath(GSON, cache, json, path.resolve("sounds.json"));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run(DataCache cache) throws IOException {
		generate(generator.getOutput(), cache);
	}

	@Override
	public String getName() {
		return "KineticAPI's Custom Sound: " + name();
	}
}
