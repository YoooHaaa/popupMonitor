package com.popup.monitor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;


public class MyAccessService extends AccessibilityService {
    String[] otherpkg = new String[]{"com.popup.monitor", "xiaomi", "huawei", "vivo", "com.system",
            "oppo", "tencent", "miui", "com.android", "qq", "com.google", "launcher"
            };
    String[] otherAct = new String[]{"launcher", "Launcher", "android.wedget"
    };

    public MyAccessService() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.d("yooha",  "in onAccessibilityEvent " );
        String packageName = event.getPackageName().toString();
        String activityName = event.getClassName().toString();
        SharedPreferences sp = null;
        try{
            sp = getSharedPreferences("monitor", 0);
        }
        catch  (Exception e) {
            //Log.d("yooha",  "error -> " + e.toString());
            e.printStackTrace();
        }

        try{
            //Log.d("yooha",  "捕捉到 " + packageName);
            for(String pkg : otherpkg)
            {
                if (packageName.indexOf(pkg) != -1) {
                    //Log.d("yooha", pkg + " 排除");
                    return;
                }
            }

            for(String pkg : otherAct)
            {
                if (activityName.indexOf(pkg) != -1) {
                    //Log.d("yooha", pkg + " 排除");
                    return;
                }
            }


            int eventType = event.getEventType();
            switch (eventType) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    //Log.d("yooha",  " TYPE_WINDOW_STATE_CHANGED");
                    //Log.d("yooha",  packageName + "/" + event.getClassName());
                    savePkgname(packageName + "/" + event.getClassName(), sp);
                default:
                    //Log.d("yooha",  " OTHER TYPE");
                    //Log.d("yooha",  packageName + event.getClassName());
            }

        }catch  (Exception e) {
            Log.d("yooha",  "getEventType  error -> " + e.toString());
            e.printStackTrace();
        }


    }



    @Override
    public void onInterrupt() {

    }

    public void savePkgname(String pkg, SharedPreferences sp){
        try{
            SharedPreferences.Editor edit = sp.edit();
            //Log.d("yooha", sp.getString("pkg1", ""));
            //Log.d("yooha", sp.getString("pkg2", ""));
            //Log.d("yooha", sp.getString("pkg3", ""));
            //Log.d("yooha", sp.getString("pkg4", ""));
            //Log.d("yooha", sp.getString("pkg5", ""));

            edit.putString("pkg1", sp.getString("pkg2", ""));
            edit.putString("pkg2", sp.getString("pkg3", ""));
            edit.putString("pkg3", sp.getString("pkg4", ""));
            edit.putString("pkg4", sp.getString("pkg5", ""));
            edit.putString("pkg5", pkg);

            edit.commit();
        }catch  (Exception e) {
            Log.d("yooha",  "getEventType  error -> " + e.toString());
            e.printStackTrace();
        }

    }



    @Override
    protected void onServiceConnected() {
        //Log.d("yooha",  " access onServiceConnected ");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED |
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOWS_CHANGED;

        info.packageNames = null;
                //{"com.superclean.master", "com.cleanmaster.lite_cn"};

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        //Log.d("yooha",  " access onServiceConnected  over");
    }







}