package com.meowduels.model;

import com.meowduels.model.StartEffect;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class Kit {
    private final String name;
    private ItemStack[] contents;
    private ItemStack[] armor;
    private ItemStack offhand;
    private Material icon = Material.IRON_SWORD;
    private String displayName;
    private String prefix = "";
    private String nameIcon;
    private boolean guiVisible = true;
    private final Set<StartEffect> startEffects = EnumSet.noneOf(StartEffect.class);

    public Kit(String string) {
        this.name = string;
    }

    public Set<StartEffect> getStartEffects() {
        return this.startEffects;
    }

    public boolean hasStartEffect(StartEffect startEffect) {
        return this.startEffects.contains((Object)startEffect);
    }

    public void toggleStartEffect(StartEffect startEffect) {
        if (!this.startEffects.remove((Object)startEffect)) {
            this.startEffects.add(startEffect);
        }
    }

    public String getName() {
        return this.name;
    }

    public Material getIcon() {
        return this.icon;
    }

    public void setIcon(Material material) {
        this.icon = material;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String string) {
        this.displayName = string;
    }

    public String getPrefix() { return this.prefix == null ? "" : this.prefix; }

    /** kits.yml name-icon: the icon shown before this kit's name; null = from the menu icon. */
    public String getNameIcon() { return this.nameIcon; }

    public void setNameIcon(String nameIcon) { this.nameIcon = nameIcon == null || nameIcon.isBlank() ? null : nameIcon.trim(); }

    /** The given label with this kit's icon in front, per kit-names in config.yml. */
    public String withIcon(String label) { return com.meowduels.util.KitIcons.decorate(this, label); }

    /** The queue name with this kit's icon in front. */
    public String getQueueDisplayNameWithIcon() { return this.withIcon(this.getQueueDisplayName()); }

    public Component getPrefixComponent() {
        return MiniMessage.miniMessage().deserialize(com.meowduels.util.MiniMessageUtil.toMiniMessage(this.getPrefix()));
    }

    /** Prefix plus configured queue name, suppressing a prefix already present in the name. */
    private transient String queueNameKey;
    private transient String queueNameValue;

    /**
     * Memoized: the queue menus refresh every second for every viewer, and
     * this parses MiniMessage four times per kit per call. The result only
     * changes when the name or prefix does.
     */
    public String getQueueDisplayName() {
        String key = this.displayName + "\u0000" + this.getPrefix() + "\u0000" + this.name;
        if (key.equals(this.queueNameKey)) {
            return this.queueNameValue;
        }
        String value = this.computeQueueDisplayName();
        this.queueNameValue = value;
        this.queueNameKey = key;
        return value;
    }

    private String computeQueueDisplayName() {
        String name = this.displayName;
        if (name == null || name.isBlank()) name = "<yellow>" + this.name.replace('_', ' ') + "</yellow>";
        String prefix = this.getPrefix();
        if (prefix.isBlank()) return com.meowduels.util.MiniMessageUtil.toMiniMessage(name);
        String prefixText = PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(com.meowduels.util.MiniMessageUtil.toMiniMessage(prefix))).trim();
        String nameText = PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(com.meowduels.util.MiniMessageUtil.toMiniMessage(name)));
        if (!prefixText.isEmpty() && nameText.toLowerCase(java.util.Locale.ROOT).contains(prefixText.toLowerCase(java.util.Locale.ROOT))) {
            return com.meowduels.util.MiniMessageUtil.toMiniMessage(name);
        }
        return com.meowduels.util.MiniMessageUtil.toMiniMessage(prefix) + com.meowduels.util.MiniMessageUtil.toMiniMessage(name);
    }

    public void setPrefix(String prefix) { this.prefix = prefix == null || prefix.equalsIgnoreCase("clear") ? "" : prefix; }

    public boolean isGuiVisible() { return this.guiVisible; }

    public void setGuiVisible(boolean guiVisible) { this.guiVisible = guiVisible; }

    public void captureFrom(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        this.contents = Kit.cloneAll(playerInventory.getStorageContents());
        this.armor = Kit.cloneAll(playerInventory.getArmorContents());
        this.offhand = playerInventory.getItemInOffHand() == null ? null : playerInventory.getItemInOffHand().clone();
        ItemStack itemStack = playerInventory.getItemInMainHand();
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            this.icon = itemStack.getType();
        }
    }

    public void applyTo(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.clear();
        if (this.contents != null) {
            playerInventory.setStorageContents(Kit.cloneAll(this.contents));
        }
        if (this.armor != null) {
            playerInventory.setArmorContents(Kit.cloneAll(this.armor));
        }
        playerInventory.setItemInOffHand(this.offhand == null ? null : this.offhand.clone());
        this.selectMainHand(player);
        player.updateInventory();
    }

    public void selectMainHand(Player player) {
        if (player == null) {
            return;
        }
        PlayerInventory playerInventory = player.getInventory();
        int n = -1;
        int n2 = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = playerInventory.getItem(i);
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (n < 0) {
                n = i;
            }
            if (this.icon == null || itemStack.getType() != this.icon) continue;
            n2 = i;
            break;
        }
        if (n2 >= 0) {
            playerInventory.setHeldItemSlot(n2);
        } else if (n >= 0) {
            playerInventory.setHeldItemSlot(n);
        }
    }

    public void applyStartEffects(Player player) {
        if (player == null) {
            return;
        }
        for (StartEffect startEffect : this.startEffects) {
            try {
                player.addPotionEffect(startEffect.toPotionEffect());
            }
            catch (Throwable throwable) {}
        }
    }

    public void applyOffhand(Player player) {
        player.getInventory().setItemInOffHand(this.offhand == null ? null : this.offhand.clone());
        player.updateInventory();
    }

    public ItemStack[] getContents() {
        return this.contents == null ? new ItemStack[36] : Kit.cloneAll(this.contents);
    }

    public ItemStack[] getArmor() {
        return this.armor == null ? new ItemStack[4] : Kit.cloneAll(this.armor);
    }

    public ItemStack getOffhand() {
        return this.offhand == null ? null : this.offhand.clone();
    }

    public void setContents(ItemStack[] itemStackArray) {
        this.contents = Kit.cloneAll(itemStackArray);
    }

    public void setArmor(ItemStack[] itemStackArray) {
        this.armor = Kit.cloneAll(itemStackArray);
    }

    public void setOffhand(ItemStack itemStack) {
        this.offhand = itemStack == null ? null : itemStack.clone();
    }

    public void clearItems() {
        this.contents = null;
        this.armor = null;
        this.offhand = null;
    }

    private static ItemStack[] cloneAll(ItemStack[] itemStackArray) {
        if (itemStackArray == null) {
            return null;
        }
        ItemStack[] itemStackArray2 = new ItemStack[itemStackArray.length];
        for (int i = 0; i < itemStackArray.length; ++i) {
            itemStackArray2[i] = itemStackArray[i] == null ? null : itemStackArray[i].clone();
        }
        return itemStackArray2;
    }

    public void save(ConfigurationSection configurationSection) {
        configurationSection.set("icon", (Object)this.icon.name());
        configurationSection.set("display-name", (Object)this.displayName);
        configurationSection.set("prefix", this.prefix);
        configurationSection.set("name-icon", this.nameIcon);
        configurationSection.set("gui-visible", this.guiVisible);
        ArrayList<String> arrayList = new ArrayList<String>();
        for (StartEffect startEffect : this.startEffects) {
            arrayList.add(startEffect.name());
        }
        configurationSection.set("start-effects", arrayList);
        configurationSection.set("contents", (Object)this.contents);
        configurationSection.set("armor", (Object)this.armor);
        configurationSection.set("offhand", (Object)this.offhand);
    }

    public static Kit load(String string, ConfigurationSection configurationSection) {
        List list;
        Kit kit = new Kit(string);
        String string2 = configurationSection.getString("icon", "IRON_SWORD");
        Material material = Material.matchMaterial((String)string2);
        kit.icon = material == null ? Material.IRON_SWORD : material;
        kit.displayName = configurationSection.getString("display-name");
        kit.prefix = configurationSection.getString("prefix", "");
        if (kit.prefix == null) kit.prefix = "";
        kit.setNameIcon(configurationSection.getString("name-icon"));
        kit.guiVisible = configurationSection.getBoolean("gui-visible", false);
        for (String string3 : configurationSection.getStringList("start-effects")) {
            try {
                kit.startEffects.add(StartEffect.valueOf(string3));
            }
            catch (IllegalArgumentException illegalArgumentException) {}
        }
        List list2 = configurationSection.getList("contents");
        if (list2 != null) {
            kit.contents = ((List<ItemStack>) list2).toArray(new ItemStack[0]);
        }
        if ((list = configurationSection.getList("armor")) != null) {
            kit.armor = ((List<ItemStack>) list).toArray(new ItemStack[0]);
        }
        kit.offhand = configurationSection.getItemStack("offhand");
        return kit;
    }
}

