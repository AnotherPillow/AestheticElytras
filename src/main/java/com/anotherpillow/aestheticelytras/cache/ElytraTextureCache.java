package com.anotherpillow.aestheticelytras.cache;

import com.anotherpillow.aestheticelytras.Aestheticelytras;
import com.anotherpillow.aestheticelytras.util.Resolution;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class ElytraTextureCache {
    private static final Identifier DEFAULT = new Identifier("textures/entity/elytra.png");
    private static final Map<String, Identifier> READY = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> IN_FLIGHT = new ConcurrentHashMap<>();

    private static final ExecutorService BACKGROUND_THREAD =
            Executors.newSingleThreadExecutor();

    public static Identifier getOrRequest(String name) {
        if (READY.containsKey(name)) return READY.get(name);

        if (!IN_FLIGHT.containsKey(name)) queueOnlineSkinLoading(name);

        return null;
    }

    private static void queueOnlineSkinLoading(String name)
    {
        IN_FLIGHT.put(name, true);
        MinecraftClient mc = MinecraftClient.getInstance();

        CompletableFuture.supplyAsync(() -> {

            Resolution.getProfileAsync(name).thenAccept((profile) -> {
                Aestheticelytras.LOGGER.info("have now got profile yes"  + profile.toString());
                mc.getSkinProvider().fetchSkinTextures(profile).thenAccept((t) -> {
                    Aestheticelytras.LOGGER.info("uwu textures"  + t);
                    READY.put(name, t.elytraTexture() == null ? t.capeTexture() : t.elytraTexture());
                    IN_FLIGHT.remove(name);
                    Aestheticelytras.LOGGER.info(READY.entrySet());
                    Aestheticelytras.LOGGER.info(IN_FLIGHT.entrySet());
                });

//                SkinTextures t = mc.getSkinProvider().getSkinTextures(profile);
//                Aestheticelytras.LOGGER.info("uwu textures"  + t);
//                READY.put(name, t.elytraTexture() == null ? t.capeTexture() : t.elytraTexture());
//                IN_FLIGHT.remove(name);
//                Aestheticelytras.LOGGER.info(READY.entrySet());
//                Aestheticelytras.LOGGER.info(IN_FLIGHT.entrySet());
            });

            return "";

        }, BACKGROUND_THREAD);
    }
}
