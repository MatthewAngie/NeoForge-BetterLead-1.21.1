package com.mango.betterlead.item;


import com.mango.betterlead.BetterLead;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BetterLead.MODID);

    public static final DeferredItem<Item> CREATURECATCHER = ITEMS.register("creaturecatcher",
            () -> new CreatureCatcherItem(new Item.Properties().durability(16).stacksTo(1)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }



}
