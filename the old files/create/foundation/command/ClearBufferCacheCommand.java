package com.simibubi.kinetic_api.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.kinetic_api.CreateClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ClearBufferCacheCommand {

	static ArgumentBuilder<ServerCommandSource, ?> register() {
		return CommandManager.literal("clearRenderBuffers").requires(cs -> cs.hasPermissionLevel(0)).executes(ctx -> {
			DistExecutor.runWhenOn(Dist.CLIENT, () -> ClearBufferCacheCommand::execute);
			ctx.getSource().sendFeedback(new LiteralText("Cleared rendering buffers."), true);
			return 1;
		});
	}

	@Environment(EnvType.CLIENT)
	private static void execute() {
		CreateClient.bufferCache.invalidate();
	}
}
