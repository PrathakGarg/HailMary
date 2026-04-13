# E2E Naming Convention

Use this naming standard for consistency and report readability.

## Test Class

Format:

`<FeatureArea><Behavior>E2ETest`

Examples:

- `LaunchRoutingE2ETest`
- `MissionBoardNavigationE2ETest`
- `SettingsRestDayE2ETest`

## Test Method

Format:

`<initial_state>_<action>_<expected_outcome>`

Examples:

- `freshInstall_launchRoutesToOnboarding`
- `onboardingComplete_openMissionsTab_showsTabRow`
- `restDayChanged_regenerateConfirmed_refreshesMissions`

## IDs for Reports

When adding custom logs, include:

- feature: route, onboarding, missions, progression, settings, boundary, workers
- severity: smoke, functional, nightly
- scenario key: short snake_case identifier

Example:

`[E2E][smoke][route][fresh_install_to_onboarding]`
