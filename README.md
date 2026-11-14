<img width="1024" height="586" alt="minecraft_title" src="https://github.com/user-attachments/assets/7ea5d738-c5f0-4a5d-9e7a-2ade941b95dc" />

# Villager Guard Guns :3

A NeoForge mod that gives Guard Villagers guards TaCZ guns, ammunition, and gun AI.

## What this adds

* Guards spawn with guns from a configurable weighted pool, including custom TaCZ gun packs.
* A dedicated ammo-box slot in the guard inventory.
* `NORMAL`, `INFINITEMAGS`, and `INFINITEAMMO` ammo modes.
* Guards refill and exchange ammo at nearby TaCZ ammo storages.
* Per-gun-type accuracy multipliers.
* Optional friendly-fire protection for guards, villagers, iron golems, allies, owners, and Hero of the Village players.
* Creative ammo boxes are internal to guards and cannot be obtained from them.

## Requirements

* Minecraft 1.21.1
* NeoForge 21.1.x
* [Timeless and Classics Zero (TaCZ)](https://modrinth.com/mod/timeless-and-classics-zero) 1.2.0+
* [Guard Villagers](https://modrinth.com/mod/guard-villagers) 2.4.12+

## Config

The config file is (`guardsgun-server.toml`)

| Key                         | Description                                                                                                                                                                   |
|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enableGuns`                | Master switch. `false` disables the mod's gun behavior.                                                                                                                       |
| `weaponWeights`             | Weighted gun pool, e.g. `"tacz:ak47=50"`. Chance = weight / total weight.                                                                                                     |
| `ammoMode`                  | `NORMAL` (finite reserve, default), `INFINITEMAGS` (infinite reserve), or `INFINITEAMMO` (magazine never empties).                                                            |
| `ammoFillPercent`           | How full the spawn ammo box is, 0–100.                                                                                                                                        |
| `ammoBoxLevel`              | Ammo box tier: 0 = iron, 1 = gold, 2 = diamond.                                                                                                                               |
| `refillAmmo`                | Whether guards refill/exchange ammo at storage blocks.                                                                                                                        |
| `refillThresholdPercent`    | Box fill percentage at which a guard starts looking for a storage.                                                                                                            |
| `refillAtNight`             | Whether refill/exchange is allowed at night. Default `false`.                                                                                                                 |
| `refillCooldownSeconds`     | Retry cooldown when no storage can serve the guard.                                                                                                                           |
| `defaultAccuracyMultiplier` | Global inaccuracy multiplier. >1 is worse aim, <1 is better.                                                                                                                  |
| `accuracyOverrides`         | Per-type inaccuracy, e.g. `"sniper=0.4"`, `"pistol=2.0"`.                                                                                                                     |
| `friendlyFire`              | Default `true`. Prevents guards from damaging Guard Villagers' protected mobs, team allies, their owner, and Hero of the Village players. Set `false` to allow friendly fire. |

The friendly-fire protected mobs come from Guard Villagers' `"Mobs that guards actively protect when they get hurt"` list. Add custom villagers or allies there if you use modded villages.


## Building

```powershell
.\gradlew.bat build
```

The jar is written to `build/libs/guardsgun-<version>.jar`.

## Credits

TaCZ dev team for TaCZ, the Guard Villagers devs, and the creator of Pillager's Gun for the original idea.
The reason i made this mod is bc pillager gun mod port didn't allowed guard to use guns, then I remembered, hey i can Code in java then made this mod due that XD

## License

All Rights Reserved. Copyright (c) 2026 B.Alex. See [LICENSE](LICENSE).
