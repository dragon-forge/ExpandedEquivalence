package org.zeith.expequiv.mixins;

import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.mappers.customConversions.CustomConversionMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.expequiv.ExpandedEquivalence;
import org.zeith.expequiv.PEMapper;
import org.zeith.expequiv.utils.ISubTagFilter;

import java.util.Map;

@Mixin(value = CustomConversionMapper.class, remap = false)
public class CustomConversionMapperMixin
{
	@Inject(
			method = "lambda$addMappingsFromFile$1",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void patchItemTags(IMappingCollector<NormalizedSimpleStack, Long> mapper,
									  Map.Entry<NormalizedSimpleStack, Long> entry,
									  NormalizedSimpleStack e,
									  CallbackInfo ci)
	{
		if(e instanceof NSSItem i && !i.representsTag() && entry.getKey() instanceof NSSItem tag && tag.representsTag())
		{
			var itemLoc = i.getResourceLocation().toString();
			var emc = entry.getValue();
			var tagLoc = tag.getResourceLocation().toString();
			
			if(PEMapper.getInstance().getScriptSystem().gatherBlockers().stream().anyMatch(ISubTagFilter.checkBlockage(itemLoc, emc, tagLoc)))
			{
				// This exact item was blocked from a given tag, so let's cancel!
				ci.cancel();
				ExpandedEquivalence.LOG.info("Cancelled setValueBefore for item " + itemLoc + " of tag #" + tagLoc);
			}
		}
	}
}