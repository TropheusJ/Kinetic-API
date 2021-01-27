package com.simibubi.create.foundation.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class AllCommands {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("create")
				//general purpose
				.then(ToggleDebugCommand.register())
				.then(OverlayConfigCommand.register())
				.then(FixLightingCommand.register())

				//dev-util
				//Comment out for release
				.then(ClearBufferCacheCommand.register())
				.then(ChunkUtilCommand.register())
//		      .then(KillTPSCommand.register())
		);
	}
}
