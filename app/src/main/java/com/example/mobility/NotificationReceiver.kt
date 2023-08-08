package com.example.mobility

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mobility.activity.MainActivity
import com.example.mobility.activity.UpdateOdoActivity


class NotificationReceiver : BroadcastReceiver() {

    var manager: NotificationManager? = null
    var builder: NotificationCompat.Builder? = null

    //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
    private val CHANNEL_ID = "ch1"
    private val CHANNEL_NAME = "Channel1"

    override fun onReceive(context: Context, intent: Intent) {

        Log.e("kkk","ok")
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager!!.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            NotificationCompat.Builder(context, CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context)
        }

        val intent = Intent(context, UpdateOdoActivity::class.java)


        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity( context, 101, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity( context, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        //알림창 제목
        builder!!.setContentTitle("주행거리를 업데이트 해주세요!!") //회의명노출

        //알림창 아이콘
        builder!!.setSmallIcon(R.mipmap.ic_launcher)

        //알림창 터치시 자동 삭제
        builder!!.setAutoCancel(true)

        builder!!.setContentIntent(pendingIntent)

        //푸시알림 빌드
        val notification: Notification = builder!!.build()

        //NotificationManager를 이용하여 푸시 알림 보내기
        manager!!.notify(1, notification)
    }
}