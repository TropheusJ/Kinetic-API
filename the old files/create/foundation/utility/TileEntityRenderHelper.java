package com.simibubi.kinetic_api.foundation.utility;

import java.util.Iterator;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import ebv;
import ebw;
import ejo;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.GameMode;

public class TileEntityRenderHelper {

	public static void renderTileEntities(GameMode world, Iterable<BeehiveBlockEntity> customRenderTEs, BufferVertexConsumer ms,
		BufferVertexConsumer localTransform, BackgroundRenderer buffer) {
		float pt = KeyBinding.B()
			.ai();
		Matrix4f matrix = localTransform.c()
			.a();

		for (Iterator<BeehiveBlockEntity> iterator = customRenderTEs.iterator(); iterator.hasNext();) {
			BeehiveBlockEntity tileEntity = iterator.next();
			ebw<BeehiveBlockEntity> renderer = ebv.a.a(tileEntity);
			if (renderer == null) {
				iterator.remove();
				continue;
			}

			try {
				BlockPos pos = tileEntity.o();
				ms.a();
				MatrixStacker.of(ms)
					.translate(pos);

				Vector4f vec = new Vector4f(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 1);
				vec.transform(matrix);
				BlockPos lightPos = new BlockPos(vec.getX(), vec.getY(), vec.getZ());
				renderer.a(tileEntity, pt, ms, buffer, JsonGlProgram.a(world, lightPos),
					ejo.a);
				ms.b();

			} catch (Exception e) {
				iterator.remove();
				
				String message = "TileEntity " + tileEntity.u()
					.getRegistryName()
					.toString() + " didn't want to render while moved.\n";
				if (AllConfigs.CLIENT.explainRenderErrors.get()) {
					Create.logger.error(message, e);
					continue;
				}
				
				Create.logger.error(message);
				continue;
			}
		}
	}

}
