package com.simibubi.kinetic_api.content.contraptions.components.clock;

import static com.simibubi.kinetic_api.foundation.utility.AngleHelper.deg;
import static com.simibubi.kinetic_api.foundation.utility.AngleHelper.getShortestAngleDiff;
import static com.simibubi.kinetic_api.foundation.utility.AngleHelper.rad;

import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.components.clock.CuckooClockTileEntity.Animation;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.kinetic_api.foundation.gui.widgets.InterpolatedValue;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.world.DummyClientTickScheduler;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;

public class CuckooClockTileEntity extends KineticTileEntity {

	public static DamageRecord CUCKOO_SURPRISE = new DamageRecord("kinetic_api.cuckoo_clock_explosion").e();

	public InterpolatedChasingValue hourHand = new InterpolatedChasingValue().withSpeed(.2f);
	public InterpolatedChasingValue minuteHand = new InterpolatedChasingValue().withSpeed(.2f);
	public InterpolatedValue animationProgress = new InterpolatedValue();
	public Animation animationType;
	private boolean sendAnimationUpdate;

	enum Animation {
		PIG, CREEPER, SURPRISE, NONE;
	}

	public CuckooClockTileEntity(BellBlockEntity<? extends CuckooClockTileEntity> type) {
		super(type);
		animationType = Animation.NONE;
	}
	
	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (clientPacket && compound.contains("Animation")) {
			animationType = NBTHelper.readEnum(compound, "Animation", Animation.class);
			animationProgress.lastValue = 0;
			animationProgress.value = 0;
		}
	}
	
	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		if (clientPacket && sendAnimationUpdate)
			NBTHelper.writeEnum(compound, "Animation", animationType);
		sendAnimationUpdate = false;
		super.write(compound, clientPacket);
	}

	@Override
	public void aj_() {
		super.aj_();
		if (getSpeed() == 0)
			return;

		int dayTime = (int) (d.T() % 24000);
		int hours = (dayTime / 1000 + 6) % 24;
		int minutes = (dayTime % 1000) * 60 / 1000;

		if (!d.v) {
			if (animationType == Animation.NONE) {
				if (hours == 12 && minutes < 5)
					startAnimation(Animation.PIG);
				if (hours == 18 && minutes < 36 && minutes > 31)
					startAnimation(Animation.CREEPER);
			} else {
				float value = animationProgress.value;
				animationProgress.set(value + 1);
				if (value > 100)
					animationType = Animation.NONE;

				if (animationType == Animation.SURPRISE && animationProgress.value == 50) {
					EntityHitResult center = VecHelper.getCenterOf(e);
					d.b(e, false);
					d.a(null, CUCKOO_SURPRISE, null, center.entity, center.c, center.d, 3, false,
						DummyClientTickScheduler.a.b);
				}

			}
		}

		if (d.v) {
			moveHands(hours, minutes);

			if (animationType == Animation.NONE) {
				if (AnimationTickHolder.ticks % 32 == 0)
					playSound(MusicType.jw, 1 / 16f, 2f);
				else if (AnimationTickHolder.ticks % 16 == 0)
					playSound(MusicType.jw, 1 / 16f, 1.5f);
			} else {

				boolean isSurprise = animationType == Animation.SURPRISE;
				float value = animationProgress.value;
				animationProgress.set(value + 1);
				if (value > 100)
					animationType = null;

				// sounds

				if (value == 1)
					playSound(MusicType.js, 2, .5f);
				if (value == 21)
					playSound(MusicType.js, 2, 0.793701f);

				if (value > 30 && isSurprise) {
					EntityHitResult pos = VecHelper.offsetRandomly(VecHelper.getCenterOf(this.e), d.t, .5f);
					d.addParticle(ParticleTypes.LARGE_SMOKE, pos.entity, pos.c, pos.d, 0, 0, 0);
				}
				if (value == 40 && isSurprise)
					playSound(MusicType.pb, 1f, 1f);

				int step = isSurprise ? 3 : 15;
				for (int phase = 30; phase <= 60; phase += step) {
					if (value == phase - step / 3)
						playSound(MusicType.bG, 1 / 16f, 2f);
					if (value == phase) {
						if (animationType == Animation.PIG)
							playSound(MusicType.kN, 1 / 4f, 1f);
						else
							playSound(MusicType.co, 1 / 4f, 3f);
					}
					if (value == phase + step / 3)
						playSound(MusicType.bE, 1 / 16f, 2f);

				}

			}

			return;
		}
	}

	public void startAnimation(Animation animation) {
		animationType = animation;
		if (animation != null && CuckooClockBlock.containsSurprise(p()))
			animationType = Animation.SURPRISE;
		animationProgress.lastValue = 0;
		animationProgress.value = 0;
		sendAnimationUpdate = true;
		
		if (animation == Animation.CREEPER)
			AllTriggers.triggerForNearbyPlayers(AllTriggers.CUCKOO, d, e, 10);
		
		sendData();
	}

	public void moveHands(int hours, int minutes) {
		float hourTarget = (float) (360 / 12 * (hours % 12));
		float minuteTarget = (float) (360 / 60 * minutes);

		hourHand.target(hourHand.value + rad(getShortestAngleDiff(deg(hourHand.value), hourTarget)));
		minuteHand.target(minuteHand.value + rad(getShortestAngleDiff(deg(minuteHand.value), minuteTarget)));

		hourHand.tick();
		minuteHand.tick();
	}

	private void playSound(MusicSound sound, float volume, float pitch) {
		EntityHitResult vec = VecHelper.getCenterOf(e);
		d.a(vec.entity, vec.c, vec.d, sound, SoundEvent.e, volume, pitch, false);
	}

}
