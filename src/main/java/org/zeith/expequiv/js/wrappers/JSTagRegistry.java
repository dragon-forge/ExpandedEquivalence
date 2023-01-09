package org.zeith.expequiv.js.wrappers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.*;

public class JSTagRegistry
{
	protected final Map<TagKey<Item>, Set<Item>> tags;
	
	public JSTagRegistry(Map<TagKey<Item>, Set<Item>> tags)
	{
		this.tags = tags;
	}
	
	public void add(ItemLike item, String tagKey)
	{
		tags.computeIfAbsent(ItemTags.create(new ResourceLocation(tagKey)), v -> new HashSet<>()).add(item.asItem());
	}
}