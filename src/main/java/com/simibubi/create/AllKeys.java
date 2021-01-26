package com.simibubi.create;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.screen.PresetsScreen;
import net.minecraft.client.options.GraphicsMode;
import net.minecraft.client.options.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public enum AllKeys {

	TOOL_MENU("toolmenu", GLFW.GLFW_KEY_LEFT_ALT), 
	ACTIVATE_TOOL("", GLFW.GLFW_KEY_LEFT_CONTROL),

	;

	private GraphicsMode keybind;
	private String description;
	private int key;
	private boolean modifiable;

	private AllKeys(String description, int defaultKey) {
		this.description = Create.ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
	}

	public static void register() {
		for (AllKeys key : values()) {
			key.keybind = new GraphicsMode(key.description, key.key, Create.NAME);
			if (!key.modifiable)
				continue;

			ClientRegistry.registerKeyBinding(key.keybind);
		}
	}

	public GraphicsMode getKeybind() {
		return keybind;
	}

	public boolean isPressed() {
		if (!modifiable)
			return isKeyDown(key);
		return keybind.d();
	}

	public String getBoundKey() {
		return keybind.j().getString().toUpperCase();
	}

	public int getBoundCode() {
		return keybind.getKey().b();
	}

	public static boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(KeyBinding.B().aB().i(), key) != 0;
	}

	public static boolean ctrlDown() {
		return PresetsScreen.x();
	}

	public static boolean shiftDown() {
		return PresetsScreen.y();
	}

	public static boolean altDown() {
		return PresetsScreen.z();
	}

}
