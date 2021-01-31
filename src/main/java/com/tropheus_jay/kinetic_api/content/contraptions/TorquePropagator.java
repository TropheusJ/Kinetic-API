package com.tropheus_jay.kinetic_api.content.contraptions;

import com.tropheus_jay.kinetic_api.KineticAPI;
import com.tropheus_jay.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.tropheus_jay.kinetic_api.foundation.utility.WorldHelper;
import net.minecraft.world.WorldAccess;

import java.util.HashMap;
import java.util.Map;

public class TorquePropagator {

	static Map<WorldAccess, Map<Long, KineticNetwork>> networks = new HashMap<>();

	public void onLoadWorld(WorldAccess world) {
		networks.put(world, new HashMap<>());
		KineticAPI.logger.debug("Prepared Kinetic Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(WorldAccess world) {
		networks.remove(world);
		KineticAPI.logger.debug("Removed Kinetic Network Space for " + WorldHelper.getDimensionID(world));
	}

	public KineticNetwork getOrCreateNetworkFor(KineticTileEntity te) {
		Long id = te.network;
		KineticNetwork network;
		Map<Long, KineticNetwork> map = networks.get(te.getWorld());
		if (id == null)
			return null;

		if (!map.containsKey(id)) {
			network = new KineticNetwork();
			network.id = te.network;
			map.put(id, network);
		}
		network = map.get(id);
		return network;
	}

}
