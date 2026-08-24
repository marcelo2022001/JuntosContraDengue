package com.example.juntoscontradengue.extras;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class Alertas {

    // Alert with Message and ok
    public static void showAlertDialog(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    // Alert with Message and ok Click Event
    public static void showAlertDialog(Context context, String title, String msg,
                                       DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("Continuar", listener);
        builder.setNegativeButton("Cancelar", listener);
        builder.create().show();
    }

    // Alert with Custom Positive and Negative buttons
    public static void showAlertDialog(Context context, String title, String msg,
                                       String positiveText, String negativeText,
                                       DialogInterface.OnClickListener positiveListener,
                                       DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveText, positiveListener);
        builder.setNegativeButton(negativeText, negativeListener);
        builder.setCancelable(false);
        builder.create().show();
    }

    // NEW: Alert with Runnable actions (simpler version)
    public static void showConfirmDialog(Context context, String title, String message,
                                         String positiveText, String negativeText,
                                         final Runnable positiveAction,
                                         final Runnable negativeAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (positiveAction != null) {
                    positiveAction.run();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (negativeAction != null) {
                    negativeAction.run();
                }
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);
        builder.create().show();
    }

    // NEW: Simplified version with default button texts
    public static void showConfirmDialog(Context context, String title, String message,
                                         final Runnable positiveAction,
                                         final Runnable negativeAction) {
        showConfirmDialog(context, title, message, "Sim", "Não", positiveAction, negativeAction);
    }

    // NEW: Dialog with three buttons
    public static void showThreeButtonDialog(Context context, String title, String message,
                                             String positiveText, String neutralText, String negativeText,
                                             final Runnable positiveAction,
                                             final Runnable neutralAction,
                                             final Runnable negativeAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (positiveAction != null) {
                    positiveAction.run();
                }
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(neutralText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (neutralAction != null) {
                    neutralAction.run();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (negativeAction != null) {
                    negativeAction.run();
                }
                dialog.dismiss();
            }
        });

        builder.setCancelable(true);
        builder.create().show();
    }

    // Adicione este método dentro da sua classe Alertas.java
    public static void showSuccessDialog(Context context, String title, String msg,
                                         DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("Ok", listener); // Apenas o botão OK
        builder.setCancelable(false); // Impede o usuário de fechar clicando fora
        builder.create().show();
    }

}