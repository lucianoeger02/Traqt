package com.catteno.traqt.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.catteno.traqt.HomeActivity;
import com.catteno.traqt.R;

/**
 * Um BroadcastReceiver para os eventos de lembretes de atividades do Traqt.
 */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Constrói a notificação de um lembrete de uma atividade
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_traqt_notification)
                .setContentTitle("Lembrete de Atividade")
                .setContentText("Você tem uma atividade agendada para hoje!");

        // Verificação de compatibilidade.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // O suporte a notificações que abrem o App só exist a partir do Jelly Bean
            Intent homeIntent = new Intent(context, HomeActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(HomeActivity.class);
            stackBuilder.addNextIntent(homeIntent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
        }

        // Envia a notificação através do serviço 'NotificationManager' do Android
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

}