/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.util.MathUtils;

import org.jetbrains.annotations.Nullable;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;

/**
 * Created by lukas on 27.10.14.
 * Updated by Sollace on 5 Jan 2023
 */
public class FluidBoxRenderer {
    private static final TextureBounds DEFAULT_BOUNDS = new TextureBounds(0, 0, 1, 1);
    private static final float[] DEFAULT_COLOR = new float[] {1, 1, 1, 1};
    private static final Vector4f POSITION_VECTOR = new Vector4f(0, 0, 0, 1);
    private static final FluidBoxRenderer INSTANCE = new FluidBoxRenderer();

    public static FluidBoxRenderer getInstance() {
        return INSTANCE;
    }

    private float scale = 1;
    private int light = 0;
    private int overlay = 0;

    private TextureBounds sprite = DEFAULT_BOUNDS;

    @Nullable
    private VertexConsumer buffer;

    private float[] color = DEFAULT_COLOR;

    @Nullable
    private Matrix4f position;

    private FluidBoxRenderer() { }

    public FluidBoxRenderer scale(float scale) {
        this.scale = scale;
        return this;
    }

    public FluidBoxRenderer light(int light) {
        this.light = light;
        return this;
    }

    public FluidBoxRenderer overlay(int overlay) {
        this.overlay = overlay;
        return this;
    }

    public FluidBoxRenderer position(MatrixStack position) {
        this.position = position.peek().getPositionMatrix();
        return this;
    }

    public FluidBoxRenderer texture(VertexConsumerProvider vertices, Resovoir tank) {
        SimpleFluid fluid = tank.getFluidType();

        if (fluid.isEmpty()) {
            texture(vertices, tank.getStack());
        } else {
            FluidAppearance appearance = FluidAppearance.of(fluid, tank.getStack());

            sprite = appearance.frame();
            color = appearance.rgba();
            buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(appearance.texture()));
        }

        return this;
    }

    public FluidBoxRenderer texture(VertexConsumerProvider vertices, ItemStack stack) {
        sprite = new TextureBounds(MinecraftClient.getInstance().getItemRenderer().getModels().getModel(stack).getParticleSprite());
        buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE));
        color = DEFAULT_COLOR;
        return this;
    }

    public void draw(float x, float y, float z, float width, float height, float length, Direction... directions) {
        renderFluidFace(x, y, z, width, height, length, directions);
    }

    private void vertex(float x, float y, float z, float u, float v, Direction direction) {
        POSITION_VECTOR.set(x, y, z, 1);
        POSITION_VECTOR.transform(position);
        buffer.vertex(
                POSITION_VECTOR.getX() * scale, POSITION_VECTOR.getY() * scale, POSITION_VECTOR.getZ() * scale,
                color[0], color[1], color[2], color[3],
                u, v,
                overlay, light,
                direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()
        );
    }

    private void renderFluidFace(float x, float y, float z, float width, float height, float length, Direction... directions) {
        for (Direction direction : directions) {
            switch (direction) {
                case DOWN:
                    vertex(x, y, z, sprite.x0, sprite.y0, direction);
                    vertex(x + width, y, z, sprite.x1, sprite.y0, direction);
                    vertex(x + width, y, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x, y, z + length, sprite.x0, sprite.y1, direction);
                    break;
                case UP:
                    vertex(x, y + height, z, sprite.x0, sprite.y0, direction);
                    vertex(x, y + height, z + length, sprite.x0, sprite.y1, direction);
                    vertex(x + width, y + height, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x + width, y + height, z, sprite.x1, sprite.y0, direction);
                    break;
                case EAST:
                    vertex(x + width, y, z, sprite.x0, sprite.y0, direction);
                    vertex(x + width, y + height, z, sprite.x1, sprite.y0, direction);
                    vertex(x + width, y + height, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x + width, y, z + length, sprite.x0, sprite.y1, direction);
                    break;
                case WEST:
                    vertex(x, y, z, sprite.x0, sprite.y0, direction);
                    vertex(x, y, z + length, sprite.x1, sprite.y0, direction);
                    vertex(x, y + height, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x, y + height, z, sprite.x0, sprite.y1, direction);
                    break;
                case NORTH:
                    vertex(x, y, z, sprite.x0, sprite.y0, direction);
                    vertex(x, y + height, z, sprite.x0, sprite.y1, direction);
                    vertex(x + width, y + height, z, sprite.x1, sprite.y1, direction);
                    vertex(x + width, y, z, sprite.x1, sprite.y0, direction);
                    break;
                case SOUTH:
                    vertex(x, y, z + length, sprite.x0, sprite.y0, direction);
                    vertex(x + width, y, z + length, sprite.x1, sprite.y0, direction);
                    vertex(x + width, y + height, z + length, sprite.x1, sprite.y1, direction);
                    vertex(x, y + height, z + length, sprite.x0, sprite.y1, direction);
                    break;
            }
        }
    }

    public record TextureBounds(float x0, float x1, float y0, float y1) {
        TextureBounds(Sprite sprite) {
            this(sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
        }
    }

    public record FluidAppearance(Identifier texture, Sprite sprite, int color) {
        public static FluidAppearance of(SimpleFluid fluid, ItemStack stack) {
            return fluid.getFlowTexture(stack).map(texture -> {
                Sprite sprite = MinecraftClient.getInstance().getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).getSprite(texture);
                return new FluidAppearance(sprite.getAtlas().getId(), sprite, 0xFFFFFFFF);
            }).orElseGet(() -> {
                int color = fluid.getColor(stack);
                Sprite sprite = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();

                if (!fluid.isCustomFluid()) {
                    FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid.getPhysical().getStandingFluid());
                    if (handler != null) {
                        FluidState state = fluid.getPhysical().getStandingFluid().getDefaultState();
                        color = handler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos(), state);
                        sprite = handler.getFluidSprites(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos(), state)[0];
                    }
                }

                return new FluidAppearance(sprite.getAtlas().getId(), sprite, color);
            });
        }

        public static int getItemColor(SimpleFluid fluid, ItemStack stack) {
            if (!fluid.isCustomFluid()) {
                FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid.getPhysical().getStandingFluid());
                if (handler != null) {
                    FluidState state = fluid.getPhysical().getStandingFluid().getDefaultState();
                    return handler.getFluidColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos(), state);
                }
            }

            return fluid.getColor(stack);
        }

        public float[] rgba() {
            return new float[] {
                    MathUtils.r(color),
                    MathUtils.g(color),
                    MathUtils.b(color),
                    1
            };
        }

        public TextureBounds frame() {
            float x0 = sprite.getFrameU(0);
            float x1 = sprite.getFrameU(16);

            float y0 = sprite.getFrameV(0);
            float y1 = sprite.getFrameV(16);

            float delta = sprite.getAnimationFrameDelta();

            float totalX = (x0 + x1) / 2F;
            float totalY = (y0 + y1) / 2F;

            return new TextureBounds(
                MathHelper.lerp(delta, x0, totalX),
                MathHelper.lerp(delta, x1, totalX),
                MathHelper.lerp(delta, y0, totalY),
                MathHelper.lerp(delta, y1, totalY)
            );
        }
    }

}
