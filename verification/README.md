# Verification evidence

These are historical results from the local Java 17 / Minecraft 1.20.1 / Forge 47.4.13 / TerraFirmaGreg Core 0.9.29 installation, not GitHub Actions results.

- `build-1.0.1.log`: compilation, solver/rule parity tests, and the final-JAR native resource-pack probe. The solver validation reports 449,391 assertions and 905 solve calls.
- `probe-old.log`: the old 1.0.0 JAR reproduces Minecraft's missing-pack-metadata warning and fails the native ResourcePackInfo check.
- `probe-fixed.log`: the fixed JAR is accepted and both language assets are readable.
- `main-menu-1.0.1.png`: the actual instance reached the main menu after the fix.
- `mod-loaded-1.0.1.png`: the in-game Mods screen shows helper 1.0.1 with State: done.

The full gameplay flow (open anvil, follow hints, produce a forged item) has not been verified. The evidence does not claim that it has.

Full Minecraft logs, failure archives, launcher accounts and local deployment manifests are deliberately excluded from Git. ANSI terminal colors and insignificant trailing whitespace are removed from the old-probe excerpt; its error text is unchanged. Local paths in the standalone test log identify the exact artifact used for API parity; no game account credentials are included.
