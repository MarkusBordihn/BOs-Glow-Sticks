# Changelog for Glow Sticks

All notable changes to this project will be documented in this file.

## Note

This change log includes the summarized changes.
For the full changelog, please go to the [GitHub History][history] instead.

### 7.4.0

- Fixed #7 by granting the crafting advancements only for actually crafted glow sticks.
- Fixed #8 by replacing the random despawn chance with an exact lifetime in seconds.
- Fixed glow sticks next to redstone components staying lit and immortal forever.
- Fixed placed glow sticks dropping nothing, floating in the air or being destroyed by throws.
- Fixed advancements and recipe book hints not unlocking for several glow stick actions.
- Changed `despawnTicks` to `glowStickLifetimeSeconds`, migrated on first start, default 36 minutes.
- Changed "Sharing is caring." from a challenge to a goal, so it no longer plays the challenge tune.
- Changed glow stick colors to runtime tinting of one shared texture set.
  Resource packs replacing single colored glow stick textures need to be updated.
- Added placing and picking up of normal glow sticks, both toggleable in the config.
- Added redstone recharging and evenly lit glow stick chains with a configurable size limit.
- Added the "Follow the light" advancement and an optional survival recipe for creative sticks.
- Added self-lit rendering, a flight animation and a particle trail for thrown glow sticks.

### 7.3.0

- Fixed #5 by adding vertical placeable creative glow sticks.
- Fixed #6 by adjusting the Japanese Name for "Glow Stick". Thanks a lot to `@looksky495`
  for the detailed explanation.
- Fixed temporary underwater light blocks to restore water correctly after expiring.
- Fixed waypoint particles for white glow sticks when no glow stick was held.
- Added config value validation for particle spawn rate and other glow stick settings.
- Improved tooltip texts and configuration handling for glow sticks.

### 7.2.0

- Refactored code for better readability and maintenance.
- Fixed #2 by adding distance requirement.
- Fixed onUseTick method to prevent crashes.
- Added japanese translation, thanks to `@PExPE3`.
- Added creative versions of the Glow Sticks which do not despawn.
- Added powered versions of the Glow Sticks which reactivate when powered by Redstone.
- Added additional 3 variants for normal and creative Glow Sticks.
- Removed `despawnEnabled` config entry and replaced it with `despawnTicks=0` configuration option.

### 7.1.0

- Added basic particle effects for the Glow Sticks.
- Added waypoint feature which shows a waypoint to the player when holding a Glow Stick and
  sneaking.
- Added a new configuration option.

### 7.0.0

- First release of reworked Glow Sticks mods for Fabric, Forge and NeoForge.
- General Code optimizations and improvements.

[history]: https://github.com/MarkusBordihn/BOs-Glow-Sticks/commits/26.2
