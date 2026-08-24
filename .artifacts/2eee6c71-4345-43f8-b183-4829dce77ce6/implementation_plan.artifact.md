# Implementation Plan - Modernize Activity Transitions

The goal is to improve the usage of `overridePendingTransition`, which is deprecated in API 34 (Android 14), and centralize the transition logic to ensure consistency and maintainability.

## User Review Required

> [!IMPORTANT]
> `overridePendingTransition(int, int)` was deprecated in Android 14. We will introduce a utility class to handle the transition logic, automatically choosing between the legacy and the new `overrideActivityTransition` API.

## Proposed Changes

### [Utils/Extras]

#### [NEW] [TransitionUtil.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/extras/TransitionUtil.java)
Create a utility class to handle activity transitions. It will provide:
- `applyFade(Activity activity)`: Applies a fade-in/fade-out transition.
- A method that handles the SDK version check internally.

### [Activities]

#### [MODIFY] [TelaLoguin.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/TelaLoguin.java)
Update `voltarParaMainActivity` and other navigation methods to use the new `TransitionUtil`.

#### [MODIFY] [VideoIniciarAppActivity.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/VideoIniciarAppActivity.java)
Update `iniciarMainActivity` and `iniciarEscolherLocalidade` to use `TransitionUtil`, removing the duplicated version check logic.

## Verification Plan

### Manual Verification
- Deploy the app on an Android 14+ device/emulator and verify the fade transition works when leaving `TelaLoguin` and `VideoIniciarAppActivity`.
- Deploy on an older device (API < 34) to ensure backward compatibility.
- Build the project to ensure no compilation errors with the new `TransitionUtil`.
