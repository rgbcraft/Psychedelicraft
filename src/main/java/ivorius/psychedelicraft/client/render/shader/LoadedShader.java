package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;

import net.minecraft.util.math.Vec3f;

import com.google.gson.JsonSyntaxException;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import ivorius.psychedelicraft.client.render.shader.UniformBinding.UniformSetter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.util.Identifier;

class LoadedShader extends ShaderEffect {
    private final UniformBinding.Set bindings;

    private int width;
    private int height;
    private float time;
    private float lastTickDelta;

    private List<Pass> passes;

    public LoadedShader(MinecraftClient client, Identifier id, UniformBinding.Set bindings) throws IOException, JsonSyntaxException {
        super(client.getTextureManager(), new ModdedResourceManager(client.getResourceManager(), id.getNamespace()), client.getFramebuffer(), id);
        this.bindings = bindings;
        if (passes == null) {
            passes = new ArrayList<>();
        }
        setupDimensions(
            client.getWindow().getFramebufferWidth(),
            client.getWindow().getFramebufferHeight()
        );
    }

    @Override
    public void render(float tickDelta) {
        if (tickDelta < lastTickDelta) {
            time += 1 - lastTickDelta;
            time += tickDelta;
        } else {
            time += tickDelta - lastTickDelta;
        }
        this.lastTickDelta = tickDelta;
        while (time > 20) {
            time -= 20;
        }

        boolean rendered = false;
        final float passRenderFrame = time / 20F;
        for (Pass pass : passes) {
            rendered |= pass.render(passRenderFrame, tickDelta, rendered);
        }
    }

    @Override
    public void setupDimensions(int targetsWidth, int targetsHeight) {
        this.width = targetsWidth;
        this.height = targetsHeight;
        super.setupDimensions(targetsWidth, targetsHeight);
    }

    @Override
    public PostProcessShader addPass(String programName, Framebuffer source, Framebuffer dest) throws IOException {
        PostProcessShader pass = super.addPass(programName, source, dest);
        if (passes == null) {
            passes = new ArrayList<>();
        }
        passes.add(new Pass(pass));
        return pass;
    }

    class Pass implements UniformSetter {
        private final JsonEffectGlShader program;
        private final PostProcessShader pass;

        private final List<FloatConsumer> replay = new ArrayList<>();
        private int updateCount;

        private boolean rendered;

        public Pass(PostProcessShader pass) {
            this.pass = pass;
            this.program = pass.getProgram();
        }

        public boolean render(float passRenderTime, float tickDelta, boolean rendered) {
            if (pass.getName().equals("blit")) {
                return false;
            }

            if (updateCount == 0) {
                replay.clear();
                var programBindings = bindings.programBindings.getOrDefault(pass.getName(), UniformBinding.EMPTY);
                bindings.global.bindUniforms(this, tickDelta, width, height, () -> {
                    programBindings.bindUniforms(this, tickDelta, width, height, () -> {
                        replay.add(this::renderPass);
                    });
                });
            }

            updateCount = (updateCount + 1) % 2;
            this.rendered = false;
            if (replay.isEmpty()) {
                return this.rendered;
            }

            try {
                for (FloatConsumer action : replay) {
                    action.accept(passRenderTime);
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception rendering shader " + pass.getName(), t);
            }
            return this.rendered;
        }

        private void renderPass(float passRenderTime) {
            pass.render(passRenderTime);
            passes.get(passes.size() - 1).pass.render(passRenderTime);
            rendered = true;
        }

        @Override
        public void set(String name, float value) {
            var uniform = program.getUniformByName(name);
            if (uniform != null) {
                replay.add(uniformSetter(name, f -> uniform.set(value)));
            }
        }

        @Override
        public void set(String name, float... values) {
            var uniform = program.getUniformByName(name);
            if (uniform != null) {
                var copy = Arrays.copyOf(values, values.length);
                replay.add(uniformSetter(name, f -> uniform.set(copy)));
            }
        }

        @Override
        public void set(String name, Vec3f values) {
            var uniform = program.getUniformByName(name);
            if (uniform != null) {
                var copy = values.copy();
                replay.add(uniformSetter(name, f -> uniform.set(copy)));
            }
        }

        private static FloatConsumer uniformSetter(String name, FloatConsumer consumer) {
            return f -> {
                try {
                    consumer.accept(f);
                } catch (Throwable t) {
                    throw new RuntimeException("Exception setting uniform: " + name, t);
                }
            };
        }
    }
}