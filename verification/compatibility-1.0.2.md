# Helper 1.0.2 / TerraFirmaGreg Modern 0.13.16

## Target observed in the installed instance

| Component | Version |
|---|---|
| TerraFirmaGreg Modern | 0.13.16 (CurseForge file ID 9323771) |
| Minecraft | 1.20.1 |
| Forge | 47.4.13 |
| Core | TerraFirmaGreg-Core-Modern-0.9.30.jar |
| Embedded TerraFirmaCraft | 3.2.28 |
| Helper | 1.0.2 |

Core SHA-256: `13c2d5872936d4c01c3c53ae1e873a6291a51b5d0d869da44e220b3df74b9647`.

Helper JAR SHA-256: `45821e544bf6e2987fd3a5dfeb92af4e3c9aac8fdca8a1f3933d8085c10d0f97`.

The Core is an external installed dependency and is not included in this repository or release.

## Regression and compatibility checks

1. Before retargeting, the existing build failed because Core 0.9.29 was no longer installed.
2. A new `ModCompatibilityTest`, run against Core 0.9.30, reproduced the actual dependency mismatch:

```text
Helper TFC range [3.2.25] rejects installed TFC 3.2.28 from TerraFirmaGreg-Core-Modern-0.9.30.jar
```

3. Helper 1.0.2 accepts exactly TFC 3.2.28. The test uses NightConfig's TOML parser and Maven's native `VersionRange`, not a home-made version comparison; unverified versions remain rejected.
4. The Java overlay compiles against the new Core. The native solver parity/oracle suite passes **449,391 assertions and 905 solve calls**, including all 278 bundled anvil recipe rule sets. See [build-1.0.2.log](build-1.0.2.log).
5. Native Minecraft resource-pack parsing accepts the final JAR and both language assets.
6. A comparison of `javap -c -p` output for **ForgeStep and AnvilBlockEntity** against the saved Core 0.9.29 baseline found **34 matching method bodies, zero changed/added/removed**, after normalizing constant-pool reference IDs. This is a scoped static comparison, not a claim that the entire Core is unchanged.
7. `Solver.java`, `AnvilOverlay.java` and `OverlayModel.java` are unchanged from helper 1.0.1; only the release/target metadata and build/test target were updated.

## Actual host loading

The real installed instance was launched after replacing the old helper, without a server/world launch argument:

- The log lists `tfg_anvil_helper 1.0.2`.
- Startup completed; no LoadingErrorScreen or helper ResourcePackInfo warning was observed.
- The captured main menu visibly identifies **modpack 0.13.16**. See [main-menu-1.0.2.png](main-menu-1.0.2.png).
- The installed helper bytes match the release JAR.

The startup log reports **149.799 seconds**. The process inherited `DRI_PRIME=pci-0000_03_00_0`, and both Embeddium and Oculus report **AMD Radeon RX 9060 XT (radeonsi, gfx1200)** with OpenGL 4.6. The observed launch did not select the Radeon 610M iGPU or a software renderer. The cause of the longer loading time has not been established; GPU selection was not changed as part of this compatibility update.

## Limits of this evidence

- No complete forging run has been observed on **TFG 0.13.16 / helper 1.0.2** yet.
- The user's earlier recording proved three Perfectly Forged outputs on the previous installation; it is not treated as gameplay verification of this updated pack.
- This report records local testing and host loading, not GitHub Actions/CI.
- No other mod, world or GPU setting was intentionally modified. Full game/launcher logs and the user's recording are not included.
