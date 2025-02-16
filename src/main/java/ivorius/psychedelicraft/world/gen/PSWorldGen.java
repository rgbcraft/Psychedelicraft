/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.world.gen;

import java.util.List;
import java.util.function.Predicate;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.AgavePlantBlock;
import ivorius.psychedelicraft.block.CannabisPlantBlock;
import ivorius.psychedelicraft.block.NightshadeBlock;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.PeyoteBlock;
import ivorius.psychedelicraft.block.VineStemBlock;
import ivorius.psychedelicraft.config.BiomeSelector;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.world.gen.structure.MutableStructurePool;
import net.fabricmc.fabric.api.biome.v1.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.*;
import net.minecraft.tag.BiomeTags;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.placementmodifier.*;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.RandomizedIntBlockStateProvider;
import net.minecraft.world.gen.trunk.ForkingTrunkPlacer;

/**
 * Created by lukas on 25.04.14.
 * Updated by Sollace on 16 Jan 2023
 */
public class PSWorldGen {
    public static final TilledPatchFeature TILLED_PATCH_FEATURE = Registry.register(Registry.FEATURE, Psychedelicraft.id("tilled_patch"), new TilledPatchFeature());

    public static RegistryEntry<ConfiguredFeature<?, ?>> JUNIPER_TREE_CONFIG;
    public static RegistryEntry<PlacedFeature> JUNIPER_TREE_PLACEMENT;

    public static RegistryKey<ConfiguredFeature<?, ?>> createConfiguredFeature(String name) {
        return RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, Psychedelicraft.id(name));
    }

    public static RegistryKey<PlacedFeature> createPlacement(String id) {
        return RegistryKey.of(Registry.PLACED_FEATURE_KEY, Psychedelicraft.id(id));
    }

    private static void registerTilledPatch(String id, CannabisPlantBlock crop, boolean requireWater, PSConfig.Balancing.Generation.FeatureConfig config) {
        var cannabisPatch =
        FeatureRegistry.registerConfiguredFeature(createConfiguredFeature(id + "_tilled_patch"), () -> {
            return new ConfiguredFeature<>(TILLED_PATCH_FEATURE, new TilledPatchFeature.Config(requireWater, crop));
        });

        var placement =
        FeatureRegistry.registerPlacedFeature(createPlacement(id + "_tilled_patch_checked"), cannabisPatch, feature -> {
            return new PlacedFeature(feature, List.of(
                    RarityFilterPlacementModifier.of(160),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of()
            ));
        });

        config.ifEnabled(spawnableBiomes -> {
            BiomeModifications.addFeature(
                    spawnableBiomes.createPredicate(BiomeSelectors.foundInOverworld().and(
                            BiomeSelector.COLD
                            .or(BiomeSelectors.tag(BiomeTags.IS_HILL))
                            .or(BiomeSelectors.tag(BiomeTags.IS_FOREST))
                            .or(ctx -> ctx.getBiomeKey() == BiomeKeys.PLAINS)
                    )),
                    GenerationStep.Feature.VEGETAL_DECORATION,
                    placement.getKey().orElseThrow()
            );
        });
    }

    private static void registerUnTilledPatch(String id, Block plant, IntProperty ageProperty, IntProvider ageRange, Predicate<BiomeSelectionContext> builtinBiomePredicate, PSConfig.Balancing.Generation.FeatureConfig config) {
        var patch =

        FeatureRegistry.registerConfiguredFeature(createConfiguredFeature(id + "_patch"), () -> {
            return new ConfiguredFeature<>(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(
                    5,
                    PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK,
                    new SimpleBlockFeatureConfig(new RandomizedIntBlockStateProvider(BlockStateProvider.of(plant), ageProperty, ageRange)))
            ));
        });

        var placement =

        FeatureRegistry.registerPlacedFeature(createPlacement(id + "_patch_checked"), patch, feature -> {
            return new PlacedFeature(feature, List.of(
                    RarityFilterPlacementModifier.of(20),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of()));
        });

        FeatureRegistry.registerPlacedFeature(createPlacement(id + "_patch_unchecked"), patch, feature -> {
            return new PlacedFeature(feature, List.of());
        });

        config.ifEnabled(spawnableBiomes -> {
            BiomeModifications.addFeature(
                    spawnableBiomes.createPredicate(builtinBiomePredicate),
                    GenerationStep.Feature.VEGETAL_DECORATION,
                    placement.getKey().orElseThrow()
            );
        });
    }
    public static void bootstrap() {
        var genConf = Psychedelicraft.getConfig().balancing.worldGeneration;

        JUNIPER_TREE_CONFIG = FeatureRegistry.registerConfiguredFeature(createConfiguredFeature("juniper_tree"), () -> {
            return new ConfiguredFeature<>(Feature.TREE, new TreeFeatureConfig.Builder(
                    BlockStateProvider.of(PSBlocks.JUNIPER_LOG),
                    new ForkingTrunkPlacer(5, 2, 2),
                    BlockStateProvider.of(PSBlocks.JUNIPER_LEAVES),
                    new BlobFoliagePlacer(
                            ConstantIntProvider.create(2),
                            ConstantIntProvider.ZERO,
                            3
                    ),
                    new TwoLayersFeatureSize(1, 0, 2))
            .dirtProvider(BlockStateProvider.of(Blocks.ROOTED_DIRT))
            .forceDirt()
            .build());
        });
        JUNIPER_TREE_PLACEMENT = FeatureRegistry.registerPlacedFeature(createPlacement("juniper_tree_checked"), JUNIPER_TREE_CONFIG, config -> {
            return new PlacedFeature(config, VegetationPlacedFeatures.modifiersWithWouldSurvive(
                    PlacedFeatures.createCountExtraModifier(1, 0.05F, 2),
                    PSBlocks.JUNIPER_SAPLING)
            );
        });

        genConf.juniper.ifEnabled(spawnableBiomes -> {
            BiomeModifications.addFeature(
                    spawnableBiomes.createPredicate(
                        BiomeSelectors.foundInOverworld().and(BiomeSelector.DRY).and(
                                BiomeSelectors.tag(BiomeTags.IS_HILL)
                            .or(BiomeSelectors.tag(BiomeTags.IS_FOREST))
                        )
                    ),
                    GenerationStep.Feature.VEGETAL_DECORATION,
                    JUNIPER_TREE_PLACEMENT.getKey().orElseThrow()
            );
        });

        registerTilledPatch("cannabis", PSBlocks.CANNABIS, false, genConf.cannabis);
        registerTilledPatch("hop", PSBlocks.HOP, false, genConf.hop);
        registerTilledPatch("tobacco", PSBlocks.TOBACCO, false, genConf.tobacco);
        registerTilledPatch("coffea", PSBlocks.COFFEA, false, genConf.coffea);
        registerTilledPatch("coca", PSBlocks.COCA, true, genConf.coca);
        registerUnTilledPatch("morning_glory", PSBlocks.MORNING_GLORY, VineStemBlock.AGE, UniformIntProvider.create(0, VineStemBlock.MAX_AGE), BiomeSelectors.includeByKey(
                BiomeKeys.FLOWER_FOREST,
                BiomeKeys.SUNFLOWER_PLAINS,
                BiomeKeys.MEADOW,
                BiomeKeys.LUSH_CAVES
        ), genConf.morningGlories);
        registerUnTilledPatch("belladonna", PSBlocks.BELLADONNA, NightshadeBlock.AGE, UniformIntProvider.create(0, NightshadeBlock.MAX_AGE), BiomeSelectors.includeByKey(BiomeKeys.DARK_FOREST), genConf.belladonna);
        registerUnTilledPatch("jimsonweed", PSBlocks.JIMSONWEEED, NightshadeBlock.AGE, UniformIntProvider.create(0, NightshadeBlock.MAX_AGE), BiomeSelectors.includeByKey(BiomeKeys.JUNGLE, BiomeKeys.SPARSE_JUNGLE), genConf.jimsonweed);
        registerUnTilledPatch("tomato", PSBlocks.TOMATOES, NightshadeBlock.AGE, UniformIntProvider.create(0, NightshadeBlock.MAX_AGE), BiomeSelectors.includeByKey(BiomeKeys.FOREST), genConf.tomato);
        registerUnTilledPatch("peyote", PSBlocks.PEYOTE, PeyoteBlock.AGE, UniformIntProvider.create(0, PeyoteBlock.MAX_AGE), BiomeSelectors.foundInOverworld().and(
                    BiomeSelectors.tag(BiomeTags.IS_SAVANNA)
                .or(BiomeSelectors.tag(BiomeTags.IS_BADLANDS))
                .or(BiomeSelectors.tag(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE))
                .or(BiomeSelector.DRY)
        ), genConf.peyote);
        registerUnTilledPatch("agave", PSBlocks.AGAVE_PLANT, AgavePlantBlock.AGE, UniformIntProvider.create(0, AgavePlantBlock.MAX_AGE), BiomeSelectors.foundInOverworld().and(
            BiomeSelectors.tag(BiomeTags.IS_BADLANDS)
            .or(BiomeSelectors.tag(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE))
            .or(BiomeSelector.DRY)
        ), genConf.peyote);

        MutableStructurePool.bootstrap();
    }

}
