# Fix NullPointerException in MainActivity.mostrarTutorial

The application crashes with a `NullPointerException` because `MainActivity` attempts to access `layoutTutorial` after it has been removed from the view hierarchy.

## Analysis
In `fragment_home.xml`, `layoutTutorial` is a child of `v_flipper` (a `ViewFlipper`).
In `MainActivity.java`, the methods `carregarImagensOffline()` and `carregarUrlsParaFlipper()` call `v_flipper.removeAllViews()`. This removes all children of the `ViewFlipper`, including the statically defined `layoutTutorial`.
When `mostrarTutorial()` is subsequently called, `findViewById(R.id.layoutTutorial)` returns `null`, leading to the crash when `setAlpha(0f)` is called.

## Proposed Changes

### [UI Layout]

#### [MODIFY] [fragment_home.xml](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/res/layout/fragment_home.xml)
- Move the `LinearLayout` with `android:id="@+id/layoutTutorial"` from being a child of `v_flipper` to being a sibling of `v_flipper` (a direct child of the parent `FrameLayout`). This prevents it from being deleted when `v_flipper.removeAllViews()` is called.

### [MainActivity]

#### [MODIFY] [MainActivity.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/MainActivity.java)
- Add null checks for `v_flipper`, `tutorial`, and `hand` views to make the code more robust against lifecycle and hierarchy changes.
- Ensure `v_flipper` is not null before calling its methods in `onCreate` and other lifecycle methods.

## Verification Plan

### Automated Tests
- Build the project to ensure no syntax errors.
- Run the app and verify that the tutorial animation still plays correctly.

### Manual Verification
- Test with and without internet connection to trigger `carregarImagensOffline()` and `carregarUrlsParaFlipper()` paths.
- Verify that the "Toque para ampliar" tutorial appears and disappears as expected.
