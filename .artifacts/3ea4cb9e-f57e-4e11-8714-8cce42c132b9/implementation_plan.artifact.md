# Implementation Plan - Improving Firebase Data Retrieval

The goal is to modernize and improve the `addListenerForSingleValueEvent` usage in the project, specifically starting with `Denunciar.java`. The current implementation uses an older callback-based approach, lacks robust error handling, and contains a type mismatch bug.

## User Review Required

> [!IMPORTANT]
> The current code in `Denunciar.java` at line 567 has a compilation error: it attempts to assign the return value of `addListenerForSingleValueEvent` (a `ValueEventListener`) to a `DatabaseReference` variable. My plan fixes this by removing the unnecessary assignment.

> [!NOTE]
> I am switching from `addListenerForSingleValueEvent` to `get()`. While both achieve a single-time read, `get()` returns a `Task<DataSnapshot>`, which is the modern standard in Firebase and allows for cleaner code with `addOnCompleteListener` and lambdas.

## Proposed Changes

### Core Improvements
- **Switch to `get()`**: Replace the verbose `ValueEventListener` with `Task.get()`.
- **Lambda Expressions**: Use Java 8 lambdas for cleaner callbacks.
- **Error Handling**: Log database errors and provide feedback if the read fails.
- **Bug Fix**: Remove the incorrect assignment to `DatabaseReference`.

### [Component Name]

#### [MODIFY] [Denunciar.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/Denunciar.java)
- Refactor `verificaTotalReclamacoes` to use `.get().addOnCompleteListener()`.
- Fix the type mismatch in the variable declaration/assignment.
- Add `Log.e` and a `Toast` for the `onCancelled` (failure) case.

## Verification Plan

### Automated Tests
- I will perform a build check to ensure the type mismatch is resolved and the code compiles with the new `get()` API.

### Manual Verification
- Verify that the complaint limit (3) is still enforced correctly.
- Verify that errors during database access are now logged and reported to the user.
