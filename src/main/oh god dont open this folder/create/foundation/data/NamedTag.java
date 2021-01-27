package com.simibubi.create.foundation.data;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.tag.RequiredTagList;
import net.minecraft.util.Identifier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NamedTag<T> implements RequiredTagList.e<T> {
	private final Identifier id;
	private final RequiredTagList<T> tag;

	public NamedTag(@Nullable RequiredTagList<T> tag, Identifier id) {
		this.tag = tag;
		this.id = id;
	}

	@Override
	public Identifier a() {
		return id;
	}

	@Override
	public boolean a(T p_230235_1_) {
		if (tag == null)
			return false;
		return tag.a(p_230235_1_);
	}

	@Override
	public List<T> b() {
		if (tag == null)
			return Collections.emptyList();
		return tag.b();
	}
}
