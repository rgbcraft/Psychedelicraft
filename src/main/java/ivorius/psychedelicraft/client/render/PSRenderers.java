package ivorius.psychedelicraft.client.render;

import org.jetbrains.annotations.Nullable;

import com.terraformersmc.terraform.boat.api.client.TerraformBoatClientHelper;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.client.particle.PSParticleFactories;
import ivorius.psychedelicraft.client.render.blocks.*;
import ivorius.psychedelicraft.client.render.shader.PSShaders;
import ivorius.psychedelicraft.entity.*;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSRenderers {
    static void bootstrap() {
        EntityRendererRegistry.register(PSEntities.MOLOTOV_COCKTAIL, context -> new FlyingItemEntityRenderer<>(context, 1, true));
        EntityRendererRegistry.register(PSEntities.REALITY_RIFT, RealityRiftEntityRenderer::new);

        BlockEntityRendererFactories.register(PSBlockEntities.DISTILLERY, FlaskBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PSBlockEntities.FLASK, FlaskBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PSBlockEntities.MASH_TUB, MashTubBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PSBlockEntities.BARREL, BarrelBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PSBlockEntities.DRYING_TABLE, DryingTableBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PSBlockEntities.RIFT_JAR, RiftJarBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PSBlockEntities.BOTTLE_RACK, BottleRackBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PSBlockEntities.PEYOTE, PeyoteBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PSBlockEntities.PLACED_DRINK, DrinksBlockEntityRenderer::new);

        ModelLoadingRegistry.INSTANCE.registerModelProvider(PlacedDrinksModelProvider.INSTANCE);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), PSBlocks.DISTILLERY, PSBlocks.FLASK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                PSBlocks.JUNIPER_SAPLING, PSBlocks.JUNIPER_LEAVES, PSBlocks.FRUITING_JUNIPER_LEAVES, PSBlocks.LATTICE, PSBlocks.WINE_GRAPE_LATTICE, PSBlocks.MORNING_GLORY_LATTICE,
                PSBlocks.CANNABIS, PSBlocks.HOP, PSBlocks.TOBACCO, PSBlocks.COCA, PSBlocks.COFFEA, PSBlocks.MORNING_GLORY,
                PSBlocks.AGAVE_PLANT,
                PSBlocks.JIMSONWEEED, PSBlocks.BELLADONNA, PSBlocks.TOMATOES,
                PSBlocks.MASH_TUB, PSBlocks.JUNIPER_DOOR, PSBlocks.JUNIPER_TRAPDOOR,
                PSBlocks.POTTED_CANNABIS, PSBlocks.POTTED_JUNIPER_SAPLING, PSBlocks.POTTED_MORNING_GLORY,
                PSBlocks.POTTED_HOP, PSBlocks.POTTED_TOBACCO, PSBlocks.POTTED_COCA, PSBlocks.POTTED_COFFEA);

        BuiltinItemRendererRegistry.INSTANCE.register(PSItems.RIFT_JAR, RiftJarBlockEntityRenderer::renderStack);

        SimpleFluid.all().forEach(fluid -> {
            FluidRenderHandlerRegistry.INSTANCE.register(fluid.getPhysical().getStandingFluid(), fluid.getPhysical().getFlowingFluid(), new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL, SimpleFluidRenderHandler.WATER_FLOWING, SimpleFluidRenderHandler.WATER_OVERLAY, 0) {
                @Override
                public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
                    return fluid.getColor(fluid.getDefaultStack());
                }
            });
        });

        TerraformBoatClientHelper.registerModelLayers(Psychedelicraft.id("juniper"));

        PSParticleFactories.bootstrap();
        PSShaders.bootstrap();
    }
}
