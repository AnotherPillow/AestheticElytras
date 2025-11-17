package com.anotherpillow.aestheticelytras.util;

import com.anotherpillow.aestheticelytras.Aestheticelytras;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.MinecraftClient;

import java.net.Proxy;
import java.util.UUID;
import java.util.concurrent.*;

public class Resolution {
    private static final ExecutorService LOOKUP_EXEC =
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "AE-ProfileLookup");
                t.setDaemon(true);
                return t;
            });

    public static CompletableFuture<GameProfile> getProfileAsync(String name) {
        return CompletableFuture.supplyAsync(() -> {
            YggdrasilAuthenticationService auth =
                    new YggdrasilAuthenticationService(Proxy.NO_PROXY);
            GameProfileRepository repo = auth.createProfileRepository();

            CompletableFuture<GameProfile> result = new CompletableFuture<>();
            repo.findProfilesByNames(
                    new String[] { name },
                    new ProfileLookupCallback() {
                        @Override
                        public void onProfileLookupSucceeded(GameProfile profile) {
                            result.complete(profile);
                        }

                        @Override
                        public void onProfileLookupFailed(String profileName, Exception exception) {
                            Aestheticelytras.LOGGER.warn(
                                    "Profile lookup failed for {}: {}",
                                    profileName, exception.toString());
                            // Complete with null so callers can fallback
                            result.complete(null);
                        }
                    }
            );

            try {
                // Block only on this worker thread, not the render thread
                return result.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                Aestheticelytras.LOGGER.warn("Profile lookup timeout for {}", name);
                return null;
            } catch (Exception e) {
                Aestheticelytras.LOGGER.warn("Profile lookup error for {}: {}", name, e.toString());
                return null;
            }
        }, LOOKUP_EXEC);
    }

    public static void shutdown() {
        LOOKUP_EXEC.shutdown();
    }
}
