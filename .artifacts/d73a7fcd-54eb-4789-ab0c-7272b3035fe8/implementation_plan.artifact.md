# Fix Firebase Database Type Conversion Exception

The application is crashing with a `com.google.firebase.database.DatabaseException: Failed to convert a value of type java.lang.String to long`. This occurs because Firebase is trying to deserialize a value from the database into a `long` or `Long` field in a Java class (or a direct `getValue(Long.class)` call), but the value in the database is currently a `String`.

## User Review Required

> [!IMPORTANT]
> The fix involves changing several data model classes (POJOs) to use `Object` for numeric fields that might be stored as `String` in the database. This pattern is already present in `ClassAgentes.java` and will be extended to other classes to ensure robustness.

## Proposed Changes

### [Component] Database Classes (POJOs)

We will update the following classes to use `Object` for fields that are causing (or could cause) the type conversion error. We will also add helper methods to safely retrieve these values as `Long`.

#### [MODIFY] [ClassUsuarios.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/database/classes_database/ClassUsuarios.java)
- Change `dataCadastro` and `updatedAt` from `Long` to `Object`.
- Add `getDataCadastroAsLong()` and `getUpdatedAtAsLong()` helper methods.
- Fix a bug in the constructor where `cpf` was being assigned twice instead of `funcao`.

#### [MODIFY] [ClassReclamacoes.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/database/classes_database/ClassReclamacoes.java)
- Change `data_envio` from `Long` to `Object`.
- Add `getData_envioAsLong()` helper method.

#### [MODIFY] [ClassListarReclamacoes.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/database/classes_database/ClassListarReclamacoes.java)
- Change `data_envio` from `Long` to `Object`.
- Add `getData_envioAsLong()` helper method.

#### [MODIFY] [ClassTrabAgentes.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/database/classes_database/ClassTrabAgentes.java)
- Change `dataUpload` from `long` (primitive) to `Object`.
- Add `getDataUploadAsLong()` helper method.

#### [MODIFY] [ClassDeletarPreCadastro.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/database/classes_database/ClassDeletarPreCadastro.java)
- Change `data_pre_cadastro` from `Long` to `Object`.
- Add `getData_pre_cadastroAsLong()` helper method.

#### [MODIFY] [ClassAddSliders.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/database/classes_database/ClassAddSliders.java)
- Change `id` from `Long` to `Object`.
- Add `getIdAsLong()` helper method.

---

### [Component] Utils

#### [NEW] [FirebaseTypeUtils.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/extras/FirebaseTypeUtils.java)
- Create a utility class with a static method `safeGetLong(DataSnapshot snapshot)` or `parseLong(Object value)` to safely convert database values to `Long` without crashing.

---

### [Component] Activities

We will update direct `getValue(Long.class)` and `getValue(Integer.class)` calls to use the new utility method to prevent crashes when reading individual fields.

#### [MODIFY] [ActivityLoginAdmin.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/ActivityLoginAdmin.java)
- Use `FirebaseTypeUtils` to safely read `dataCadastro` and `updateAt`.

#### [MODIFY] [ActivityLoginAgentes.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/ActivityLoginAgentes.java)
- Use `FirebaseTypeUtils` to safely read `dataCadastro` and `updateAt`.

#### [MODIFY] [TelaLoguin.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/TelaLoguin.java)
- Use `FirebaseTypeUtils` to safely read `dataCadastro` and `updateAt`.

#### [MODIFY] [ResponderDenunciaActivity.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/ResponderDenunciaActivity.java)
- Use `FirebaseTypeUtils` to safely read `data_envio`.

#### [MODIFY] [AddAgentes.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/AddAgentes.java)
- Use `FirebaseTypeUtils` to safely read `totalAdminPodeCadastrar`, `totalAgentePodeCadastrar`, and `valor`.

#### [MODIFY] [UploadTrabAgentes.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/UploadTrabAgentes.java)
- Use `FirebaseTypeUtils` to safely read `valor`.

#### [MODIFY] [ExcluirAgentesActivity.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/ExcluirAgentesActivity.java)
- Use `FirebaseTypeUtils` to safely read `valorAtual`.

#### [MODIFY] [ExcluirTrabAgentesActivity.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/ExcluirTrabAgentesActivity.java)
- Use `FirebaseTypeUtils` to safely read `currentValue`.

#### [MODIFY] [database/adapters/AdapterReclamacaoUsuarios.java](file:///C:/Users/Marcelo/AndroidStudioProjects/JuntosContraDengue/app/src/main/java/com/example/juntoscontradengue/database/adapters/AdapterReclamacaoUsuarios.java)
- Use `FirebaseTypeUtils` to safely read `valorAtual`.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- (Optional) Create a unit test for `FirebaseTypeUtils` to verify it handles `Long`, `String`, and `null` correctly.

### Manual Verification
- Deploy the app and navigate to the screens where the crash occurred (Profile, Login, etc.).
- Verify that the app no longer crashes even if the database contains `String` representations of numeric timestamps.
