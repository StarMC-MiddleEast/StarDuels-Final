# StarDuels

StarDuels is a Paper plugin for player duels, parties, queues, arenas, and events. It targets Paper 1.21.11 and Java 21.

## Build

Run `mvn package` from this directory. The plugin JAR is written to `target/StarDuels-5.1.10.jar`.

## Install

Copy the JAR into the Paper server's `plugins` directory and restart the server. On first start, StarDuels creates its configuration files in `plugins/StarDuels/`.

Completed duels return each player to their assigned arena spawn: player 1 to `pos1`, player 2 to `pos2`. `/starduels setspawn` remains the plugin's general return point for flows that use it; duel endings use the arena positions.

## Configuration and reload

Edit the YAML files in `plugins/StarDuels/`, then run `/starduels reload` (permission: `meowduels.admin`) to reload `config.yml`, `messages.yml`, `titles.yml`, kits, categories, queue categories, GUI layouts, and menu configuration. `messages.yml` contains chat and match-result text; `titles.yml` contains match-found, countdown, fight, victory/defeat, party, and event titles. Existing title customizations in `messages.yml` are migrated to `titles.yml` the first time it is created. Arena, match, and player data are saved separately.

## Lobby and match behavior

The lobby wind charge is placed in the offhand, protected from dropping, swapping, and inventory removal, and refilled after use. The tagged lobby elytra does not take durability damage. Water and lava are allowed to flow during active duel and party matches, and their block changes are tracked for arena restoration. Falling, downward-aimed wind-charge throws reset downward velocity in Mace, ModernMace, and SpearMace kits. Queued matches use first-to-two rounds. Between rounds, players glide by velocity to their arena spawns with no particles and one launch sound; the landing fall is protected. Queued matches show a private totem-pop visual using the queued kit icon. Match losses play a server-wide sting; match endings return players to their configured destinations.

The command also accepts `/sd reload`, `/md reload`, and `/meowduels reload`.

## Buttons GUI

Player-facing menus use Paper's native dialog layout by default, including the Settings menu. The Settings menu no longer offers a switch between dialogs and chest GUIs. Admin and arena setup menus remain chest GUIs.

The per-player **Buttons GUI** setting controls other supported menus. Settings itself always opens as a Paper dialog on compatible clients. Client brand strings such as Fabric/Vulkan are not used to disable dialogs; when ViaVersion reports a client older than 1.21.6, the plugin falls back to an inventory menu.

## Party Hunger Games

Create a mode arena with `/createarena hungergames [name]`. Follow the chat prompts to set one shared player spawn, two arena corners, and the two corners for each of the two chest-generation regions. The arena baseline is saved for regeneration. Players can host with `/party hungergames`; the host must be a party leader, belong to a party, and have `hg.host.party` (or `starduels.hungergames.host`). The host GUI lets the leader select an available arena, preview and select an HG kit, and start the match for the party. Default kits and generated chest spacing are stored in `hungergames-kits.yml` and `config.yml`; arena loot regions are stored in `hungergames.yml`. Loot is filled one tick after generated double chests are placed. The post-match arena closing countdown has been removed.
