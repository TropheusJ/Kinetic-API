package com.simibubi.kinetic_api.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import bqx;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.content.contraptions.fluids.FluidTransportBehaviour.AttachmentTypes;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.block.connected.BakedModelWrapperWithData;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import elg;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public class PipeAttachmentModel extends BakedModelWrapperWithData {

	private static ModelProperty<PipeModelData> PIPE_PROPERTY = new ModelProperty<>();

	public PipeAttachmentModel(elg template) {
		super(template);
	}

	@Override
	protected Builder gatherModelData(Builder builder, bqx world, BlockPos pos, PistonHandler state) {
		PipeModelData data = new PipeModelData();
		FluidTransportBehaviour transport = TileEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
		BracketedTileEntityBehaviour bracket = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);

		if (transport != null)
			for (Direction d : Iterate.directions)
				data.putRim(d, transport.getRenderedRimAttachment(world, pos, state, d));
		if (bracket != null)
			data.putBracket(bracket.getBracket());

		data.setEncased(FluidPipeBlock.shouldDrawCasing(world, pos, state));
		return builder.withInitial(PIPE_PROPERTY, data);
	}

	@Override
	public List<SpriteTexturedVertexConsumer> getQuads(PistonHandler state, Direction side, Random rand, IModelData data) {
		List<SpriteTexturedVertexConsumer> quads = super.getQuads(state, side, rand, data);
		if (data instanceof ModelDataMap) {
			ModelDataMap modelDataMap = (ModelDataMap) data;
			if (modelDataMap.hasProperty(PIPE_PROPERTY)) {
				quads = new ArrayList<>(quads);
				addQuads(quads, state, side, rand, modelDataMap, modelDataMap.getData(PIPE_PROPERTY));
			}
		}
		return quads;
	}

	private void addQuads(List<SpriteTexturedVertexConsumer> quads, PistonHandler state, Direction side, Random rand, IModelData data,
		PipeModelData pipeData) {
		for (Direction d : Iterate.directions)
			if (pipeData.hasRim(d))
				quads.addAll(AllBlockPartials.PIPE_ATTACHMENTS.get(pipeData.getRim(d))
					.get(d)
					.get()
					.getQuads(state, side, rand, data));
		if (pipeData.isEncased())
			quads.addAll(AllBlockPartials.FLUID_PIPE_CASING.get()
				.getQuads(state, side, rand, data));
		elg bracket = pipeData.getBracket();
		if (bracket != null)
			quads.addAll(bracket.getQuads(state, side, rand, data));
	}

	private class PipeModelData {
		AttachmentTypes[] rims;
		boolean encased;
		elg bracket;

		public PipeModelData() {
			rims = new AttachmentTypes[6];
			Arrays.fill(rims, AttachmentTypes.NONE);
		}

		public void putBracket(PistonHandler state) {
			this.bracket = KeyBinding.B()
				.aa()
				.a(state);
		}

		public elg getBracket() {
			return bracket;
		}

		public void putRim(Direction face, AttachmentTypes rim) {
			rims[face.getId()] = rim;
		}

		public void setEncased(boolean encased) {
			this.encased = encased;
		}

		public boolean hasRim(Direction face) {
			return rims[face.getId()] != AttachmentTypes.NONE;
		}

		public AttachmentTypes getRim(Direction face) {
			return rims[face.getId()];
		}

		public boolean isEncased() {
			return encased;
		}
	}

}
