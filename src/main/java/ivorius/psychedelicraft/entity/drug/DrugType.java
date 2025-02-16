/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.type.*;

/**
 * Created by lukas on 22.10.14.
 */
public record DrugType (Identifier id, Function<DrugType, Drug> constructor) {
    public static final Registry<DrugType> REGISTRY = FabricRegistryBuilder.createSimple(DrugType.class, Psychedelicraft.id("drugs")).buildAndRegister();
    public static final DrugType ALCOHOL = register("alcohol", type -> new AlcoholDrug(type, 1, 0.0002d));
    public static final DrugType CANNABIS = register("cannabis", type -> new CannabisDrug(1, 0.0002d));
    public static final DrugType BROWN_SHROOMS = register("brown_shrooms", type -> new BrownShroomsDrug(1, 0.0002d));
    public static final DrugType RED_SHROOMS = register("red_shrooms", type -> new RedShroomsDrug(1, 0.0002d));
    public static final DrugType TOBACCO = register("tobacco", type -> new TobaccoDrug(1, 0.003d));
    public static final DrugType COCAINE = register("coccaine", type -> new CocaineDrug(1, 0.0003d));
    public static final DrugType CAFFEINE = register("caffeine", type -> new CaffeineDrug(type, 1, 0.0002d, 1));
    public static final DrugType SUGAR = register("sugar", type -> new CaffeineDrug(type, 1, 0.0002d, 0));
    public static final DrugType BATH_SALTS = register("bath_salts", type -> new BathSaltsDrug(1, 0.00012d));
    public static final DrugType SLEEP_DEPRIVATION = register("sleep_deprivation", type -> new SleepDeprivationDrug());
    public static final DrugType LSD = register("lsd", type -> new LsdDrug(type, 1, 0.0003d, false));
    public static final DrugType ATROPINE = register("atropine", type -> new LsdDrug(type, 1, 0.0003d, true));
    public static final DrugType KAVA = register("kava", type -> new AlcoholDrug(type, 1, 0.0002d));
    public static final DrugType WARMTH = register("warmth", type -> new WarmthDrug(1, 0.004d));
    public static final DrugType PEYOTE = register("peyote", type -> new PeyoteDrug(1, 0.0002d));
    public static final DrugType ZERO = register("zero", type -> new SimpleDrug(type, 1, 0.0001d));
    public static final DrugType POWER = register("power", type -> new PowerDrug(0.95, 0.0001d));
    public static final DrugType HARMONIUM = register("harmonium", type -> new HarmoniumDrug(1, 0.0003d));

    public Drug create() {
        return constructor.apply(this);
    }

    static DrugType register(String name, Function<DrugType, Drug> constructor) {
        DrugType type = new DrugType(Psychedelicraft.id(name), constructor);
        PSSounds.register("drug." + name);
        return Registry.register(REGISTRY, type.id(), type);
    }
}
