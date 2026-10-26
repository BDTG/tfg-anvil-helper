# Helper 1.1.0 — optional one-input automation (pre-release)

## Target

- TerraFirmaGreg Modern 0.13.16; Core 0.9.30 / embedded TFC 3.2.28.
- Minecraft 1.20.1; Forge 47.4.13; Java 17.
- Previous helper 1.0.2 backed up outside the instance; only the helper JAR replaced.

## Executed checks

`python3 build.py` passed all test mains and final native resource-pack probe; see `build-1.1.0.log`.

- Solver validation: 449391 assertions, 905 solve calls, 13 groups passed / 0 failed against the installed TFC artifact.
- Auto replay: 278 recipe resources, 31 distinct rule patterns; 366 reachable jobs, 3384 requested steps, delayed model acknowledgements at 1/7/29 ticks. Simulated acknowledgements are not gameplay measurements.
- Controller regressions: cancellation/timeout retain the outstanding-request reservation and prevent a second auto request; a new server menu can rearm without auto-starting. Send-rate deadlines survive restarts.
- Input integration: real CommonInputs Enter/keypad Enter/Space predicate cancels; same-menu and invalid bindings cannot clear quarantine, new server menu/connection can.
- Controller regressions: explicit start, one outstanding action, four-tick spacing, cancellation, input changes, workability, unexpected progress, timeout and late output acknowledgement.
- ASM integration: five input/lifecycle/tick hooks and one native AnvilStepButton callback site; no custom network packets or direct forging/inventory mutations.
- 10000 inactive client ticks run outside Minecraft without singleton access or activation.
- English/Vietnamese strings cover every controller status. Final JAR parsed through Minecraft native pack APIs.

## Runtime observed

The final installed 1.1.0 JAR (`dec2103544cf2b9985610d16fa4403a21f51d77ee5aabd18456626d9b07ea671`) starts successfully on the target instance; the post-fix run reports 46.98 seconds to start. The same run reports RX 9060 XT as OpenGL renderer and has no helper LoadingErrorScreen/resource-pack error. Its separately bounded user service has MemoryMax=17179869184 rather than inheriting the terminal worker's smaller cap; JVM/graphics/game settings were not changed.

A local screenshot from the earlier development JAR, before the two reviewed safety fixes, shows the real anvil GUI with the F8 hint and `Auto: finished | F8: new job`. This is evidence only for that earlier UI layout, not final-patch gameplay. It does not independently establish the full keyboard-to-output flow or output quality. The screenshot includes the user's world and remains local/untracked.

## Not yet independently verified

- A recorded F8 start through the complete native action sequence and resulting item-quality tooltip.
- Real multiplayer latency/disconnect/cancellation cases and external inventory automation.
- Rebinding F8 (currently a fixed anvil-only shortcut).

If an unconfirmed native request remains after cancellation/timeout, close and reopen the anvil before restarting; key presses alone cannot clear that reservation. One already-sent native action cannot be recalled on cancellation. No automatic second input, inventory transfers, temperature edits, batch forging or packet retries are implemented.
