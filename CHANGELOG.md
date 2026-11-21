# Changelog

---

## 1.1.1

- Fixed left-handed guards not showing their gun.
- Fixed guards keeping a finite ammo box after switching to `INFINITEMAGS`.
- Switching ammo modes no longer creates free ammo. A guard's finite box is kept while in `INFINITEMAGS` and restored when switching back.

---

## 1.1.0

- Added an optional Cloth Config in-game settings screen. Without Cloth Config the mod still works and shows a notice.
- Added an icon picker for the weapon pool: select a TaCZ gun, enter a weight, and add it to the pool.
- Added an in-game accuracy editor: type cells plus per-gun icon pickers.
- Added the DMR (marksman rifle) gun-type to accuracy type with a default override.
- The config is now a common config (`guardsgun-common.toml`) so it can be edited in-game from any screen. Existing `guardsgun-server.toml` values should be copied over once.
---

## 1.0.2

- Added the `gunSpawnChance` config option (0-100, default `100`) controlling the chance that a guard spawns with a gun. It is rolled once per guard, so it does not affect guards that already spawned/had custom weapons. - 300anthem suggestion
- Updated the default gun pool: AK-47, SVD, RPG-7, Type 81, M1897, M1911, and Desert Eagle.
- Fixed invalid list entries being silently dropped from the generated config. `weaponWeights` and `accuracyOverrides` entries must use `=`, for example `tacz:ak47=30` and `sniper=0.4`.
- Added per-gun accuracy overrides: `accuracyOverrides` now accepts `<modid:gun_id>=<multiplier>` (e.g. `tacz:type_81=0.5`), which takes priority over gun-type entries.
- NeoForge builds now produce `guardsgun-neoforge-<version>.jar`.
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
