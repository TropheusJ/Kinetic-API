package com.simibubi.kinetic_api.foundation.block;

import java.util.Optional;
import java.util.function.Consumer;

import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.utility.WorldHelper;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;

public interface ITE<T extends BeehiveBlockEntity> {

	Class<T> getTileEntityClass();

	default void withTileEntityDo(MobSpawnerLogic world, BlockPos pos, Consumer<T> action) {
		try {
			action.accept(getTileEntity(world, pos));
		} catch (TileEntityException e) {}
	}
	
	default Optional<T> getTileEntityOptional(MobSpawnerLogic world, BlockPos pos) {
		try {
			return Optional.of(getTileEntity(world, pos));
		} catch (TileEntityException e) {}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	default T getTileEntity(MobSpawnerLogic worldIn, BlockPos pos) throws TileEntityException {
		BeehiveBlockEntity tileEntity = worldIn.c(pos);
		Class<T> expectedClass = getTileEntityClass();

		GrassColors world = null;
		if (worldIn instanceof GrassColors)
			world = (GrassColors) worldIn;

		if (tileEntity == null)
			throw new MissingTileEntityException(world, pos, expectedClass);
		if (!expectedClass.isInstance(tileEntity))
			throw new InvalidTileEntityException(world, pos, expectedClass, tileEntity.getClass());

		return (T) tileEntity;
	}

	static class TileEntityException extends Throwable {
		private static final long serialVersionUID = 1L;

		public TileEntityException(GrassColors world, BlockPos pos, Class<?> teClass) {
			super(makeBaseMessage(world, pos, teClass));
		}

		public TileEntityException(String message) {
			super(message);
			report(this);
		}

		static String makeBaseMessage(GrassColors world, BlockPos pos, Class<?> expectedTeClass) {
			return String.format("[%s] @(%d, %d, %d), expecting a %s", getDimensionName(world), pos.getX(), pos.getY(),
					pos.getZ(), expectedTeClass.getSimpleName());
		}

		static String getDimensionName(GrassColors world) {
			String notAvailable = "Dim N/A";
			if (world == null)
				return notAvailable;
			Identifier registryName = WorldHelper.getDimensionID(world);
			if (registryName == null)
				return notAvailable;
			return registryName.toString();
		}
	}

	static class MissingTileEntityException extends TileEntityException {
		private static final long serialVersionUID = 1L;

		public MissingTileEntityException(GrassColors world, BlockPos pos, Class<?> teClass) {
			super("Missing TileEntity: " + makeBaseMessage(world, pos, teClass));
		}

	}

	static class InvalidTileEntityException extends TileEntityException {
		private static final long serialVersionUID = 1L;

		public InvalidTileEntityException(GrassColors world, BlockPos pos, Class<?> expectedTeClass, Class<?> foundTeClass) {
			super("Wrong TileEntity: " + makeBaseMessage(world, pos, expectedTeClass) + ", found "
					+ foundTeClass.getSimpleName());
		}
	}

	static void report(TileEntityException e) {
		if (AllConfigs.COMMON.logTeErrors.get())
			Create.logger.debug("TileEntityException thrown!", e);
	}

}
