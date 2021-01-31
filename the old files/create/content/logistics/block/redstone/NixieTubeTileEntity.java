package com.simibubi.kinetic_api.content.logistics.block.redstone;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import apx;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.kinetic_api.foundation.item.TooltipHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.Couple;
import com.simibubi.kinetic_api.foundation.utility.Pair;

public class NixieTubeTileEntity extends SmartTileEntity {

	Optional<Pair<Text, Integer>> customText;
	JsonElement rawCustomText;
	Couple<String> renderText;

	int redstoneStrength;

	public NixieTubeTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		redstoneStrength = 0;
		customText = Optional.empty();
	}

	@Override
	public void aj_() {
		super.aj_();

		// Dynamic text components have to be ticked manually and re-sent to the client
		if (customText.isPresent() && d instanceof ServerWorld) {
			Pair<Text, Integer> textSection = customText.get();
			textSection.setFirst(updateDynamicTextComponents(Text.Serializer.fromJson(rawCustomText)));

			Couple<String> currentText = getVisibleText();
			if (renderText != null && renderText.equals(currentText))
				return;

			renderText = currentText;
			sendData();
		}
	}

	//

	public void clearCustomText() {
		if (!customText.isPresent())
			return;
		displayRedstoneStrength(0);
	}

	public void displayCustomNameOf(ItemCooldownManager stack, int nixiePositionInRow) {
		CompoundTag compoundnbt = stack.b("display");
		if (compoundnbt != null && compoundnbt.contains("Name", 8)) {
			JsonElement fromJson = getJsonFromString(compoundnbt.getString("Name"));
			Text displayed = Text.Serializer.fromJson(fromJson);
			if (this.d instanceof ServerWorld)
				displayed = updateDynamicTextComponents(displayed);
			this.customText = Optional.of(Pair.of(displayed, nixiePositionInRow));
			this.rawCustomText = fromJson;
			notifyUpdate();
		}
	}

	public void displayRedstoneStrength(int signalStrength) {
		customText = Optional.empty();
		redstoneStrength = signalStrength;
		notifyUpdate();
	}

	public boolean reactsToRedstone() {
		return !customText.isPresent();
	}

	public Couple<String> getVisibleText() {
		if (!customText.isPresent())
			return Couple.create(redstoneStrength < 10 ? "0" : "1", redstoneStrength % 10 + "");
		String fullText = TooltipHelper.getUnformattedDeepText(customText.get()
			.getFirst());
		int index = customText.get()
			.getSecond() * 2;
		return Couple.create(charOrEmpty(fullText, index), charOrEmpty(fullText, index + 1));
	}

	//

	@Override
	protected void fromTag(PistonHandler state, CompoundTag nbt, boolean clientPacket) {
		customText = Optional.empty();
		redstoneStrength = nbt.getInt("RedstoneStrength");
		if (nbt.contains("CustomText")) {
			Text displayed = Text.Serializer.fromJson(nbt.getString("CustomText"));
			rawCustomText = getJsonFromString(nbt.getString("RawCustomText"));
			customText = Optional.of(Pair.of(displayed, nbt.getInt("CustomTextIndex")));
		}
		super.fromTag(state, nbt, clientPacket);
	}

	protected void write(CompoundTag nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		nbt.putInt("RedstoneStrength", redstoneStrength);

		if (customText.isPresent()) {
			nbt.putString("RawCustomText", rawCustomText.toString());
			nbt.putString("CustomText", Text.Serializer.toJson(customText.get()
				.getFirst()));
			nbt.putInt("CustomTextIndex", customText.get()
				.getSecond());
		}
	}

	private JsonElement getJsonFromString(String string) {
		return new JsonParser().parse(string);
	}

	protected Text updateDynamicTextComponents(Text customText) {
		try {
			return Texts.a(this.getCommandSource(null), customText,
				(apx) null, 0);
		} catch (CommandSyntaxException e) {
		}
		return customText;
	}

	// From SignTileEntity
	protected ServerCommandSource getCommandSource(@Nullable ServerPlayerEntity p_195539_1_) {
		String s = p_195539_1_ == null ? "Sign"
			: p_195539_1_.Q()
				.getString();
		Text itextcomponent =
			(Text) (p_195539_1_ == null ? new LiteralText("Sign") : p_195539_1_.d());
		return new ServerCommandSource(CommandOutput.DUMMY,
			new EntityHitResult((double) this.e.getX() + 0.5D, (double) this.e.getY() + 0.5D,
				(double) this.e.getZ() + 0.5D),
			BlockHitResult.a, (ServerWorld) this.d, 2, s, itextcomponent, this.d.l(), p_195539_1_);
	}

	private String charOrEmpty(String string, int index) {
		return string.length() <= index ? " " : string.substring(index, index + 1);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

}
