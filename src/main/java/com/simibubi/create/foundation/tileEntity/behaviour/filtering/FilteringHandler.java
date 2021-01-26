package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import afj;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RaycastHelper;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.ItemHandlerHelper;

@EventBusSubscriber
public class FilteringHandler {

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		GameMode world = event.getWorld();
		BlockPos pos = event.getPos();
		PlayerAbilities player = event.getPlayer();
		ItemScatterer hand = event.getHand();

		if (player.bt())
			return;

		FilteringBehaviour behaviour = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;

		dcg ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return;
		if (behaviour instanceof SidedFilteringBehaviour) {
			behaviour = ((SidedFilteringBehaviour) behaviour).get(ray.b());
			if (behaviour == null)
				return;
		}
		if (!behaviour.isActive())
			return;
		if (behaviour.slotPositioning instanceof ValueBoxTransform.Sided)
			((Sided) behaviour.slotPositioning).fromSide(ray.b());
		if (!behaviour.testHit(ray.e()))
			return;

		ItemCooldownManager toApply = player.b(hand)
			.i();

		if (event.getSide() != LogicalSide.CLIENT) {
			if (!player.b_()) {
				if (behaviour.getFilter()
					.b() instanceof FilterItem)
					player.bm.a(world, behaviour.getFilter());
				if (toApply.b() instanceof FilterItem)
					player.b(hand)
						.g(1);
			}
			if (toApply.b() instanceof FilterItem)
				toApply.e(1);
			behaviour.setFilter(toApply);

		} else {
			ItemCooldownManager filter = behaviour.getFilter();
			String feedback = "apply_click_again";
			if (toApply.b() instanceof FilterItem || !behaviour.isCountVisible())
				feedback = "apply";
			else if (ItemHandlerHelper.canItemStacksStack(toApply, filter))
				feedback = "apply_count";
			String translationKey = world.d_(pos)
				.b()
				.i();
			Text formattedText = new TranslatableText(translationKey);
			player.a(Lang.createTranslationTextComponent("logistics.filter." + feedback, formattedText)
				.formatted(Formatting.WHITE), true);
		}

		event.setCanceled(true);
		event.setCancellationResult(Difficulty.SUCCESS);
		world.a(null, pos, MusicType.gF, SoundEvent.e, .25f, .1f);
	}

	@Environment(EnvType.CLIENT)
	public static boolean onScroll(double delta) {
		Box objectMouseOver = KeyBinding.B().v;
		if (!(objectMouseOver instanceof dcg))
			return false;

		dcg result = (dcg) objectMouseOver;
		KeyBinding mc = KeyBinding.B();
		DragonHeadEntityModel world = mc.r;
		BlockPos blockPos = result.a();

		FilteringBehaviour filtering = TileEntityBehaviour.get(world, blockPos, FilteringBehaviour.TYPE);
		if (filtering == null)
			return false;
		if (mc.s.bt())
			return false;
		if (!mc.s.eJ())
			return false;
		if (!filtering.isCountVisible())
			return false;
		if (filtering.slotPositioning instanceof ValueBoxTransform.Sided)
			((Sided) filtering.slotPositioning).fromSide(result.b());
		if (!filtering.testHit(objectMouseOver.e()))
			return false;
		ItemCooldownManager filterItem = filtering.getFilter();
		filtering.ticksUntilScrollPacket = 10;
		int maxAmount = (filterItem.b() instanceof FilterItem) ? 64 : filterItem.c();
		filtering.scrollableValue =
			(int) afj.a(filtering.scrollableValue + delta * (AllKeys.ctrlDown() ? 16 : 1), 0, maxAmount);

		return true;
	}

}
