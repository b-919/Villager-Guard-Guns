# Changelog

---

## 1.0.2 (Forge 1.20.1)

- Added the `gunSpawnChance` config option (0-100, default `100`) controlling the chance that a guard spawns with a gun. It is rolled once per guard, so it does not affect guards that already spawned/had custom weapons.
- Fixed a server crash when opening a guard's inventory.
- Forge builds now produce `guardsgun-forge-<version>.jar`.

---

## 1.0.1 (Forge 1.20.1)

- Forge 1.20.1 port of GuardsGun 1.0.1.
- Same features and fixes as the 1.21.1 NeoForge build.

---

## 1.0.1

- Fixed guards in already-generated villages occasionally spawning without guns.
- Guard arming now retries and verifies the equipped gun after spawn.
- Creative ammo boxes are no longer obtainable from guards.
- Added the `FRIENDLY_FIRE` config option, using Guard Villagers' protected-mob list.
- The config is now a synced server config.
---
## 1.0.0

- Initial release
