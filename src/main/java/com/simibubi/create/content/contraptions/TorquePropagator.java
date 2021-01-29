package com.simibubi.create.content.contraptions;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.color.world.GrassColors;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.WorldHelper;
import net.minecraft.world.WorldAccess;

public class TorquePropagator {

	static Map<WorldAccess, Map<Long, KineticNetwork>> networks = new HashMap<>();

	public void onLoadWorld(WorldAccess world) {
		networks.put(world, new HashMap<>());
		Create.logger.debug("Prepared Kinetic Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(WorldAccess world) {
		networks.remove(world);
		Create.logger.debug("Removed Kinetic Network Space for " + WorldHelper.getDimensionID(world));
	}

	public KineticNetwork getOrCreateNetworkFor(KineticTileEntity te) {
		Long id = te.network;
		KineticNetwork network;
		Map<Long, KineticNetwork> map = networks.get(te.v());
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
