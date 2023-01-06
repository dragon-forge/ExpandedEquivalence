package org.zeith.expequiv.mixins;

import moze_intel.projecte.emc.EMCMappingHandler;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.expequiv.PEMapper;
import org.zeith.expequiv.js.ScriptSystem;

@Mixin(value = EMCMappingHandler.class, remap = false)
public class EMCMappingHandlerMixin
{
	@Inject(
			method = "map",
			at = @At("HEAD")
	)
	private static void map_EE(ReloadableServerResources serverResources, ResourceManager resourceManager, CallbackInfo ci)
	{
		PEMapper.getInstance()
				.newScriptSystem(new ScriptSystem(serverResources, resourceManager));
	}
}