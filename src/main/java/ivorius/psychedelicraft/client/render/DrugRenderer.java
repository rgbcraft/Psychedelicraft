/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.effect.*;
import ivorius.psychedelicraft.client.render.shader.PostEffectRenderer;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.client.sound.ClientDrugMusicManager;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.hallucination.DriftingCamera;
import ivorius.psychedelicraft.entity.drug.hallucination.Hallucination;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3d;

import java.lang.Math;

import net.minecraft.util.math.Quaternion;

/**
 * Created by lukas on 17.02.14.
 */
public class DrugRenderer {
    public static final DrugRenderer INSTANCE = new DrugRenderer();

    private final EnvironmentalScreenEffect environmentalEffects = new EnvironmentalScreenEffect();
    private final ScreenEffect screenEffects = CompoundScreenEffect.of(
            new LensFlareScreenEffect(),
            new WarmthOverlayScreenEffect(),
            new AlcoholOverlayScreenEffect(),
            new PowerOverlayScreenEffect(),
            environmentalEffects,
            new TirednessScreenEffect(),
            new MotionBlurScreenEffect()
    );

    private final PostEffectRenderer postEffects = new PostEffectRenderer();

    private final ClientDrugMusicManager musicManager = new ClientDrugMusicManager();

    private float screenBackgroundBlur;

    public ScreenEffect getScreenEffects() {
        return screenEffects;
    }

    public PostEffectRenderer getPostEffects() {
        return postEffects;
    }

    public EnvironmentalScreenEffect getEnvironmentalEffects() {
        return environmentalEffects;
    }

    public float getMenuBlur() {
        return PsychedelicraftClient.getConfig().visual.pauseMenuBlur * screenBackgroundBlur * screenBackgroundBlur * screenBackgroundBlur;
    }

    public void update(DrugProperties drugProperties, LivingEntity entity) {
        if (MinecraftClient.getInstance().isPaused()) {
            screenBackgroundBlur = Math.min(1, screenBackgroundBlur + 0.25F);
        } else {
            screenBackgroundBlur = Math.max(0, screenBackgroundBlur - 0.25F);
        }

        screenEffects.update(ShaderContext.tickDelta());
        musicManager.update(drugProperties);
    }

    public void distortScreen(MatrixStack matrices, float tickDelta) {
        DrugProperties properties = DrugProperties.of(MinecraftClient.getInstance().player);
        if (properties == null) {
            return;
        }

        float wobblyness = Math.min(1, properties.getModifier(Drug.VIEW_WOBBLYNESS));

        float tick = properties.asEntity().age + tickDelta;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        DriftingCamera driftingCam = properties.getHallucinations().getCamera();

        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0f));

        Vec3d cameraOffset = driftingCam.getPosition();
        Vec3d prevCameraOffset = driftingCam.getPrevPosition();
        matrices.translate(
             MathHelper.lerp(tickDelta, prevCameraOffset.x, cameraOffset.x),
             MathHelper.lerp(tickDelta, prevCameraOffset.y, cameraOffset.y),
             MathHelper.lerp(tickDelta, prevCameraOffset.z, cameraOffset.z)
        );
        Vec3d cameraRoll = driftingCam.getRotation();
        Vec3d prevCameraRoll = driftingCam.getPrevRotation();
        matrices.multiply(Quaternion.fromEulerXyz(
                (float)MathHelper.lerp(tickDelta, prevCameraRoll.x, cameraRoll.x),
                (float)MathHelper.lerp(tickDelta, prevCameraRoll.y, cameraRoll.y),
                (float)MathHelper.lerp(tickDelta, prevCameraRoll.z, cameraRoll.z)
        ));

        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0f));
        matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(camera.getPitch()));

        if (wobblyness > 0) {
            float f4 = MathHelper.square(5F / (wobblyness * wobblyness + 5F) - wobblyness * 0.04F);

            float sin1 = MathHelper.sin(tick / 150 * MathHelper.PI);
            float sin2 = MathHelper.sin(tick / 170 * MathHelper.PI);
            float sin3 = MathHelper.sin(tick / 190 * MathHelper.PI);

            float yz = tick * 3F * MathHelper.RADIANS_PER_DEGREE;
            Quaternion rotation = Quaternion.fromEulerXyz(0, yz, yz);
            matrices.multiply(rotation);
            matrices.scale(
                    1F / (f4 + (wobblyness * sin1) / 2),
                    1F / (f4 + (wobblyness * sin2) / 2),
                    1F / (f4 + (wobblyness * sin3) / 2)
            );
            rotation.conjugate();
            matrices.multiply(rotation);
        }

        matrices.translate(
                DrugEffectInterpreter.getCameraShiftX(properties, tick),
                DrugEffectInterpreter.getCameraShiftY(properties, tick),
                0
        );
    }

    public void distortHand(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        float ticks = ShaderContext.ticks();

        DrugProperties.of((Entity)client.player).ifPresent(drugProperties -> {
            float shiftX = DrugEffectInterpreter.getHandShiftX(drugProperties, ticks);
            float shiftY = DrugEffectInterpreter.getHandShiftY(drugProperties, ticks);
            matrices.translate(shiftX, shiftY, 0);
        });
    }

    public void poseModel(PlayerEntity player, BipedEntityModel<?> model) {
        ModelPart head = model.getHead();
        ModelPart leftArm = model.leftArm;
        ModelPart rightArm = model.rightArm;

        DrugProperties properties = DrugProperties.of(player);
        float tick = player.age + MinecraftClient.getInstance().getTickDelta();
        float shiftX = DrugEffectInterpreter.getHandShiftX(properties, tick) * 2;

        leftArm.pitch += shiftX;
        rightArm.pitch -= shiftX;

        float shiftY = DrugEffectInterpreter.getHandShiftY(properties, tick);

        leftArm.roll += shiftY;
        rightArm.roll -= shiftY;

        head.pitch += DrugEffectInterpreter.getCameraShiftX(properties, tick);
        head.yaw += DrugEffectInterpreter.getCameraShiftY(properties, tick);
        head.roll = DrugEffectInterpreter.getAlcohol(properties);

        if (model instanceof PlayerEntityModel<?> pem) {
            pem.hat.copyTransform(head);
            pem.leftSleeve.copyTransform(leftArm);
            pem.rightSleeve.copyTransform(rightArm);
        }
    }

    public void onRenderOverlay(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();

        RenderPhase.SCREEN.push();

        getScreenEffects().render(matrices,
                client.getBufferBuilders().getEntityVertexConsumers(),
                window.getScaledWidth(), window.getScaledHeight(), tickDelta, null);

        postEffects.render(tickDelta);
        client.getFramebuffer().beginWrite(true);

        RenderPhase.pop();
    }

    public void renderAllHallucinations(MatrixStack matrices, VertexConsumerProvider vertices, Camera camera, float tickDelta, DrugProperties drugProperties) {
        HallucinationManager hallucinations = drugProperties.getHallucinations();
        float alpha = MathHelper.clamp(hallucinations.getHallucinationStrength(tickDelta) * 15, 0, 1);
        float forcedAlpha = hallucinations.getEntities().getForcedAlpha(tickDelta);
        if (forcedAlpha > 0) {
            alpha += forcedAlpha;
            alpha /= 2F;
        }

        for (Hallucination h : hallucinations.getEntities()) {
            try {
                h.render(matrices, vertices, camera, tickDelta, alpha);
            } catch (Throwable t) {
                Psychedelicraft.LOGGER.fatal("Exception occured whilst rendering hallucination ", t);
                h.setDead();
                // clean up the matrix stack after an error
                while (!matrices.isEmpty()) {
                    matrices.pop();
                }
            }
        }
    }

}
