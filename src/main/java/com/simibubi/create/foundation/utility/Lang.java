package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import com.simibubi.create.Create;

public class Lang {

	public static TranslatableText translate(String key, Object... args) {
		return createTranslationTextComponent(key, args);
	}

	public static TranslatableText createTranslationTextComponent(String key, Object... args) {
		return new TranslatableText(Create.ID + "." + key, args);
	}

	public static void sendStatus(PlayerAbilities player, String key, Object... args) {
		player.a(createTranslationTextComponent(key, args), true);
	}

	public static List<Text> translatedOptions(String prefix, String... keys) {
		List<Text> result = new ArrayList<>(keys.length);
		for (String key : keys) {
			result.add(translate(prefix + "." + key));
		}
		return result;
	}

	public static String asId(String name) {
		return name.toLowerCase(Locale.ENGLISH);
	}

}
