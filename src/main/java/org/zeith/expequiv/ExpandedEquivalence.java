package org.zeith.expequiv;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;
import org.zeith.hammerlib.event.fml.FMLFingerprintCheckEvent;
import org.zeith.hammerlib.util.CommonMessages;
import org.zeith.hammerlib.util.configured.ConfigFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import static org.zeith.expequiv.ExpandedEquivalence.MOD_ID;

@Mod(MOD_ID)
public class ExpandedEquivalence
{
	public static final Logger LOG = LogManager.getLogger("ExpandedEquivalence");
	public static final String MOD_ID = "expequiv";
	
	public ExpandedEquivalence()
	{
		CommonMessages.printMessageOnIllegalRedistribution(ExpandedEquivalence.class,
				LOG, "Expanded Equivalence", "https://www.curseforge.com/minecraft/mc-mods/expanded-equivalence");
		
		LanguageAdapter.registerMod(MOD_ID);
		
		var bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::checkFingerprint);
	}
	
	private void checkFingerprint(FMLFingerprintCheckEvent e)
	{
		CommonMessages.printMessageOnFingerprintViolation(e, "97e852e9b3f01b83574e8315f7e77651c6605f2b455919a7319e9869564f013c",
				LOG, "Expanded Equivalence", "https://www.curseforge.com/minecraft/mc-mods/expanded-equivalence");
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}
	
	public static ConfigFile openExpansionConfig(ResourceLocation path) throws IOException
	{
		var pth = FMLPaths.CONFIGDIR.get()
				.resolve("ExpandedEquivalence")
				.resolve("built-in expansions")
				.resolve(path.getNamespace())
				.resolve(path.getPath() + ".cfg")
				.normalize();
		
		pth.getParent().toFile().mkdirs();
		
		var cfg = new ConfigFile(pth.toFile());
		
		var ioe = cfg.load().map(wrapOrSelf(IOException.class, IOException::new)).orElse(null);
		if(ioe instanceof FileNotFoundException) return cfg;
		if(ioe != null) throw ioe;
		
		return cfg;
	}
	
	public static <E, T> Function<E, T> wrapOrSelf(Class<T> req, Function<E, T> wrap)
	{
		return (base) ->
		{
			if(req.isInstance(base))
				return (T) base;
			return wrap.apply(base);
		};
	}
}