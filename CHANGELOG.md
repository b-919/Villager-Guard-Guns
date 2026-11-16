# Changelog

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
