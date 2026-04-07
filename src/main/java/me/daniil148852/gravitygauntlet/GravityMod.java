package me.daniil148852.gravitygauntlet;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GravityMod implements ModInitializer {
	public static final String MOD_ID = "gravitygauntlet";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final GravityGauntletItem GRAVITY_GAUNTLET = new GravityGauntletItem(
		new Item.Settings().maxCount(1)
	);

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gravity_gauntlet"), GRAVITY_GAUNTLET);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(GRAVITY_GAUNTLET);
		});

		LOGGER.info("Gravity Gauntlet initialized!");
	}
}
