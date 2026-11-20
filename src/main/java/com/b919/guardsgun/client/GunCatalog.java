package com.b919.guardsgun.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects TaCZ guns for the config pickers.
 * In a world the common (server) index is available meanwhile on the main menu only the client index is loaded.
 */
public final class GunCatalog {
    private GunCatalog() {
    }

    public record GunInfo(ResourceLocation id, String type, ItemStack icon, Component name) {
    }

    public static List<GunInfo> collect() {
        List<GunInfo> guns = new ArrayList<>();
        var common = TimelessAPI.getAllCommonGunIndex();
        if (!common.isEmpty()) {
            for (var entry : common) {
                ResourceLocation id = entry.getKey();
                CommonGunIndex index = entry.getValue();
                ItemStack icon;
                try {
                    icon = GunItemBuilder.create().setId(id).build();
                } catch (Exception e) {
                    continue;
                }
                if (icon.isEmpty()) {
                    continue;
                }
                guns.add(new GunInfo(id, index.getType(), icon,
                        name(index.getPojo() == null ? null : index.getPojo().getName(), icon)));
            }
            return guns;
        }

        for (var entry : TimelessAPI.getAllClientGunIndex()) {
            ResourceLocation id = entry.getKey();
            ClientGunIndex index = entry.getValue();
            ItemStack icon = clientIcon(id, index.getItemType());
            if (icon.isEmpty()) {
                continue;
            }
            guns.add(new GunInfo(id, index.getType(), icon, name(index.getName(), icon)));
        }
        return guns;
    }

    private static ItemStack clientIcon(ResourceLocation id, String itemType) {
        var item = GunItemManager.getGunItemRegistryObject(itemType);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item.get());
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return ItemStack.EMPTY;
        }
        gun.setGunId(stack, id);
        return stack;
    }

    private static Component name(String translationKey, ItemStack icon) {
        if (translationKey == null || translationKey.isBlank()) {
            return icon.getHoverName();
        }
        return Component.translatable(translationKey);
    }
}
