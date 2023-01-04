/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client;

import ivorius.psychedelicraft.client.sound.MovingSoundDrug;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import ivorius.psychedelicraft.client.rendering.DrugEffectInterpreter;
import ivorius.psychedelicraft.client.rendering.SmoothCameraHelper;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by lukas on 21.02.14.
 */

//TODO: (Sollace) A lot of these are hooks that need reimplementing
public class PSCoreHandlerClient
{
    // Taken from RenderHelper
   // private final Vec3 field_82884_b = Vec3.createVectorHelper(0.20000000298023224D, 1.0D, -0.699999988079071D).normalize();
   // private final Vec3 field_82885_c = Vec3.createVectorHelper(-0.20000000298023224D, 1.0D, 0.699999988079071D).normalize();
    //@SubscribeEvent
    public void onEntityJoinWorld(Entity entity) {
        if (entity.world.isClient) {
            DrugProperties.of(entity).ifPresent(MovingSoundDrug::initializeForEntity);
        }
    }

    //@SubscribeEvent
    public void onRenderOverlay(MatrixStack matrices) {
        //if (event.type == RenderGameOverlayEvent.ElementType.PORTAL)
        MinecraftClient mc = MinecraftClient.getInstance();
        DrugProperties.of((Entity)mc.player).ifPresent(properties -> {
            properties.getDrugRenderer().ifPresent(renderer -> {
                matrices.push();
                renderer.renderOverlaysAfterShaders(
                        matrices,
                        mc.getTickDelta(), properties.asEntity(),
                        properties.asEntity().age,
                        mc.getWindow().getScaledWidth(),
                        mc.getWindow().getScaledHeight(),
                        properties
                );
                matrices.pop();
            });
        });
    }

    /*@SubscribeEvent
    public void renderWorld(RenderWorldEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();

        float partialTicks = event.partialTicks;
        int rendererUpdateCount = mc.ingameGUI.getUpdateCounter();
        float ticks = partialTicks + rendererUpdateCount;

        if (event instanceof RenderWorldEvent.Pre)
        {
//            setPlayerAngles(partialTicks);

            PSRenderStates.preRender(ticks);

            for (String pass : PSRenderStates.getRenderPasses(partialTicks))
            {
                if (!pass.equals("Default"))
                {
                    if (PSRenderStates.startRenderPass(pass, partialTicks, ticks))
                    {
                        mc.entityRenderer.renderWorld(partialTicks, 0L);
                        PSRenderStates.endRenderPass();
                    }
                }
            }

            PSRenderStates.startRenderPass("Default", partialTicks, ticks);
            PSRenderStates.preRender3D(ticks);
        }
        else if (event instanceof RenderWorldEvent.Post)
        {
            PSRenderStates.endRenderPass();

            DrugProperties drugProperties = DrugProperties.getDrugProperties(mc.renderViewEntity);

            if (drugProperties != null && drugProperties.renderer != null)
                drugProperties.renderer.renderOverlaysBeforeShaders(event.partialTicks, mc.renderViewEntity, rendererUpdateCount, mc.displayWidth, mc.displayHeight, drugProperties);

            PSRenderStates.postRender(ticks, partialTicks);
        }
    }*/

    /*@SubscribeEvent
    public void psycheGLEnable(GLSwitchEvent event)
    {
        PSRenderStates.setEnabled(event.cap, event.enable);
    }

    @SubscribeEvent
    public void psycheGLBlendFunc(GLBlendFuncEvent event)
    {
        PSRenderStates.setBlendFunc(event.sFactor, event.dFactor, event.dfactorAlpha, event.dfactorAlpha);
    }

    @SubscribeEvent
    public void psycheGLActiveTexture(GLActiveTextureEvent event)
    {
        GLStateProxy.setActiveTextureUnit(event.texture);
    }

    @SubscribeEvent
    public void standardItemLighting(ItemLightingEvent event)
    {
        if (event.enable)
        {
            float var0 = 0.4F;
            float var1 = 0.6F;
            float var2 = 0.0F;

            PSRenderStates.setGLLightEnabled(true);
            PSRenderStates.setGLLight(0, (float) field_82884_b.xCoord, (float) field_82884_b.yCoord, (float) field_82884_b.zCoord, var1, var2);
            PSRenderStates.setGLLight(1, (float) field_82885_c.xCoord, (float) field_82885_c.yCoord, (float) field_82885_c.zCoord, var1, var2);
            PSRenderStates.setGLLightAmbient(var0);
        }
        else
        {
            PSRenderStates.setGLLightEnabled(false);
        }
    }*/

    public void orientCamera() {
        MinecraftClient mc = MinecraftClient.getInstance();
        DrugProperties.of((Entity)mc.player).ifPresent(properties -> {
            properties.getDrugRenderer().ifPresent(renderer -> {
                renderer.distortScreen(mc.getTickDelta(), mc.player, mc.player.age, properties);
            });
        });
    }

    public void renderHeldItem(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int rendererUpdateCount = mc.inGameHud.getTicks();
        float partialTicks = mc.getTickDelta();

        DrugProperties.of((Entity)mc.player).ifPresent(drugProperties -> {
            float shiftX = DrugEffectInterpreter.getHandShiftX(drugProperties, rendererUpdateCount + partialTicks);
            float shiftY = DrugEffectInterpreter.getHandShiftY(drugProperties, rendererUpdateCount + partialTicks);
            matrices.translate(shiftX, shiftY, 0);
        });
    }

    public void renderEntities() {
        MinecraftClient mc = MinecraftClient.getInstance();
        DrugProperties.of((Entity)mc.player).ifPresent(properties -> {
            properties.getDrugRenderer().ifPresent(renderer -> {
                renderer.renderAllHallucinations(mc.getTickDelta(), properties);
            });
        });
    }

    //@SubscribeEvent
    /*public void renderHand(RenderHandEvent event)
    {
        if (event instanceof RenderHandEvent.Pre)
        {
            if (!"Default".equals(PSRenderStates.currentRenderPass))
            {
                PSRenderStates.setDepthMultiplier(0.0f);
                event.setCanceled(true);
            }
        }
        else if (event instanceof RenderHandEvent.Post)
        {
            if (!"Default".equals(PSRenderStates.currentRenderPass))
            {
                PSRenderStates.setDepthMultiplier(1.0f);
            }
        }
    }*/

    public void setPlayerAngles() {
        MinecraftClient mc = MinecraftClient.getInstance();
        DrugProperties.of((Entity)mc.player).ifPresent(properties -> {
            float smoothness = DrugEffectInterpreter.getSmoothVision(properties);
            if (smoothness < 1 && mc.isWindowFocused()) {
                float deltaX = (float) mc.mouse.getX();
                float deltaY = (float) mc.mouse.getY();

                float[] angles = SmoothCameraHelper.INSTANCE.getAngles(deltaX, deltaY);

                if (!mc.options.smoothCameraEnabled) {
                    float[] originalAngles = SmoothCameraHelper.INSTANCE.getOriginalAngles(deltaX, deltaY);
                    mc.player.changeLookDirection(angles[0] - originalAngles[0], angles[1] - originalAngles[1]);
                } else {
                    mc.player.changeLookDirection(angles[0], angles[1]);
                }
            }
        });
    }

    public float getSoundVolume(float volume) {
        return DrugProperties.of((Entity)MinecraftClient.getInstance().player).map(properties -> {
            return MathHelper.clamp(volume * properties.getSoundMultiplier(), 0, 1);
        }).orElse(volume);
    }

    public void setupCameraTransform(CallbackInfo event) {
        if (PSRenderStates.setupCameraTransform()) {
            event.cancel();
        }
    }

    /*
    //@SubscribeEvent
    public void psycheGLFogi(GLFogiEvent event)
    {
        if (event.pname == GL11.GL_FOG_MODE)
        {
            PSRenderStates.setFogMode(event.param);
        }
    }
*/
/*
    @SubscribeEvent
    public void renderBlockOverlay(RenderBlockOverlayEvent event)
    {
        if (!"Default".equals(PSRenderStates.currentRenderPass))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void fixGLState(GLStateFixEvent event)
    {
        PSRenderStates.setUseScreenTexCoords(false);
        PSRenderStates.setTexture2DEnabled(OpenGlHelper.defaultTexUnit, true);
    }

    @SubscribeEvent
    public void glClear(GLClearEvent event)
    {
        event.currentMask = event.currentMask & PSRenderStates.getCurrentAllowedGLDataMask();
    }

    @SubscribeEvent
    public void renderSkyPre(RenderSkyEvent.Pre event)
    {
        PSRenderStates.preRenderSky(event.partialTicks);
    }*/
}
