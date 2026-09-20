# NeoForge 1.21.11 build notes

This branch (`neoforge/1.21.11`) is andyspancakes' community NeoForge port of
enjarai's [Do a Barrel Roll](https://modrinth.com/mod/do-a-barrel-roll), updated
to build against Minecraft 1.21.11 / NeoForge 21.11.x.

Original mod: enjarai (GPLv3) — https://github.com/enjarai/do-a-barrel-roll
NeoForge port base: andyspancakes — https://github.com/andyspancakes/do-a-barrel-roll-neoforge-port

## What changed to get 1.21.11 building

- Wired the existing `versions/1.21.11-neoforge` Stonecutter entry into
  `settings.gradle.kts` (it existed but was never registered).
- Fixed `deps.fml` (NeoForge version) — the configured `21.11.2` doesn't exist;
  NeoForge betas for this cycle are also missing installer data Loom needs, so
  this now builds against stable `21.11.45`. The mod's `neoforge.mods.toml` has
  no NeoForge version range, so this doesn't raise the runtime floor.
- Added the missing `maven.isxander.dev` repository (YACL dependency).
- Set `mod.mc_version=1.21.11` in the version's `gradle.properties` (was
  silently falling back to the root default of `1.21.4`).
- Bumped Architectury Loom to `1.17.493` and Gradle to `9.5.0` (required by
  that Loom version).
- Bumped the `me.fallenbreath.yamlang` Gradle plugin to `1.5.0` (Gradle 9
  task-validation compatibility).
- Removed the stale `extensions:*` Gradle subproject includes (directories no
  longer exist).
- Dropped the `1.21.4-neoforge` Stonecutter entry — it shared the same source
  tree with no version-conditional guards, so it can no longer coexist with
  1.21.11 on this branch.
- Synced several source files with the current 1.21.11 Fabric API (Mojang's
  render pipeline rewrite, `KeyBinding.Category`, `Entity.getWorld()` →
  `getEntityWorld()`, `Entity.getPos()` → `getEntityPos()`, the new
  `PermissionPredicate`-based op check, `ClientPacketDistributor` split from
  `PacketDistributor`, `FMLLoader.getDist()` → `FMLLoader.getCurrent().getDist()`,
  `World.addParticle` → `addImportantParticleClient`, `DrawContext.drawTexture`
  now taking a `RenderPipeline`).
- Registered the crosshair's inverted-color `RenderPipeline` via NeoForge's
  `RegisterRenderPipelinesEvent` instead of the Fabric-only direct
  `RenderPipelines.register()` call.

## Requirements

- Minecraft 1.21.11
- NeoForge 21.11.x (built/tested against 21.11.45, should work on the
  21.11.24-beta+ range too since there's no explicit version floor)
- [YACL](https://modrinth.com/mod/yacl) `3.8.1+1.21.11-neoforge` — optional,
  only needed for the in-game config screen

Server-side only feature (visual roll sync) works with just this mod on the
server; clients don't need it installed to join, but need it to see the
flight camera effects themselves.
