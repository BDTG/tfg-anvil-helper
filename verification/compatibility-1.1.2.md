# Helper 1.1.2 — explicit cancel-only control

Target remains TerraFirmaGreg Modern 0.13.16, Core 0.9.30 / embedded TFC 3.2.28, MC 1.20.1 / Forge 47.4.13.

## Cause and change

Earlier versions rendered shortcut text only; no clickable STOP control existed. F8 was already intended to toggle one-input automation. This release adds an explicit red STOP AUTO / DỪNG AUTO hit target at the right of the toolbar while a job is active. It disappears when the job finishes or is cancelled; auto does not remain enabled after completion.

The button calls the existing `AnvilAutomation.stop`, never starts a job, and does not clear pending-request quarantine. A highest-priority, anvil-scoped Pre mouse subscriber consumes the click before native inventory/screen dispatch. The hitbox is bound weakly to the screen that rendered it. Repeated clicks in a still-visible rectangle are consumed without repeating the callback. Hidden/inactive/error UI releases the hitbox. The toolbar reserves button space and clips its legend rather than painting text over the control.

## Executed tests

`python3 build.py` passes all tests and native resource-pack probe; see `build-1.1.2.log`.

- `StopControlTest`: hidden/wrong-screen/right-click/exclusive-edge checks, single callback and repeat click consumption, hidden hitbox release, narrow-toolbar bounds, null owner, actual controller cancellation with pending request, no new request/restart, HIGHEST mouse hook plus event cancellation and localized label wiring.
- Previous overlay geometry/legend, solver parity, 366 auto replay jobs, input/quarantine and idle-tick regressions remain passing.
- Tests of the control model, callback and subscriber metadata are not represented as real in-world mouse-click evidence.

Solver, AutoForgeController and native AnvilAutomation adapter are unchanged from v1.1.1. The final build was installed only after the user confirmed saving and exiting Minecraft. SHA-256: `1b476701a4737d46e5c04b7da23f7bf624044edbbffd8cf55b15a996be6e9bfc`. All 19 production entries match a clean build. The installed run reports helper 1.1.2, RX 9060 XT and completed startup with no helper loading/resource-pack error; its live process has MemoryMax=17179869184 in the dedicated user service. No screenshot, world data or full game logs are published.

Live in-world visual/click verification of the new control remains for the user; an already-sent native action cannot be recalled when cancelled.
