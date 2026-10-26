# Helper 1.1.1 — UI-only follow-up

Target: TerraFirmaGreg Modern 0.13.16, Core 0.9.30 / TFC 3.2.28, MC 1.20.1 / Forge 47.4.13.

## Changes

- A separate F8 start/cancel and H show/hide legend is the first row in both active-route and idle/completed/no-recipe panels, in English and Vietnamese.
- Status lines no longer compete with the permanent shortcut legend.
- The next route icon and native button share an opaque saturated-magenta (#FF2DA6) / dark outer double frame. Combined thickness is two GUI pixels.
- The 20x20 frame leaves the entire native 16x16 icon untouched and stays within the existing bounds, so it cannot bleed into adjacent buttons.
- Geometry is immutable and allocated once. Solver, automation controller and native input/packet adapter are unchanged from v1.1.0.

## Executed checks

`python3 build.py` passed all tests and the final Minecraft native resource-pack probe; see `build-1.1.1.log`.

`OverlayPresentationTest` rasterizes the production rectangle geometry, checks every frame/interior pixel, checks both render paths reference the fixed controls, confirms both highlight sites and English/Vietnamese legends. This verifies component geometry and wiring, not a screenshot of live Minecraft.

Existing solver validation (449391 assertions / 905 solve calls), 366 model replay jobs, native key/cancellation/quarantine tests and inactive-tick checks remain passing.

The user reported testing 1.1.0 and requested these UI changes. That report and the supplied screenshot are not proof of 1.1.1 visuals or a particular output-quality tooltip. No user screenshot or game/world logs are included in this release.

The final JAR was copied into the instance only after the user confirmed saving and exiting Minecraft. SHA-256: `0007b626e3562991a6e5bd1757d6088862c94844575ffa0e48d7f9662cf7bcaa`. A clean build produced identical contents in all 18 JAR entries. The installed run reports helper 1.1.1, RX 9060 XT, successful startup, and no helper loading/resource-pack error. Its process remains in a separately bounded 16-GiB user service; game settings were not changed. Live visual layout across GUI scales and color preference still require the user's in-game check.
