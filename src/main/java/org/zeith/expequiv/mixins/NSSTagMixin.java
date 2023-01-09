package org.zeith.expequiv.mixins;

import com.mojang.datafixers.util.Either;
import moze_intel.projecte.api.nss.*;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.expequiv.PEMapper;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(AbstractNSSTag.class)
public class NSSTagMixin<TYPE>
{
	@Inject(
			method = "lambda$forEachElement$1",
			at = @At("TAIL")
	)
	private void forEachElement_EE(Consumer<NormalizedSimpleStack> consumer, Either<HolderSet.Named<TYPE>, ITag<TYPE>> tag, CallbackInfo ci)
	{
		TagKey<TYPE> key = tag.map(HolderSet.Named::key, ITag::getKey);
		var ss = PEMapper.getInstance().getScriptSystem();
		if(ss != null)
			ss.setupTags().getOrDefault(key, Set.of()).stream().map(NSSItem::createItem).forEach(consumer);
	}
}