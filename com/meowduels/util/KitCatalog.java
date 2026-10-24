package com.meowduels.util;

import com.meowduels.MeowDuels;
import java.util.ArrayList;
import java.util.List;

/** The curated kit list used by the main duel and queue pickers. */
public final class KitCatalog {
    private static final Entry[] ENTRIES = new Entry[]{
        new Entry("Sword", "Sword"),
        new Entry("UHC", "UHC"),
        new Entry("ModernSMP", "SMP"),
        new Entry("CrystalIO", "Crystal"),
        new Entry("Mace", "Mace"),
        new Entry("SpearMace", "Spear Mace"),
        new Entry("NetheriteOP", "NetOP"),
        new Entry("DiaSMP", "DiaSMP"),
        new Entry("PotionIO", "DiaPot"),
        new Entry("ModernAxe", "Axe"),
        new Entry("Speed", "Speed"),
        new Entry("MoneySMP", "Money SMP"),
        new Entry("CartLT", "Cart LT"),
        new Entry("CartHT", "Cart HT")
    };

    private KitCatalog() {
    }

    public static List<Entry> available(MeowDuels plugin) {
        ArrayList<Entry> result = new ArrayList<Entry>(ENTRIES.length);
        java.util.HashSet<String> included = new java.util.HashSet<String>();
        for (Entry entry : ENTRIES) {
            com.meowduels.model.Kit kit = plugin.getKitManager().get(entry.kit);
            if (kit != null && kit.isGuiVisible()) {
                result.add(entry);
                included.add(entry.kit.toLowerCase(java.util.Locale.ROOT));
            }
        }
        for (com.meowduels.model.Kit kit : plugin.getKitManager().all()) {
            if (!kit.isGuiVisible() || included.contains(kit.getName().toLowerCase(java.util.Locale.ROOT))) continue;
            String label = kit.getDisplayName() == null || kit.getDisplayName().isEmpty() ? kit.getName() : kit.getDisplayName();
            result.add(new Entry(kit.getName(), label));
        }
        return result;
    }

    public static boolean defaultVisible(String name) {
        for (Entry entry : ENTRIES) if (entry.kit.equalsIgnoreCase(name)) return true;
        return false;
    }

    public static List<com.meowduels.model.Kit> visibleKits(MeowDuels plugin) {
        ArrayList<com.meowduels.model.Kit> kits = new ArrayList<com.meowduels.model.Kit>();
        for (Entry entry : available(plugin)) {
            com.meowduels.model.Kit kit = plugin.getKitManager().get(entry.kit);
            if (kit != null) kits.add(kit);
        }
        return kits;
    }

    public static final class Entry {
        private final String kit;
        private final String label;

        private Entry(String kit, String label) {
            this.kit = kit;
            this.label = label;
        }

        public String kit() {
            return this.kit;
        }

        public String label() {
            return this.label;
        }
    }
}
