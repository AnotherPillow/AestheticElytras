package com.anotherpillow.aestheticelytras.mixin;

import com.anotherpillow.aestheticelytras.Aestheticelytras;
import com.anotherpillow.aestheticelytras.cache.ElytraTextureCache;
import com.anotherpillow.aestheticelytras.cache.UUIDCache;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Mixin(ElytraFeatureRenderer.class)
public class ElytraFeatureRendererMixin {
    private static final Identifier SKIN = new Identifier("textures/entity/elytra.png");

    @Inject(
            method= "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at=@At("HEAD"),
            cancellable = true
    )
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
           int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) throws ExecutionException, InterruptedException {
        ItemStack itemStack = livingEntity.getEquippedStack(EquipmentSlot.CHEST);

        String itemName = itemStack.getName().getString();
        boolean hasCustomName = !Objects.equals(itemName, "Elytra");

        if (itemStack.isOf(Items.ELYTRA)) {
            Identifier identifier = SKIN;

            if (livingEntity instanceof AbstractClientPlayerEntity player) {
                if (hasCustomName) {
                    identifier = ElytraTextureCache.getOrRequest(itemName);
                    if (identifier == null) identifier = SKIN;
                } else {
                    SkinTextures skin = player.getSkinTextures();
                    if (skin != null) {
                        if (skin.elytraTexture() != null) identifier = skin.elytraTexture();
                        else if (skin.capeTexture() != null && player.isPartVisible(PlayerModelPart.CAPE))
                            identifier = skin.capeTexture();
                    }
                }
            }

            matrixStack.push();
            matrixStack.translate(0.0F, 0.0F, 0.125F);
            ((ElytraFeatureRenderer<LivingEntity, EntityModel<LivingEntity>>) (Object) this).getContextModel().copyStateTo(
                    ((ElytraFeatureRenderer<LivingEntity, EntityModel<LivingEntity>>) (Object) this).elytra);
            ((ElytraFeatureRenderer<LivingEntity, EntityModel<LivingEntity>>) (Object) this).elytra.setAngles(livingEntity, f, g, j, k, l);
            VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(
                    vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(identifier), false, itemStack.hasGlint()
            );
            ((ElytraFeatureRenderer<LivingEntity, EntityModel<LivingEntity>>) (Object) this).elytra.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.pop();
        }
        ci.cancel();
    }
}
