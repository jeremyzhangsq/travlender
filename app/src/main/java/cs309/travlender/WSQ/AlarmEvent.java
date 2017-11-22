package cs309.travlender.WSQ;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import cs309.travlender.ZSQ.Event;

/**
 * Created by alicewu on 11/20/17.
 */

public class AlarmEvent extends Event implements Serializable{
    private AlarmManager alarm;//对应闹钟的指针
    private Event fatherE;//指向原始闹钟的指针

    boolean isQuery = true;//if location is null
    private long onwayTime = 0;//on way time, if location is not null
    private long departTime;//if location is not null
    private long remindEarly = 0;//if set by user
    private long startTime;
    //查询队列的优先级是min（deparTime,remindBefore）,由小到大
    //when happen，it must remind.
    String bestTransport;

    public AlarmEvent(Alarm value, Event father) {
        super((Cursor) value);
        if (father != null) {
            fatherE = father;
            startTime = fatherE.getStarttime();
            if (super.getLocation() == null) {//无地点
                onwayTime = 0;//路程时间为零
                isQuery = false;//不需要查询
            } else {
                updateBestTransport(new Date());
                isQuery = true;
            }
            departTime = fatherE.getStarttime() - onwayTime;
            remindEarly = fatherE.getStarttime() - fatherE.getRemindtime();
        }
        //else throw exception
    }

    //设置闹钟,给定时间设置闹钟
    public void schedule(Context context, long remindTime) {
        Intent myIntent = new Intent(context, AlarmReceiver.class);//在闹钟设定时刻通知alarmAlert
        myIntent.putExtra("alarm", this);
        myIntent.put

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, getRealAlarmTime(remindTime).getTimeInMillis(), pendingIntent);
    }

    public Calendar getRealAlarmTime(long remindTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(remindTime);
        return calendar;
    }

    //每次查询都要调用地图
    public void updateBestTransport(Date current) {
        onwayTime = map.getOnWayTime(fatherE.getLocation(), fatherE.getStarttime());
        bestTransport = map.getBestTrans(fatherE.getTransport(), fatherE.getLocation(), fatherE.getStarttime());//优化交通方式, 交通方式的比较

    }

    public Event getFatherE() {
        return fatherE;
    }

    public long getDepartT() {
        return departTime;
    }

    public long getRemindEarlyT() {
        return remindEarly;
    }

    public long getOnWayTime() {
        return onwayTime;
    }

    @Override
    public long getStarttime() {
        return startTime;
    }
}
