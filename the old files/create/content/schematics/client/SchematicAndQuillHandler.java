package com.simibubi.kinetic_api.content.schematics.client;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import afj;
import bnx;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllKeys;
import com.simibubi.kinetic_api.AllSpecialTextures;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.content.schematics.ClientSchematicLoader;
import com.simibubi.kinetic_api.content.schematics.packet.InstantSchematicPacket;
import com.simibubi.kinetic_api.foundation.gui.ScreenOpener;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.utility.FilesHelper;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import com.simibubi.kinetic_api.foundation.utility.RaycastHelper;
import com.simibubi.kinetic_api.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import com.simibubi.kinetic_api.foundation.utility.outliner.Outliner;
import cqx;
import dcg;
import net.minecraft.block.BellBlock;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.potion.PotionUtil;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box.a;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.timer.Timer;

public class SchematicAndQuillHandler {

	private Object outlineSlot = new Object();

	private BlockPos firstPos;
	private BlockPos secondPos;
	private BlockPos selectedPos;
	private Direction selectedFace;
	private int range = 10;

	public boolean mouseScrolled(double delta) {
		if (!isActive())
			return false;
		if (!AllKeys.ctrlDown())
			return false;
		if (secondPos == null)
			range = (int) afj.a(range + delta, 1, 100);
		if (selectedFace == null)
			return true;

		Timer bb = new Timer(firstPos, secondPos);
		Vec3i vec = selectedFace.getVector();
		EntityHitResult projectedView = KeyBinding.B().boundKey.k()
			.b();
		if (bb.d(projectedView))
			delta *= -1;

		int x = (int) (vec.getX() * delta);
		int y = (int) (vec.getY() * delta);
		int z = (int) (vec.getZ() * delta);

		AxisDirection axisDirection = selectedFace.getDirection();
		if (axisDirection == AxisDirection.NEGATIVE)
			bb = bb.d(-x, -y, -z);

		double maxX = Math.max(bb.eventCounter - x * axisDirection.offset(), bb.LOGGER);
		double maxY = Math.max(bb.eventsByName - y * axisDirection.offset(), bb.callback);
		double maxZ = Math.max(bb.f - z * axisDirection.offset(), bb.events);
		bb = new Timer(bb.LOGGER, bb.callback, bb.events, maxX, maxY, maxZ);

		firstPos = new BlockPos(bb.LOGGER, bb.callback, bb.events);
		secondPos = new BlockPos(bb.eventCounter, bb.eventsByName, bb.f);
		Lang.sendStatus(KeyBinding.B().s, "schematicAndQuill.dimensions", (int) bb.b() + 1,
			(int) bb.c() + 1, (int) bb.d() + 1);

		return true;
	}

	public void onMouseInput(int button, boolean pressed) {
		if (!pressed || button != 1)
			return;
		if (!isActive())
			return;

		FishingParticle player = KeyBinding.B().s;

		if (player.bt()) {
			discard();
			return;
		}

		if (secondPos != null) {
			ScreenOpener.open(new SchematicPromptScreen());
			return;
		}

		if (selectedPos == null) {
			Lang.sendStatus(player, "schematicAndQuill.noTarget");
			return;
		}

		if (firstPos != null) {
			secondPos = selectedPos;
			Lang.sendStatus(player, "schematicAndQuill.secondPos");
			return;
		}

		firstPos = selectedPos;
		Lang.sendStatus(player, "schematicAndQuill.firstPos");
	}
	
	public void discard() {
		FishingParticle player = KeyBinding.B().s;
		firstPos = null;
		secondPos = null;
		Lang.sendStatus(player, "schematicAndQuill.abort");
	}

	public void tick() {
		if (!isActive())
			return;

		FishingParticle player = KeyBinding.B().s;
		if (AllKeys.ACTIVATE_TOOL.isPressed()) {
			float pt = KeyBinding.B()
				.ai();
			EntityHitResult targetVec = player.j(pt)
				.e(player.bg()
					.a(range));
			selectedPos = new BlockPos(targetVec);

		} else {
			dcg trace = RaycastHelper.rayTraceRange(player.l, player, 75);
			if (trace != null && trace.c() == a.b) {

				BlockPos hit = trace.a();
				boolean replaceable = player.l.d_(hit)
					.a(new PotionUtil(new bnx(player, ItemScatterer.RANDOM, trace)));
				if (trace.b()
					.getAxis()
					.isVertical() && !replaceable)
					hit = hit.offset(trace.b());
				selectedPos = hit;
			} else
				selectedPos = null;
		}

		selectedFace = null;
		if (secondPos != null) {
			Timer bb = new Timer(firstPos, secondPos).b(1, 1, 1)
				.g(.45f);
			EntityHitResult projectedView = KeyBinding.B().boundKey.k()
				.b();
			boolean inside = bb.d(projectedView);
			PredicateTraceResult result =
				RaycastHelper.rayTraceUntil(player, 70, pos -> inside ^ bb.d(VecHelper.getCenterOf(pos)));
			selectedFace = result.missed() ? null
				: inside ? result.getFacing()
					.getOpposite() : result.getFacing();
		}

		Timer currentSelectionBox = getCurrentSelectionBox();
		if (currentSelectionBox != null)
			outliner().chaseAABB(outlineSlot, currentSelectionBox)
				.colored(0x6886c5)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.highlightFace(selectedFace);
	}

	private Timer getCurrentSelectionBox() {
		if (secondPos == null) {
			if (firstPos == null)
				return selectedPos == null ? null : new Timer(selectedPos);
			return selectedPos == null ? new Timer(firstPos)
				: new Timer(firstPos, selectedPos).b(1, 1, 1);
		}
		return new Timer(firstPos, secondPos).b(1, 1, 1);
	}

	private boolean isActive() {
		return isPresent() && AllItems.SCHEMATIC_AND_QUILL.isIn(KeyBinding.B().s.dC());
	}

	private boolean isPresent() {
		return KeyBinding.B() != null && KeyBinding.B().r != null
			&& KeyBinding.B().y == null;
	}

	public void saveSchematic(String string, boolean convertImmediately) {
		StructureProcessor t = new StructureProcessor();
		cqx bb = new cqx(firstPos, secondPos);
		BlockPos origin = new BlockPos(bb.a, bb.b, bb.c);
		BlockPos bounds = new BlockPos(bb.d(), bb.e(), bb.f());

		t.a(KeyBinding.B().r, origin, bounds, true, BellBlock.FACING);

		if (string.isEmpty())
			string = Lang.translate("schematicAndQuill.fallbackName").getString();

		String folderPath = "schematics";
		FilesHelper.createFolderIfMissing(folderPath);
		String filename = FilesHelper.findFirstValidFilename(string, folderPath, "nbt");
		String filepath = folderPath + "/" + filename;

		Path path = Paths.get(filepath);
		OutputStream outputStream = null;
		try {
			outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE);
			CompoundTag nbttagcompound = t.a(new CompoundTag());
			NbtIo.writeCompressed(nbttagcompound, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null)
				IOUtils.closeQuietly(outputStream);
		}
		firstPos = null;
		secondPos = null;
		Lang.sendStatus(KeyBinding.B().s, "schematicAndQuill.saved", filepath);

		if (!convertImmediately)
			return;
		if (!Files.exists(path)) {
			Create.logger.fatal("Missing Schematic file: " + path.toString());
			return;
		}
		try {
			if (!ClientSchematicLoader.validateSizeLimitation(Files.size(path)))
				return;
			AllPackets.channel.sendToServer(new InstantSchematicPacket(filename, origin, bounds));

		} catch (IOException e) {
			Create.logger.fatal("Error finding Schematic file: " + path.toString());
			e.printStackTrace();
			return;
		}
	}

	private Outliner outliner() {
		return CreateClient.outliner;
	}

}