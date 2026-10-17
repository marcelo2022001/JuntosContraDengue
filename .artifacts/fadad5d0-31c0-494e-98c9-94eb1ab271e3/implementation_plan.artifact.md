# Fix Resource Conflict: Duplicate `text_primary` and `text_secondary`

The project build is failing because the resource names `text_primary` and `text_secondary` are defined twice in `colors.xml` with different color values. This conflict prevents the resource merger from completing successfully.

## Proposed Changes

I will rename the duplicate color resources in the section specific to the "Sobre" (About) screen to unique names and update the corresponding layout file to use these new names. This preserves the intended design while resolving the build error.

### Resources

#### [MODIFY] [colors.xml](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/res/values/colors.xml)
- Rename `text_primary` (#0F172A) to `text_sobre_primary` in the "Cores Gerais Tela Sobre" section.
- Rename `text_secondary` (#334155) to `text_sobre_secondary` in the "Cores Gerais Tela Sobre" section.

#### [MODIFY] [activity_sobre_aplicativo.xml](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/res/layout/activity_sobre_aplicativo.xml)
- Update all occurrences of `@color/text_primary` to `@color/text_sobre_primary`.
- Update all occurrences of `@color/text_secondary` to `@color/text_sobre_secondary`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:mergeDebugResources` to verify that the resource conflict is resolved and the build succeeds.

### Manual Verification
- None required for build fix, but the "Sobre" screen UI should be visually checked to ensure the colors are still applied correctly.
