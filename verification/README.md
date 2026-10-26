# Verification evidence

Evidence is kept separate by installed version; none of these files claims a GitHub Actions run.

## Helper 1.0.2 / TFG 0.13.16 / Core 0.9.30 / TFC 3.2.28

- [compatibility-1.0.2.md](compatibility-1.0.2.md): exact artifact identity, dependency RED→GREEN regression, API comparison, native tests and scope limits.
- `build-1.0.2.log`: 449,391 solver assertions, 905 solve calls, the new native dependency-range test, and final-JAR ResourcePackInfo probe.
- `main-menu-1.0.2.png`: the actual updated instance reached the main menu, visibly showing modpack 0.13.16.
- Complete forging on this new pack has not yet been observed.

## Helper 1.0.1 / TFG 0.13.15 / Core 0.9.29 / TFC 3.2.25

- `build-1.0.1.log`: compilation, solver/rule parity tests, and the final-JAR native resource-pack probe. The solver validation reports 449,391 assertions and 905 solve calls.
- `probe-old.log`: the old 1.0.0 JAR reproduces Minecraft's missing-pack-metadata warning and fails the native ResourcePackInfo check.
- `probe-fixed.log`: the fixed JAR is accepted and both language assets are readable.
- `main-menu-1.0.1.png`: the actual instance reached the main menu after the fix.
- `mod-loaded-1.0.1.png`: the in-game Mods screen shows helper 1.0.1 with State: done.

The user's later screen recording was inspected at timestamped output tooltips: Steel Screwdriver Tip (~17.5s), Steel Axe Head (~35s), and Steel Prospector's Pick Head (~51s) each showed **Perfectly Forged!**. That recording confirms the visible manual-forging flow on the previous installation only; H-toggle, server-lag behavior and all recipes were not verified. The recording and extracted gameplay images remain local.

Full Minecraft logs, failure archives, launcher accounts and local deployment manifests are deliberately excluded from Git. ANSI terminal colors and insignificant trailing whitespace are removed from the old-probe excerpt; its error text is unchanged. Local paths in the standalone test log identify the exact artifact used for API parity; no game account credentials are included.
