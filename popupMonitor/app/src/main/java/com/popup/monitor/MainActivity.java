package com.popup.monitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {
    public String TAG = "yooha";
    PopupService mPopupService = null;
    private ServiceConnection mServiceConnection;
    private Intent mServiceIntent;
    Switch mSwitch;
    private Intent mIntent = null;
    private int result = 0;
    private int REQUEST_MEDIA_PROJECTION = 1;

    /**
     * 截屏相关
     *
     */
    private MediaProjectionManager mMediaProjectionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPower(this);

        Intent intent = new Intent(MainActivity.this, MyAccessService.class);
        this.startService(intent);

        mSwitch = (Switch) findViewById(R.id.switch1);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onMyCheckedChanged(isChecked);
            }
        });
        Log.d(TAG, "get MediaProjectionManager");
        mMediaProjectionManager = (MediaProjectionManager)getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Log.d(TAG, "get MediaProjectionManager over");
        startIntent();
    }


    private void startIntent(){
        if(mIntent != null && result != 0){
            ((StubApplication)getApplication()).setResult(result);
            ((StubApplication)getApplication()).setIntent(mIntent);
            myBindService();
        }else{
            Log.d(TAG, "get MediaProjectionManager startActivityForResult");
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            ((StubApplication)getApplication()).setMediaProjectionManager(mMediaProjectionManager);
            Log.d(TAG, "get MediaProjectionManager startActivityForResult over");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            Log.d(TAG, "onActivityResult REQUEST_MEDIA_PROJECTION");
            if (resultCode != Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult RESULT  NOT  OK");
                return;
            } else if (data != null && resultCode != 0) {
                Log.d(TAG, "onActivityResult RESULT   OK");
                result = resultCode;
                mIntent = data;
                ((StubApplication) getApplication()).setResult(resultCode);
                ((StubApplication) getApplication()).setIntent(data);
                myBindService();
            }
        }
    }


    public void onMyCheckedChanged(boolean isChecked) {
        if(isChecked) {
            mPopupService.show();
        } else {
            mPopupService.dimiss();
        }
    }


    public void requestPower(Context mContext) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                //第一次上面这个方法返回的是false，之后就一直返回true
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);//核心
                //此处会弹出一个框框询问你是否给予权限
            }
        }

        //悬浮窗权限
        checkOVERLAYPermission();

        //无障碍权限要与自启动权限一起打开才能生效
        if (!isAccessibilitySettingsOn(mContext, MyAccessService.class)){
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
    }

    //检查无障碍权限是否打开
    public static boolean isAccessibilitySettingsOn(Context mContext, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + clazz.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1){
            for(int i=0;i<permissions.length;i++){//可能有多个权限，需要观测是否为PERMISSION_GRANTED状态
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "权限" + permissions[i] + "申请失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //悬浮窗权限
    public void checkOVERLAYPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 10);
                openAutoStartSetting();
            }
        }
    }

    //自启动权限
    public void openAutoStartSetting(){
        ComponentName componentName = null;
        String brand = Build.MANUFACTURER;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (brand.toLowerCase()) {
            case "samsung"://三星
                componentName = new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity");
                break;
            case "huawei"://华为
                //荣耀V8，EMUI 8.0.0，Android 8.0上，以下两者效果一样
                componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity");
                break;
            case "xiaomi"://小米
                componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                break;
            case "vivo"://VIVO
                componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
                break;
            case "oppo"://OPPO
                componentName = new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
                break;
            case "yulong":
            case "360"://360
                componentName = new ComponentName("com.yulong.android.coolsafe", "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity");
                break;
            case "meizu"://魅族
                componentName = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity");
                break;
            case "oneplus"://一加
                componentName = new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity");
                break;
            case "letv"://乐视
                intent.setAction("com.letv.android.permissionautoboot");
            default://其他
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", this.getPackageName(), null));
                break;
        }
        intent.setComponent(componentName);
        Log.d(TAG, "AutoStartSetting = " + intent);
        startActivity(intent);
    }


    private void myBindService() {
        mServiceIntent = new Intent(MainActivity.this, PopupService.class);

        if(mServiceConnection == null) {
            Log.d("yooha", "myBindService  in  mServiceConnection");
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d("yooha", "myBindService  onServiceConnected");
                    mPopupService = ((PopupService.PopupBinder) service).getService();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("yooha", "onService   Disconnected");
                }
            };
            bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    private void myUnBindService() {
        if(null != mServiceConnection) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
    }


    @Override
    protected void onPause() {
        myUnBindService();
        super.onPause();
    }


    @Override
    protected void onStop() {
        myUnBindService();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        myUnBindService();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        myBindService();
        super.onRestart();
    }


    @Override
    protected void onResume() {
        myBindService();
        super.onResume();
    }

}