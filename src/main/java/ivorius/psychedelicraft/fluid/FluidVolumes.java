/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

/**
 * Created by lukas on 22.10.14.
 */
public interface FluidVolumes {
    int BUCKET = 1_000;

    int CAULDRON = BUCKET;
    int GLASS_BOTTLE = CAULDRON / 8;
    int BOWL = BUCKET / 20;

    int MUG = BUCKET / 2;
    int CUP = BUCKET / 4;
    int CHALLICE = BUCKET / 5;
    int SHOT = BUCKET / 25;
    int BOTTLE = BUCKET * 2;
    int SYRINGE = BUCKET / 100;

    int BARREL = BUCKET * 16;
    int VAT = BUCKET * 32;
}
