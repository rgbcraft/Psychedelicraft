/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.command.*;
import ivorius.psychedelicraft.config.JsonConfig;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entity.PSEntities;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.item.PSItemGroups;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.network.Channel;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import ivorius.psychedelicraft.world.gen.PSWorldGen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Psychedelicraft implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    @Deprecated
    public static final String TEXTURES_PATH = "textures/mod/";
    @Deprecated
    public static final String SHADERS_PATH = "shaders/";

    private static final Supplier<JsonConfig.Loader<PSConfig>> CONFIG_LOADER = JsonConfig.create("psychedelicraft.json", PSConfig::new);

    public static PSConfig getConfig() {
        return CONFIG_LOADER.get().getData();
    }

    public static Identifier id(String name) {
        return new Identifier("psychedelicraft", name);
    }

    @Override
    public void onInitialize() {
        PSBlocks.bootstrap();
        PSItems.bootstrap();
        PSTags.bootstrap();
        PSItemGroups.bootstrap();
        PSFluids.bootstrap();
        PSRecipes.bootstrap();
        PSEntities.bootstrap();
        PSWorldGen.bootstrap();
        PSCommands.bootstrap();
        PSSounds.bootstrap();
        PSScreenHandlers.bootstrap();
        Channel.bootstrap();
        PSCriteria.bootstrap();
    }
}