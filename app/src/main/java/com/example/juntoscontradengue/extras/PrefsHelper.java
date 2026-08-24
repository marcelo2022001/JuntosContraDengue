package com.example.juntoscontradengue.extras;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {

    // Nome do arquivo de preferências no dispositivo
    private static final String PREFS_NAME = "MyAppPrefs";

    /**
     * Salva uma string genérica associada a uma chave específica.
     * @param context O contexto da aplicação.
     * @param key A chave única para identificar o dado (ex: "user_email", "user_phone").
     * @param value O valor a ser salvo.
     */
    public static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);
        editor.apply(); // Salva de forma assíncrona

    }

        /**
         * Recupera uma string usando uma chave específica.
         * @param context O contexto da aplicação.
         * @param key A chave única para identificar o dado.
         * @param defaultValue O valor padrão a ser retornado se a chave não existir.
         * @return O valor associado à chave ou o valor padrão.
         */

        public static String getString(Context context, String key, String defaultValue) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(key, defaultValue);
        }

        /**
         * Remove um dado específico pela chave.
         */
        public static void removeData(Context context, String key) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.apply();
        }

    }

    /*private static final String KEY_USER_EMAIL = "user_email";

    private SharedPreferences sharedPreferences;

    public PrefsHelper(Context context) {
        try {
            // Cria a chave mestra usando MasterKey
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Inicializa o EncryptedSharedPreferences
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Log de erro usando Log.e
            Log.e(TAG, "Erro ao inicializar EncryptedSharedPreferences", e);
            // Fallback para SharedPreferences comum (não recomendado para dados sensíveis)
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

    }

    // Salva o e-mail do usuário
    public void saveUserEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    // Recupera o e-mail do usuário
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    // Remove o e-mail do usuário (por exemplo, no logout)
    // Método para uso futuro (ex: durante o logout)

    public void clearUserEmail() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_EMAIL);
        editor.apply();
    }
}
*/
