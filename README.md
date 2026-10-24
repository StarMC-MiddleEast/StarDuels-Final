# StarDuels

StarDuels is a Paper plugin for player duels, parties, queues, arenas, and events. It targets Paper 1.21.11 and Java 21.

## Build

Run `mvn package` from this directory. The plugin JAR is written to `target/StarDuels-5.1.9.jar`.

## Install

Copy the JAR into the Paper server's `plugins` directory and restart the server. On first start, StarDuels creates its configuration files in `plugins/StarDuels/`.

## Configuration and reload

Edit the YAML files in `plugins/StarDuels/`, then run `/starduels reload` (permission: `meowduels.admin`) to reload `config.yml`, messages, kits, categories, queue categories, GUI layouts, and menu configuration. Arena, match, and player data are saved separately.

The command also accepts `/sd reload`, `/md reload`, and `/meowduels reload`.
