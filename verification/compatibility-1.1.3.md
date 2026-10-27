# Helper 1.1.3 — TerraFirmaGreg Modern 0.13.17

## Exact target

- Instance metadata: managed pack 0.13.17, CurseForge file ID 9340476.
- Minecraft 1.20.1 / Forge 47.4.13 remain unchanged.
- Installed Core: `TerraFirmaGreg-Core-Modern-0.9.31.jar`.
- Core SHA-256: `9b78d57b898a309783b82b623128899ed60badb70aac401e31936195d8620913`.
- Core metadata still declares TFC 3.2.28. The helper dependency stays exactly `[3.2.28]`; no unverified TFC version was admitted.

## Retargeting and evidence

The original offline builder stopped because Core 0.9.30 is no longer installed. Pointing it at 0.9.31 then triggered the existing exact-artifact regression, which correctly refused the old expected Core filename. The build target and native oracle were explicitly retargeted to 0.9.31 rather than removing the check.

Nine relevant installed anvil/forging/screen/packet classes were inspected through javap. The helper compiles directly against the new Core and the exact MC/Forge classpath.

`build-1.1.3.log` records the fresh execution against Core 0.9.31: 449391 solver assertions, 905 solve calls, 13 groups passed / 0 failed; 278 recipe resources / 31 rule patterns, 366 model auto jobs and 3384 requested steps. STOP-control, input/cancellation/quarantine, idle tick, overlay geometry and final native resource-pack probe also pass. Replay counts are model checks, not real gameplay counts.

Binary comparison with the installed 1.1.2 helper finds changed entry contents only in `META-INF/mods.toml` and `vn/bdtg/anvil/TFGAnvilHelper.class` (target/version text). Every solver, automation, overlay/style and STOP-control class is byte-identical. This compares helper artifacts, not the complete old/new Core JARs.

## Installation and scope

The user confirmed saving and exiting before replacement. Only the helper JAR was replaced, with 1.1.2 backed up outside the instance. Installed 1.1.3 SHA-256: `97f11cf7fbca7f827c4bf9a46f5ce7fc8d4c4351a61b8a59a8df60b298df0211`.

A clean build produced identical contents in all 19 JAR entries. The installed 1.1.3 run reports successful startup on pack 0.13.17, RX 9060 XT, and no helper LoadingErrorScreen/resource-pack error. The actual Minecraft process was launched by the existing desktop Prism service; its effective memory.max is unlimited, not the terminal worker cap. Game/JVM settings were not modified.

No completed in-world forging/quality-tooltip sequence on the new pack is claimed from these tests. Release-channel labeling follows the owner's stable-release preference. No user world, account data, game dependency or full game log is included.
