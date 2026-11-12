<img width="1024" height="586" alt="minecraft_title" src="https://github.com/user-attachments/assets/7ea5d738-c5f0-4a5d-9e7a-2ade941b95dc" />
# Source Code of Villager Guard gun :3
Mod focused on Implementing a compatibility and functions of guards with Tacz Mod.


## What this adds:
* Compatibily with Guards mod with TacZ allowing guards to use any weapon and custom weapons from the mod.
* A new Inventory Slot for Ammo Boxes, Guards stores and uses their ammo there.
* A fully Configurable mod (still not possible to configure ingame yet).
* Guards spawning naturally with guns.
* Guards ammo refill in nearby Ammo Storages blocks.
* and maybe more functions on the future.

## Config:
* **WEAPON_WEIGHTS**: Weighted pool system used for choose the weapons which the guards spawns with. Entries works with '<modid:gun_id>=<weight>' example: "tacz:ak47=50", "tacz:m4a1=30".
* **AMMO_MODE**: Behavior of the ammo in guards. INFINITEAMMO the guard mag never runs out. INFINITEMAGS the mag run out but they are infinite. NORMAL normal behavior they use the ammo they spawn with and search for more in storages when low, default.
* **AMMO_FILL_PERCENT**: How % full is the ammo box that guards spawn with.
* **AMMO_BOX_LEVEL**: The Kind of ammo box the guards spawn with 0= iron, 1= gold, 2= diamond.
* **REFILL_AMMO**: true by default controls if guards refill their ammo on storages. Data: if you give them the wrong ammo type they will exchange their ammo with one they can use in the ammo storage.
* **REFILL_THRESHOLD_PERCENT**: % of how fill is they ammo box to execute the ammo refilling.
* **REFILL_AT_NIGHT**: true by default, this execute the ammo refilling during night if before mentioned conditions are met.
* **REFILL_COOLDOWN_SECONDS**: Cooldown in seconds if ammo refills fails to retry again the ammo searching in nearby storages.
* **DEFAULT_ACCURACY_MULTIPLIER**: Global gun Inaccuracy multiplier 1.0 = normal aim. Above 1.0 worse aim. Under 1.0 better aim.
* **ACCURACY_OVERRIDES**: Gun Inaccuracy multiplier per gun type: Known TaCZ types: rifle, smg, pistol, sniper, shotgun, mg, rpg. follows same logic as global aim example: "sniper=0.4"(better aim), "pistol=2.0"(worse aim)



-# The reason i made this mod is bc pillager gun mod port didn't allowed guard to use guns, then I remembered, hey i can Code in java then made this mod due that XD

### Credits to TacZ dev team for making their wonderful mod, Guards devs for keeping that mod and more important to the creator of Pillager Gun mod, the idea of this mod born thanks to that mod.
