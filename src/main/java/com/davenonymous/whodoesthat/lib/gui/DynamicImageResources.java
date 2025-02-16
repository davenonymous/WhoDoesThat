package com.davenonymous.whodoesthat.lib.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.neoforge.common.util.Size2i;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforgespi.language.IModInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DynamicImageResources {
	public record ModLogo(ResourceLocation resource, Size2i size) {
	}

	private static Map<String, ModLogo> modLogos = new HashMap<>();

	public static Optional<ModLogo> getModLogo(IModInfo modInfo) {
		String modId = modInfo.getModId();
		if(modLogos.containsKey(modId)) {
			return Optional.of(modLogos.get(modId));
		}

		Optional<String> logoFile = modInfo.getLogoFile();
		if(logoFile.isEmpty()) {
			return Optional.empty();
		}

		Optional<Pack.ResourcesSupplier> optPack = ResourcePackLoader.getPackFor(modInfo.getModId());
		if(optPack.isEmpty()) {
			return Optional.empty();
		}

		Pack.ResourcesSupplier resourcePack = optPack.get();
		TextureManager tm = Minecraft.getInstance().getTextureManager();

		try(PackResources packResources = resourcePack.openPrimary(new PackLocationInfo("mod/" + modInfo.getModId(), Component.empty(), PackSource.BUILT_IN, Optional.empty()))) {
			NativeImage logo = null;
			IoSupplier<InputStream> logoResource = packResources.getRootResource(logoFile.get().split("[/\\\\]"));
			if(logoResource != null) {
				logo = NativeImage.read(logoResource.get());
			}

			if(logo != null) {
				ResourceLocation resource = tm.register(
					"modlogo_" + modInfo.getModId(), new DynamicTexture(logo) {
						public void upload() {
							this.bind();
							NativeImage td = this.getPixels();
							this.getPixels().upload(0, 0, 0, 0, 0, td.getWidth(), td.getHeight(), modInfo.getLogoBlur(), false, false, false);
						}
					}
				);

				modLogos.put(modId, new ModLogo(resource, new Size2i(logo.getWidth(), logo.getHeight())));
			}
		} catch (IllegalArgumentException | IOException var11) {
		}

		return Optional.ofNullable(modLogos.get(modId));
	}
}
