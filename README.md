# StarDuels

StarDuels is a Paper plugin for player duels, parties, queues, arenas, and events. It targets Paper 1.21.11 and Java 21.

## Build

Run `mvn package` from this directory. The plugin JAR is written to `target/StarDuels-5.1.9.jar`.

## Install

Copy the JAR into the Paper server's `plugins` directory and restart the server. On first start, StarDuels creates its configuration files in `plugins/StarDuels/`.

## Configuration and reload

Edit the YAML files in `plugins/StarDuels/`, then run `/starduels reload` (permission: `meowduels.admin`) to reload `config.yml`, `messages.yml`, `titles.yml`, kits, categories, queue categories, GUI layouts, and menu configuration. `messages.yml` contains chat and match-result text; `titles.yml` contains match-found, countdown, fight, victory/defeat, party, and event titles. Existing title customizations in `messages.yml` are migrated to `titles.yml` the first time it is created. Arena, match, and player data are saved separately.

## Lobby and match behavior

The lobby wind charge is placed in the offhand, protected from dropping, swapping, and inventory removal, and refilled after use. The tagged lobby elytra does not take durability damage. Water and lava are allowed to flow during active duel and party matches, and their block changes are tracked for arena restoration. Falling, downward-aimed wind-charge throws reset downward velocity in Mace, ModernMace, and SpearMace kits.

The command also accepts `/sd reload`, `/md reload`, and `/meowduels reload`.
