package com.meowduels.util;

import com.meowduels.MeowDuels;
import com.meowduels.model.Party;
import com.meowduels.util.Items;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class SpawnItems {
    public static final String QUEUE = "spawn-queue";
    public static final String QUICK = "spawn-quick";
    public static final String LEAVEQUEUE = "spawn-leavequeue";
    public static final String KITEDITOR = "spawn-kiteditor";
    public static final String SETTINGS = "spawn-settings";
    public static final String REMATCH = "spawn-rematch";
    public static final String CREATEPARTY = "spawn-createparty";
    public static final String PARTYMATCH = "party-match";
    public static final String PARTYSPECTATE = "party-spectate";
    public static final String PARTYLEAVE = "party-leave";
    public static final String PARTYSETTINGS = "party-settings";
    public static final String PARTYINFO = "party-info";
    public static final String LOBBY_WIND_CHARGE = "lobby-wind-charge";
    public static final String LOBBY_ELYTRA = "lobby-elytra";
    private static final int LEAVE_SLOT = 5;
    private static final int REMATCH_SLOT = 2;

    private SpawnItems() {
    }

    public static void give(MeowDuels meowDuels, Player player) {
        if (player == null) {
            return;
        }
        if ((meowDuels.getDuelManager() != null && meowDuels.getDuelManager().isInDuel(player.getUniqueId()))
                || (meowDuels.getEventManager() != null && meowDuels.getEventManager().isInvolved(player.getUniqueId()))
                || (meowDuels.getPartyManager() != null && meowDuels.getPartyManager().inPartyMatch(player.getUniqueId()))
                || (meowDuels.getSpectateManager() != null && meowDuels.getSpectateManager().isSpectating(player.getUniqueId()))) {
            return;
        }
        boolean handWasEmpty = isEmpty(player.getInventory().getItemInMainHand());
        setIfEmpty(player, 3, SpawnItems.windCharge(meowDuels));
        ItemStack[] itemStackArray = player.getInventory().getArmorContents();
        if (itemStackArray != null && itemStackArray.length > 2 && isEmpty(itemStackArray[2])) {
            itemStackArray[2] = Items.of(Material.ELYTRA).miniName("<gradient:#FFE27A:#F2A900>\u029f\u1d0f\u0299\u0299\u028f \u1d07\u029f\u028f\u1d1b\u0280\u1d00</gradient>").hideTooltip().tag(meowDuels.keyButton(), LOBBY_ELYTRA).build();
            player.getInventory().setArmorContents(itemStackArray);
        }
        if (meowDuels.getPartyManager() != null && meowDuels.getPartyManager().inParty(player.getUniqueId())) {
            SpawnItems.giveParty(meowDuels, player);
            selectFirstItemWhenHandEmpty(player, handWasEmpty);
            player.updateInventory();
            return;
        }
        setIfEmpty(player, 0, Items.of(Material.DIAMOND_SWORD).miniName("<gradient:#FF6B6B:#A01028>\u2694 \u0280\u1d00\u0274\u1d0b\u1d07\u1d05 \u01eb\u1d1c\u1d07\u1d1c\u1d07</gradient>").hideTooltip().tag(meowDuels.keyButton(), QUEUE).build());
        setIfEmpty(player, 1, Items.of(Material.GOAT_HORN).miniName("<gold>\u1d18\u1d00\u0280\u1d1b\u028f</gold>").hideTooltip().tag(meowDuels.keyButton(), CREATEPARTY).build());
        setIfEmpty(player, 4, Items.of(Material.CHEST).miniName("<red>\u2739</red> <red>\u01eb\u1d1c\u026a\u1d04\u1d0b \u01eb\u1d1c\u1d07\u1d1c\u1d07</red> <white>(\u0280\u026a\u0262\u029c\u1d1b \u1d04\u029f\u026a\u1d04\u1d0b)</white>").rawLore("", "<white>\u0280\u026a\u0262\u029c\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d0a\u1d0f\u026a\u0274 \u1d00 \u01eb\u1d1c\u1d07\u1d1c\u1d07</white>").tag(meowDuels.keyButton(), QUICK).build());
        setIfEmpty(player, 7, Items.of(Material.BOOK).miniName("<gradient:#8AD9FF:#3F6BE0>\ud83d\udcd6 \u1d0b\u026a\u1d1b \u1d07\u1d05\u026a\u1d1b\u1d0f\u0280</gradient>").hideTooltip().tag(meowDuels.keyButton(), KITEDITOR).build());
        setIfEmpty(player, 8, Items.of(Material.GRINDSTONE).miniName("<gradient:#C8C8C8:#707070>\u2699 \u0455\u1d07\u1d1b\u1d1b\u026a\u0274\u0262\u0455</gradient>").hideTooltip().tag(meowDuels.keyButton(), SETTINGS).build());
        if (meowDuels.getQueueManager().isQueued(player.getUniqueId())) {
            setIfEmpty(player, 5, SpawnItems.leaveQueueItem(meowDuels));
        }
        selectFirstItemWhenHandEmpty(player, handWasEmpty);
        player.updateInventory();
    }

    private static void setIfEmpty(Player player, int slot, ItemStack item) {
        if (isEmpty(player.getInventory().getItem(slot))) {
            player.getInventory().setItem(slot, item);
        }
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private static void selectFirstItemWhenHandEmpty(Player player, boolean handWasEmpty) {
        if (!handWasEmpty) return;
        for (int slot = 0; slot < 9; slot++) {
            if (isEmpty(player.getInventory().getItem(slot))) continue;
            player.getInventory().setHeldItemSlot(slot);
            return;
        }
    }

    private static void giveParty(MeowDuels meowDuels, Player player) {
        Party party = meowDuels.getPartyManager().partyOf(player.getUniqueId());
        if (party == null) {
            return;
        }
        boolean bl = party.isLeader(player.getUniqueId());
        if (bl) {
            setIfEmpty(player, 0, Items.of(Material.DIAMOND_SWORD).miniName("<gradient:#FF8AD0:#B04BD6>\u2694 \u1d18\u1d00\u0280\u1d1b\u028f \u1d0d\u1d00\u1d1b\u1d04\u029c</gradient>").rawLore("", "<#8E959D>\u0280\u026a\u0262\u029c\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \ua731\u1d07\u1d1b \u1d1c\u1d18 \u1d1b\u029c\u1d07 \u1d0d\u1d00\u1d1b\u1d04\u029c", "<dark_gray>\u25b8 <#8E959D>\ua730\ua730\u1d00, \ua731\u1d18\u029f\u026a\u1d1b \u1d0f\u0280 \u1d05\u1d1c\u1d07\u029f\ua731").hideTooltip().tag(meowDuels.keyButton(), PARTYMATCH).build());
            setIfEmpty(player, 8, Items.of(Material.GRINDSTONE).miniName("<gradient:#C8C8C8:#707070>\u2699 \u1d18\u1d00\u0280\u1d1b\u028f \ua731\u1d07\u1d1b\u1d1b\u026a\u0274\u0262\ua731</gradient>").hideTooltip().tag(meowDuels.keyButton(), PARTYSETTINGS).build());
        } else {
            setIfEmpty(player, 0, Items.of(Material.PAPER).miniName("<gradient:#FF8AD0:#B04BD6>\u25c8 \u1d18\u1d00\u0280\u1d1b\u028f \u026a\u0274\ua730\u1d0f</gradient>").rawLore("", "<#8E959D>\u0280\u026a\u0262\u029c\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \ua731\u1d07\u1d07 \u1d21\u029c\u1d0f'\ua731 \u026a\u0274", "", "<#6B7079>\u0280\u1d1c\u0274 \u0299\u028f <#E6E8EB>" + SpawnItems.nameOf(party.getLeader())).hideTooltip().tag(meowDuels.keyButton(), PARTYINFO).build());
        }
        setIfEmpty(player, 1, Items.of(Material.ENDER_PEARL).miniName("<gradient:#8AD9FF:#3F6BE0>\u25c9 \u1d18\u1d00\u0280\u1d1b\u028f \ua731\u1d18\u1d07\u1d04\u1d1b\u1d00\u1d1b\u1d07</gradient>").rawLore("", "<#8E959D>\u1d21\u1d00\u1d1b\u1d04\u029c \u028f\u1d0f\u1d1c\u0280 \u1d18\u1d00\u0280\u1d1b\u028f'\ua731 \u1d0d\u1d00\u1d1b\u1d04\u029c", "<dark_gray>\u25b8 <#8E959D>/leave \u1d1b\u1d0f \ua731\u1d1b\u1d0f\u1d18").hideTooltip().tag(meowDuels.keyButton(), PARTYSPECTATE).build());
        setIfEmpty(player, 7, Items.of(Material.RED_DYE).miniName(bl ? "<gradient:#FF6B6B:#A01028>\u2716 \u1d05\u026a\ua731\u0299\u1d00\u0274\u1d05 \u1d18\u1d00\u0280\u1d1b\u028f</gradient>" : "<gradient:#FF6B6B:#A01028>\u2716 \u029f\u1d07\u1d00\u1d20\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f</gradient>").rawLore("", bl ? "<#8E959D>\u1d1b\u029c\u026a\ua731 \u1d07\u0274\u1d05\ua731 \u1d1b\u029c\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f \ua730\u1d0f\u0280 \u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07" : "<#8E959D>\u028f\u1d0f\u1d1c \u1d0b\u1d07\u1d07\u1d18 \u1d18\u029f\u1d00\u028f\u026a\u0274\u0262 \u1d00\u029f\u1d0f\u0274\u1d07", "", "<dark_gray>\u25b8 <#8E959D>\u0280\u026a\u0262\u029c\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d04\u1d0f\u0274\ua730\u026a\u0280\u1d0d").hideTooltip().tag(meowDuels.keyButton(), PARTYLEAVE).build());
        player.updateInventory();
    }

    private static String nameOf(UUID uUID) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            return player.getName();
        }
        String string = Bukkit.getOfflinePlayer((UUID)uUID).getName();
        return string == null ? "?" : string;
    }

    public static void addRematch(MeowDuels meowDuels, Player player, UUID uUID, String string) {
        if (player == null) {
            return;
        }
        ItemStack itemStack = Items.of(Material.PLAYER_HEAD).miniName("<gradient:#FF5C5C:#8B0000><bold>\u0280\u1d07\u1d0d\u1d00\u1d1b\u1d04\u029c \u2694\ufe0f</bold></gradient>").lore("&7\u0280\u026a\u0262\u029c\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d04\u029c\u1d00\u029f\u029f\u1d07\u0274\u0262\u1d07", "&f" + string + " &7\u1d00\u0262\u1d00\u026a\u0274.").hideTooltip().tag(meowDuels.keyButton(), REMATCH).build();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof SkullMeta) {
            try {
                ((SkullMeta)itemMeta).setOwningPlayer(Bukkit.getOfflinePlayer((UUID)uUID));
                itemStack.setItemMeta(itemMeta);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        setIfEmpty(player, 2, itemStack);
        player.updateInventory();
    }

    public static void setLeaveQueue(MeowDuels meowDuels, Player player, boolean bl) {
        if (player == null) {
            return;
        }
        setIfEmpty(player, 5, bl ? SpawnItems.leaveQueueItem(meowDuels) : null);
        player.updateInventory();
    }

    private static ItemStack leaveQueueItem(MeowDuels meowDuels) {
        return Items.of(Material.RED_DYE).miniName("<gradient:#FF8A8A:#C0392B>\u2716 \u029f\u1d07\u1d00\u1d20\u1d07 \u01eb\u1d1c\u1d07\u1d1c\u1d07</gradient>").hideTooltip().tag(meowDuels.keyButton(), LEAVEQUEUE).build();
    }

    public static ItemStack windCharge(MeowDuels meowDuels) {
        return Items.of(Material.WIND_CHARGE, 64).miniName("<gradient:#FFE27A:#F2A900>\u029f\u1d0f\u0299\u0299\u028f \u1d21\u026a\u0274\u1d05 \u1d04\u029c\u1d00\u0280\u0262\u1d07</gradient>").rawLore("", "<gray>Infinite lobby movement item</gray>").hideTooltip().tag(meowDuels.keyButton(), LOBBY_WIND_CHARGE).build();
    }
}

