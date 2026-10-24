package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.DuelConfirmMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import com.meowduels.util.KitCatalog;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class KitPickMenu extends Menu {
    private static final String MENU = "kitpick";
    private static final int[] KIT_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private final MeowDuels plugin;
    private final DuelConfirmMenu confirm;

    public KitPickMenu(MeowDuels plugin, DuelConfirmMenu confirm) {
        this.plugin = plugin;
        this.confirm = confirm;
    }

    @Override
    public void build() {
        GuiConfigManager gui = this.plugin.gui();
        int rows = gui.rows(MENU, 4);
        this.createRaw(rows, gui.titleOf(MENU, "plain", "<bold><gradient:#FF465E:#FFAA55>ᴅᴜᴇʟ ʀᴇQᴜᴇꜱᴛ</gradient></bold>", null));
        gui.frame(MENU, this.inventory, rows);
        List<KitCatalog.Entry> kits = KitCatalog.available(this.plugin);
        for (int i = 0; i < KIT_SLOTS.length && i < kits.size() && KIT_SLOTS[i] < this.inventory.getSize(); i++) {
            ItemStack icon = this.kitIcon(gui, kits.get(i));
            if (icon != null) {
                this.inventory.setItem(KIT_SLOTS[i], icon);
            }
        }
    }

    private ItemStack kitIcon(GuiConfigManager gui, KitCatalog.Entry entry) {
        Kit kit = this.plugin.getKitManager().get(entry.kit());
        if (kit == null) {
            return null;
        }
        HashMap<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("kit", entry.label());
        String[] gradient = kitGradient(kit.getName());
        // The kit's own name from kits.yml when it has one, so a kit reads the
        // same here as in /queue, chat and the scoreboard.
        String title = kit.withIcon(kit.getDisplayName() != null && !kit.getDisplayName().isBlank()
            ? kit.getDisplayName()
            : "<bold><gradient:" + gradient[0] + ":" + gradient[1] + ">" + entry.label().toUpperCase(Locale.ROOT) + "</gradient></bold>");
        Items icon = gui.item(MENU, "kit", placeholders, gui.material(MENU, "kit", kit.getIcon()), title, new String[0])
            .hideTooltip().tag(this.plugin.keyKit(), kit.getName());
        String configuredName = gui.name(MENU, "kit", placeholders);
        if (configuredName != null && configuredName.trim().equalsIgnoreCase("&e" + kit.getName())) {
            icon.miniName(title);
        } else if (configuredName == null || configuredName.contains("{kit}")) {
            icon.miniName(title);
        }
        return icon.build();
    }

    private static String[] kitGradient(String kitName) {
        String name = kitName.toLowerCase(Locale.ROOT);
        if (name.contains("sword")) return new String[]{"#62F5F5", "#15A8E8"};
        if (name.contains("mace")) return new String[]{"#FFF36A", "#F39A22"};
        if (name.contains("crystal")) return new String[]{"#FF82E8", "#9D55FF"};
        if (name.contains("axe")) return new String[]{"#FF9A62", "#E83D4F"};
        return new String[]{"#72D8FF", "#B35CFF"};
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        String kitName = Items.readTag(event.getCurrentItem(), this.plugin.keyKit());
        if (kitName != null && this.plugin.getKitManager().exists(kitName)) {
            this.confirm.setSelectedKit(kitName);
            this.confirm.reopen(player);
        }
    }
}
