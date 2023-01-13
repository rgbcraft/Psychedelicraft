/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.items.PSItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.*;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSTradeOffers {
    RegistryKey<PointOfInterestType> DRUG_DEALER_POI = poi("drug_dealer");
    VillagerProfession DRUG_DEALER_PROFESSION = register("drug_dealer", type -> type.matchesKey(DRUG_DEALER_POI), SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION);

    static void bootstrap() {
        TradeOfferHelper.registerVillagerOffers(DRUG_DEALER_PROFESSION, 0, factories -> {
            factories.add(buy(Items.EMERALD, 1, PSItems.CANNABIS_LEAF, 1, 9, 1, 0.7f));
            factories.add(buy(Items.EMERALD, 1, PSItems.CANNABIS_SEEDS, 5, 12, 2, 0.5f));
            factories.add(buy(Items.EMERALD, 1, PSItems.DRIED_CANNABIS_BUDS, 2, 8, 2, 0.9f));
            factories.add(buy(Items.EMERALD, 1, PSItems.DRIED_CANNABIS_LEAF, 2, 5, 3, 0.8f));
            factories.add(buy(Items.EMERALD, 1, PSItems.HASH_MUFFIN, 2, 3, 1, 0.7f));

            factories.add(buy(Items.EMERALD, 2, PSItems.BROWN_MAGIC_MUSHROOMS, 8, 3, 3, 0.5f));
            factories.add(buy(Items.EMERALD, 2, PSItems.RED_MAGIC_MUSHROOMS, 8, 3, 3, 0.5f));

            factories.add(buy(Items.EMERALD, 1, PSItems.COCA_LEAVES, 4, 4, 1, 0.5f));
            factories.add(buy(Items.EMERALD, 2, PSItems.COCA_SEEDS, 4, 4, 1, 0.5f));
            factories.add(buy(Items.EMERALD, 2, PSItems.DRIED_COCA_LEAVES, 20, 3, 2, 0.5f));

            factories.add(buy(Items.EMERALD, 2, PSItems.SYRINGE, 4, 3, 1, 0.5f));

            factories.add(buy(Items.EMERALD, 3, PSItems.DRIED_PEYOTE, 10, 2, 2, 0.5f));
            factories.add(buy(Items.EMERALD, 1, PSItems.PEYOTE_JOINT, 3, 2, 3, 0.5f));
            factories.add(buy(Items.EMERALD, 1, PSItems.PEYOTE, 5, 4, 4, 0.5f));
        });

        PointOfInterestTypes.register(Registries.POINT_OF_INTEREST_TYPE, DRUG_DEALER_POI, Stream.concat(
                        PSBlocks.DRYING_TABLE.getStateManager().getStates().stream(),
                        PSBlocks.IRON_DRYING_TABLE.getStateManager().getStates().stream()
        ).collect(Collectors.toUnmodifiableSet()), 1, 1);

        if (Psychedelicraft.getConfig().balancing.worldGeneration.farmerDrugDeals) {
            TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 0, factories -> {
                factories.add(buy(Items.EMERALD, 2, PSItems.WINE_GRAPES, 3, 8, 1, 0.5F));
                factories.add(buy(Items.EMERALD, 1, PSItems.HOP_CONES, 1, 4, 1, 0.6F));
                factories.add(buy(Items.EMERALD, 1, PSItems.HOP_SEEDS, 1, 4, 1, 0.4F));
                factories.add(buy(Items.EMERALD, 1, PSItems.WOODEN_MUG, 1, 4, 1, 0.5F));
                factories.add(buy(Items.EMERALD, 4, PSItems.DRIED_TOBACCO, 1, 4, 1, 0.3F));
                factories.add(buy(Items.EMERALD, 2, PSItems.CIGARETTE, 1, 4, 1, 0.8F));
                factories.add(buy(Items.EMERALD, 2, PSItems.CIGAR, 1, 4, 1, 0.8F));
                factories.add(buy(Items.EMERALD, 2, PSItems.TOBACCO_SEEDS, 1, 4, 1, 0.3F));
                factories.add(buy(Items.EMERALD, 1, PSItems.COFFEE_BEANS, 1, 4, 1, 0.8F));
                factories.add(buy(Items.EMERALD, 1, PSItems.COFFEA_CHERRIES, 1, 4, 1, 0.6F));
            });
        }
    }

    private static TradeOffers.Factory buy(Item item, int count, Item returnItem, int returnCount, int maxUses, int experience, float priceChange) {
        return (e, rng) -> new TradeOffer(new ItemStack(item, count), new ItemStack(returnItem, returnCount), maxUses, experience, priceChange);
    }

    private static RegistryKey<PointOfInterestType> poi(String id) {
        return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, Psychedelicraft.id(id));
    }

    private static VillagerProfession register(String id, Predicate<RegistryEntry<PointOfInterestType>> workstation, @Nullable SoundEvent workSound) {
        return register(id, workstation, workstation, ImmutableSet.of(), ImmutableSet.of(), workSound);
    }

    private static VillagerProfession register(String id, Predicate<RegistryEntry<PointOfInterestType>> heldWorkstation, Predicate<RegistryEntry<PointOfInterestType>> acquirableWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        return Registry.register(Registries.VILLAGER_PROFESSION, Psychedelicraft.id(id), new VillagerProfession("psychedelicraft:" + id, heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound));
    }
}
