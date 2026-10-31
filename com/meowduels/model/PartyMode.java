package com.meowduels.model;

public enum PartyMode {
    FFA("ᴘᴀʀᴛʏ ꜰꜰᴀ", "Everyone fights for themselves, last one standing wins.", true),
    SPLIT("ᴘᴀʀᴛʏ ꜱᴘʟɪᴛ", "Two teams, aqua against red.", true),
    DUELS("ᴘᴀʀᴛʏ ᴅᴜᴇʟꜱ", "Your party versus another party, team versus team.", true),
    HUNGERGAMES("HUNGER GAMES", "Last player alive wins.", true);

    private final String label;
    private final String description;
    private final boolean ready;

    PartyMode(String label, String description, boolean ready) {
        this.label = label;
        this.description = description;
        this.ready = ready;
    }

    public String getLabel() { return this.label; }
    public String getDescription() { return this.description; }
    public boolean isReady() { return this.ready; }
}
