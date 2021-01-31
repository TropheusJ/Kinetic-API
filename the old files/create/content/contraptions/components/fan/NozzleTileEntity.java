package com.simibubi.kinetic_api.content.contraptions.components.fan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import afj;
import apx;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.world.DummyClientTickScheduler.a;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.b;
import net.minecraft.world.timer.Timer;

public class NozzleTileEntity extends SmartTileEntity {

	private List<apx> pushingEntities = new ArrayList<>();
	private float range;
	private boolean pushing;
	private BlockPos fanPos;

	public NozzleTileEntity(BellBlockEntity<? extends NozzleTileEntity> type) {
		super(type);
		setLazyTickRate(5);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		if (!clientPacket)
			return;
		compound.putFloat("Range", range);
		compound.putBoolean("Pushing", pushing);
	}
	
	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (!clientPacket)
			return;
		range = compound.getFloat("Range");
		pushing = compound.getBoolean("Pushing");
	}

	@Override
	public void initialize() {
		fanPos = e.offset(p().c(NozzleBlock.SHAPE)
			.getOpposite());
		super.initialize();
	}

	@Override
	public void aj_() {
		super.aj_();

		float range = calcRange();
		if (this.range != range)
			setRange(range);

		EntityHitResult center = VecHelper.getCenterOf(e);
		if (d.v && range != 0) {
			if (d.t.nextInt(
				afj.a((AllConfigs.SERVER.kinetics.fanPushDistance.get() - (int) range), 1, 10)) == 0) {
				EntityHitResult start = VecHelper.offsetRandomly(center, d.t, pushing ? 1 : range / 2);
				EntityHitResult motion = center.d(start)
					.d()
					.a(afj.a(range * (pushing ? .025f : 1f), 0, .5f) * (pushing ? -1 : 1));
				d.addParticle(ParticleTypes.POOF, start.entity, start.c, start.d, motion.entity, motion.c, motion.d);
			}
		}

		for (Iterator<apx> iterator = pushingEntities.iterator(); iterator.hasNext();) {
			apx entity = iterator.next();
			EntityHitResult diff = entity.cz()
				.d(center);

			if (!(entity instanceof PlayerAbilities) && d.v)
				continue;

			double distance = diff.f();
			if (distance > range || entity.bt()
				|| (entity instanceof PlayerAbilities && ((PlayerAbilities) entity).b_())) {
				iterator.remove();
				continue;
			}

			if (!pushing && distance < 1.5f)
				continue;

			float factor = (entity instanceof PaintingEntity) ? 1 / 128f : 1 / 32f;
			EntityHitResult pushVec = diff.d()
				.a((range - distance) * (pushing ? 1 : -1));
			entity.f(entity.cB()
				.e(pushVec.a(factor)));
			entity.C = 0;
			entity.w = true;
		}

	}

	public void setRange(float range) {
		this.range = range;
		if (range == 0)
			pushingEntities.clear();
		sendData();
	}

	private float calcRange() {
		BeehiveBlockEntity te = d.c(fanPos);
		if (!(te instanceof IAirCurrentSource))
			return 0;

		IAirCurrentSource source = (IAirCurrentSource) te;
		if (source instanceof EncasedFanTileEntity && ((EncasedFanTileEntity) source).isGenerator)
			return 0;
		if (source.getAirCurrent() == null)
			return 0;
		if (source.getSpeed() == 0)
			return 0;
		pushing = source.getAirFlowDirection() == source.getAirflowOriginSide();
		return source.getMaxDistance();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();

		if (range == 0)
			return;

		EntityHitResult center = VecHelper.getCenterOf(e);
		Timer bb = new Timer(center, center).g(range / 2f);

		for (apx entity : d.a(apx.class, bb)) {
			EntityHitResult diff = entity.cz()
				.d(center);

			double distance = diff.f();
			if (distance > range || entity.bt()
				|| (entity instanceof PlayerAbilities && ((PlayerAbilities) entity).b_())) {
				continue;
			}

			boolean canSee = canSee(entity);
			if (!canSee) {
				pushingEntities.remove(entity);
				continue;
			}

			if (!pushingEntities.contains(entity))
				pushingEntities.add(entity);
		}

		for (Iterator<apx> iterator = pushingEntities.iterator(); iterator.hasNext();) {
			apx entity = iterator.next();
			if (entity.aW())
				continue;
			iterator.remove();
		}

		if (!pushing && pushingEntities.size() > 256 && !d.v) {
			d.a(null, center.entity, center.c, center.d, 2, a.a);
			for (Iterator<apx> iterator = pushingEntities.iterator(); iterator.hasNext();) {
				apx entity = iterator.next();
				entity.ac();
				iterator.remove();
			}
		}

	}

	private boolean canSee(apx entity) {
		BlockView context = new BlockView(entity.cz(), VecHelper.getCenterOf(e),
			net.minecraft.world.BlockView.a.a, b.a, entity);
		return e.equals(d.a(context)
			.a());
	}

}
