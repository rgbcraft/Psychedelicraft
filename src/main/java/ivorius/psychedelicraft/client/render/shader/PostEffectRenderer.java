package ivorius.psychedelicraft.client.render.shader;

import java.util.*;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import net.minecraft.client.gl.ShaderEffect;

public class PostEffectRenderer {
    private List<LoadedShader> shaders = new ArrayList<>();

    public void render(float tickDelta) {
        if (PsychedelicraftClient.getConfig().visual.shader2DEnabled) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.resetTextureMatrix();

            if (shaders.size() == 1) {
                shaders.get(0).render(tickDelta);
            } else {
                shaders.forEach(shader -> shader.render(tickDelta));
            }
        }
    }

    public void setupDimensions(int width, int height) {
        shaders.forEach(shader -> shader.setupDimensions(width, height));
    }

    public void onShadersLoaded(List<LoadedShader> shaders) {
        List<LoadedShader> oldShaders = this.shaders;
        this.shaders = shaders;
        oldShaders.forEach(ShaderEffect::close);
    }
}
