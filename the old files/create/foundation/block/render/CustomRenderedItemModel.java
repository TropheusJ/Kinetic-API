package com.simibubi.kinetic_api.foundation.block.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.simibubi.kinetic_api.Create;
import elg;
import net.minecraft.client.input.Input;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.client.resource.metadata.LanguageResourceMetadata;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.event.ModelBakeEvent;

@SuppressWarnings("deprecation")
public abstract class CustomRenderedItemModel extends WrappedBakedModel {

	protected String basePath;
	protected Map<String, elg> partials = new HashMap<>();
	protected b currentPerspective;
	protected Input renderer;

	public CustomRenderedItemModel(elg template, String basePath) {
		super(template);
		this.basePath = basePath;
		this.renderer = createRenderer();
	}

	public final List<Identifier> getModelLocations() {
		return partials.keySet().stream().map(this::getPartialModelLocation).collect(Collectors.toList());
	}
	
	public Input getRenderer() {
		return renderer;
	}

	public abstract Input createRenderer();

	@Override
	public boolean d() {
		return true;
	}

	@Override
	public elg handlePerspective(b cameraTransformType, BufferVertexConsumer mat) {
		currentPerspective = cameraTransformType;
		return super.handlePerspective(cameraTransformType, mat);
	}

	protected void addPartials(String... partials) {
		this.partials.clear();
		for (String name : partials)
			this.partials.put(name, null);
	}

	public CustomRenderedItemModel loadPartials(ModelBakeEvent event) {
		for (String name : partials.keySet())
			partials.put(name, loadModel(event, name));
		return this;
	}

	private elg loadModel(ModelBakeEvent event, String name) {
		return event.getModelLoader().a(getPartialModelLocation(name), LanguageResourceMetadata.READER);
	}

	private Identifier getPartialModelLocation(String name) {
		return new Identifier(Create.ID, "item/" + basePath + "/" + name);
	}

	public b getCurrentPerspective() {
		return currentPerspective;
	}
	
	public elg getPartial(String name) {
		return partials.get(name);
	}

}
