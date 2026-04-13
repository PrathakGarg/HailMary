# HailMary Emulator Test Suite Plan

This is the executable plan for the emulator-driven E2E suite.
Use it as the source of truth for what to build next and what is done.

## 1. Quick Start

1. Start emulator (if not already running):

```bash
~/Android/Sdk/emulator/emulator -avd HailMary_API_34
```

2. Run full E2E package (default: clears app data before run):

```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.arise.habitquest.e2e
```

3. Run smoke E2E suite:

```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.arise.habitquest.e2e.SmokeE2ESuite
```

4. Run full functional E2E suite:

```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.arise.habitquest.e2e.FullFunctionalE2ESuite
```

5. Run nightly boundary suite:

```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.arise.habitquest.e2e.BoundaryNightlySuite
```

6. Keep debug data instead of clearing:

```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.arise.habitquest.e2e -PkeepDebugData=true
```

7. Build-only validation (no emulator):

```bash
./gradlew :app:compileDebugAndroidTestKotlin :app:assembleDebugAndroidTest
```

8. Run CI pass gates (recommended):

```bash
./scripts/ci_e2e_gates.sh pr
./scripts/ci_e2e_gates.sh daily
./scripts/ci_e2e_gates.sh nightly
```

## 2. Current Status

### Done

- [x] Baseline E2E infrastructure and dependencies added.
- [x] Pre-test emulator data reset hook added in Gradle (`prepareConnectedTestDevice`).
- [x] Data reset can be skipped with `-PkeepDebugData=true`.
- [x] Stable selector tags available for core nav and mission board:
  - `bottom_nav_home`, `bottom_nav_missions`, `bottom_nav_status`, `bottom_nav_awards`
  - `onboarding_back`, `onboarding_next`
  - `onboarding_phase_<index>`
  - `registration_complete_screen`, `registration_complete_enter`
  - `missions_tab_row`, `missions_tab_<index>`, `missions_pager`, `missions_list_page_<index>`
- [x] Runtime package command validated on emulator.
- [x] Shared E2E selector/assertion helper utilities added.
- [x] E2E naming convention documented.
- [x] Initial seeded-state utilities added (first-run and returning-user seeds).
- [x] Active non-flaky e2e smoke tests added and passing:
  - `com.arise.habitquest.e2e.BottomNavSmokeE2ETest`
  - `com.arise.habitquest.e2e.OnboardingNavBarSmokeE2ETest`
  - `com.arise.habitquest.e2e.SmokeE2ESuite`
- [x] Active routing E2E tests restored and passing:
  - `com.arise.habitquest.e2e.LaunchRoutingE2ETest`
  - `com.arise.habitquest.e2e.ReturningUserLaunchE2ETest`
  - `com.arise.habitquest.e2e.OnboardingNavigationE2ETest`
  - `com.arise.habitquest.e2e.MissionBoardNavigationE2ETest`
  - `com.arise.habitquest.e2e.OnboardingFlowE2ETest`
  - `com.arise.habitquest.e2e.MissionDetailFlowE2ETest`
  - `com.arise.habitquest.e2e.HomeMissionBoardSyncE2ETest`
  - `com.arise.habitquest.e2e.BottomNavBackStackE2ETest`
  - `com.arise.habitquest.e2e.ProgressionStatusAchievementsE2ETest`
  - `com.arise.habitquest.e2e.HistoryE2ETest`
  - `com.arise.habitquest.e2e.RankUpFlowE2ETest`
  - `com.arise.habitquest.e2e.FullFunctionalE2ESuite`
- [x] Mission board empty-state selectors added for deterministic page assertions.
- [x] Registration completion layout updated so CTA remains visible on compact emulator screens.
- [x] Deterministic mission-detail selectors and seed utilities added for active daily mission flows.
- [x] Cross-surface Home → Mission Board synchronization coverage added for quick completion flows.
- [x] Status, achievements, history, and rank-up surfaces tagged for deterministic Phase 2 assertions.
- [x] Rank-up screen updated so the CTA remains reachable on compact emulator screens.
- [x] Suite split established: smoke vs full functional emulator suites.
- [x] Nightly boundary suite established for later-phase day-boundary and worker coverage.
- [x] Deterministic trusted-time harness added for Phase 3 boundary testing.
- [x] Worker scheduling verified: DailyResetWorker, PreResetReminderWorker, BootReceiver (all 7 workers).
- [x] DailyResetWorker.doWork() integration tested via custom WorkerFactory.
- [x] Settings boundary tests added: day-start-minutes shift, countdown update.
- [x] All notification workers tested (Morning, MidDay, Evening, WindDown, Sleep, MonthlyReport doWork paths).
- [x] MonthlyReportWorker idempotency: second run same month is a no-op.
- [x] Tamper-resistance suite: anchor retention, isClockTampered(), session-day isolation, re-pin stability.
- [x] Settings actions tested via ViewModel: rest-day update, regenerate effect (replace/skip on rest day), notification-hour reschedule, focus-theme constraints, emergency grace.
- [x] Focus-theme mission-content influence validated via social-theme regeneration coverage.
- [x] Settings UI nightly coverage added: Home → Settings navigation + regenerate dialog cancel/confirm flow.

### In Progress / Parked

- [x] No Phase 1 routing/onboarding tests currently parked.
- [x] No active Phase 2 routing/progression/status/history work currently parked.
- [x] Phase 3 settings/day-boundary/workers/tamper coverage complete in nightly suite.
- [x] Phase 4 regression matrix + CI pass gates implemented.

## 3. Implementation Phases

## Phase 1: Harness and Determinism

- [x] Deterministic emulator pre-run setup.
- [x] Reset utilities for app/test app state.
- [x] Permission handling for Android 13+ notifications.
- [x] Shared assertion helpers for route/state checks.
- [x] Naming convention for E2E IDs and reports.
- [x] Seeded state utilities (initial: first-run, returning-user).
- [x] Stable component-smoke coverage added for core navigation selectors.

## Phase 2: Core Routing and Mission Lifecycle

- [x] Splash routing: first run vs returning user.
- [x] Onboarding completion handoff to registration/home.
- [x] Bottom navigation and back stack behavior.
- [x] Mission board tab parity (tap + swipe).
- [x] Mission detail to complete/fail path validation.
- [x] Home and mission board synchronization after actions.

## Phase 2: Progression and Achievements

- [x] XP math validation for mission completion.
- [x] Achievement bonus XP application timing.
- [x] Level-up and rank-up transitions.
- [x] Streak increment/reset behavior.
- [x] Achievement locked/unlocked rendering and detail modal.

## Phase 2: Status and History

- [x] Status/profile values (rank, level, streak, stats).
- [x] History heatmap, weekly XP, insights rendering.
- [x] Empty state and populated state checks.

## Phase 3: Settings and Day Boundary

- [x] Day-start slider: shifting boundary late moves session day to yesterday.
- [x] Day-start slider: shifting boundary early moves session day to today.
- [x] Day-start change updates minutes-until-reset countdown proportionally.
- [x] Rest-day change with immediate state effect.
- [x] Regenerate-current-missions effect (replace on non-rest day, skip on rest day).
- [x] Notification hour updates + morning-worker reschedule.
- [x] Focus-theme constraints (min 1 / max 3) + mission-category influence.
- [x] Emergency stasis (grace activation) effects.

## Phase 3: Day Boundary and Workers (High Priority)

- [x] Before-boundary, at-boundary, after-boundary behavior.
- [x] Active daily auto-fail on rollover.
- [x] Rest-day skip mission generation.
- [x] Home countdown correctness.
- [x] Worker coverage:
  - [x] `DailyResetWorker` — schedule() enqueues named periodic work; doWork() fails expiring missions and generates fresh ones.
  - [x] `PreResetReminderWorker` — schedule() enqueues named periodic work.
  - [x] `BootReceiver` — onReceive(BOOT_COMPLETED) registers all 7 named workers.
  - [x] `MorningNotificationWorker` — doWork() active/no-missions paths return success.
  - [x] `MidDayCheckWorker` — doWork() stale-morning/no-morning paths return success.
  - [x] `EveningReminderWorker` — doWork() active-missions path returns success.
  - [x] `WindDownWorker` — doWork() active-wellness-mission path returns success.
  - [x] `SleepReminderWorker` — doWork() active and completed sleep-mission paths return success.
  - [x] `MonthlyReportWorker` — first-run writes key; second-run same month is idempotent.

## Phase 3: Tamper Resistance

- [x] Trusted anchor retained after NTP sync attempt (pinned future date stays unless real NTP wins).
- [x] isClockTampered() fires when trusted anchor diverges from system clock by >5 min.
- [x] isClockTampered() returns false when clocks agree.
- [x] Session day computed from trusted anchor, not System.currentTimeMillis.
- [x] Re-pinning identical timestamp twice yields stable session day.

## Phase 4: Regression Matrix and CI Strategy

- [x] Explicit regression tests for known defects:
  - streak inflation
  - delayed level-up after achievement XP
  - rest-day/session-day mismatch
  - mission regeneration correctness
  - route regressions
  - covered by:
    - `com.arise.habitquest.e2e.RegressionMatrixNightlyTest`
    - `com.arise.habitquest.e2e.RouteRegressionE2ETest`
- [x] Split suites:
  - smoke (fast)
  - full functional (daily)
  - boundary/worker (nightly)
- [x] Define pass gates and flake thresholds.
  - CI gate script: `scripts/ci_e2e_gates.sh`
  - `pr` mode:
    - run: `SmokeE2ESuite`
    - hard pass requirement: 100% pass on first run
    - duration cap: 15 minutes
    - flaky tolerance: 0 suites
  - `daily` mode:
    - run: `SmokeE2ESuite` + `FullFunctionalE2ESuite`
    - hard pass requirement: all suites pass
    - duration caps: 15 minutes smoke, 60 minutes full functional
    - retry policy: one retry for full functional if first attempt fails
    - flaky tolerance: max 1 suite that only passes on retry
  - `nightly` mode:
    - run: `SmokeE2ESuite` + `FullFunctionalE2ESuite` + `BoundaryNightlySuite`
    - hard pass requirement: all suites pass
    - duration caps: 15 minutes smoke, 60 minutes full functional, 90 minutes nightly boundary
    - retry policy: one retry for full functional and boundary if first attempt fails
    - flaky tolerance: max 1 suite that only passes on retry

## 4. Coverage Matrix Checklist

- [x] Entry/routing
- [x] Onboarding
- [x] Home
- [x] Mission board
- [x] Mission detail/completion
- [x] Achievements
- [x] Status/profile
- [x] History
- [x] Settings
- [x] Time boundary semantics
- [x] Worker scheduling (DailyReset, PreResetReminder, BootReceiver)
- [x] Worker notification payloads (Morning, MidDay, Evening, WindDown, Sleep, MonthlyReport)
- [x] Settings boundary (day-start minutes / countdown)
- [x] Settings UI interaction smoke (Home settings button + regenerate dialog flow)
- [x] Progression/penalties
- [x] Persistence/relaunch

## 5. Suite Maintenance Rules

- Every new emulator E2E test added in later phases must be assigned to one suite immediately.
- Default placement is `FullFunctionalE2ESuite`.
- Only promote a test into `SmokeE2ESuite` when it is fast, deterministic, and covers high-signal routing or surface health.
- Boundary, worker, and clock-tamper tests stay out of smoke and belong in `BoundaryNightlySuite` unless explicitly reclassified later.
- When Phase 3+ adds new E2E classes, update the target suite class and this plan in the same change.

## 6. Recommended Run Cadence

- Per PR (smoke): run `com.arise.habitquest.e2e.SmokeE2ESuite`, target < 15 min.
- Daily full functional: run `com.arise.habitquest.e2e.FullFunctionalE2ESuite`, target < 60 min.
- Nightly boundary/worker: run `com.arise.habitquest.e2e.BoundaryNightlySuite`, no strict cap, prioritize stability.

## 7. Report and Triage

On failure, collect:

- connected test HTML report
- XML result file
- logcat for failing class

Treat as:

- P0: day-boundary/progression corruption
- P1: worker/notification regression
- P2: UI route/surface behavior regression
- P3: cosmetic/render-only mismatch
