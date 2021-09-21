package com.popup.monitor;


import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;


public class PopupService extends Service implements View.OnClickListener {
    public String TAG = "yooha";

    /**
     * 控件window
     */
    private FloatingView mFloatingWindow;

    /**
     * 两个状态的View
     */
    private View mFloatView;
    private View mPopupView;

    /**
     * 显示结果的View
     */
    private View mShowPkgView;

    /**
     * popup功能图片
     */
    private ImageView mIvpkgname;
    private ImageView mIvAllPkgname;
    private ImageView mIvScreenShot;
    private ImageView mIvFunc4;


    /**
     * 截图相关
     */
    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;
    private SimpleDateFormat dateFormat = null;
    private String strDate = null;
    private String nameImage = null;
    public static int mResultCode = 0;
    public static Intent mResultData = null;
    public static MediaProjectionManager mMediaProjectionManager1 = null;
    private WindowManager mWindowManager1 = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;


    /**
     * 显示相关控件
     */
    private ImageView mFloatImage;
    private TextView mTexyPkg1;
    private TextView mTexyPkg2;
    private TextView mTexyPkg3;
    private TextView mTexyPkg4;
    private TextView mTexyPkg5;


    @Override
    public IBinder onBind(Intent intent) {
        return new PopupBinder();
    }

    public class PopupBinder extends Binder {
        public PopupService getService() {
            return PopupService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initFloatingWindow();
        createVirtualEnvironment();
    }


    private void initFloatingWindow() {
        mFloatView = LayoutInflater.from(this).inflate(R.layout.float_ball, null);
        mPopupView = LayoutInflater.from(this).inflate(R.layout.popup, null);
        mShowPkgView = LayoutInflater.from(this).inflate(R.layout.show_pkg, null);

        mFloatImage = (ImageView) mFloatView.findViewById(R.id.id_iv);

        mIvpkgname = (ImageView) mPopupView.findViewById(R.id.id_pop_show_pkgname);
        mIvAllPkgname = (ImageView) mPopupView.findViewById(R.id.id_pop_show_all_pkgname);
        mIvScreenShot = (ImageView) mPopupView.findViewById(R.id.id_pop_screenshot);
        mIvFunc4 = (ImageView) mPopupView.findViewById(R.id.id_pop_function4);

        mTexyPkg1 = (TextView) mShowPkgView.findViewById(R.id.id_pkg_1);
        mTexyPkg2 = (TextView) mShowPkgView.findViewById(R.id.id_pkg_2);
        mTexyPkg3 = (TextView) mShowPkgView.findViewById(R.id.id_pkg_3);
        mTexyPkg4 = (TextView) mShowPkgView.findViewById(R.id.id_pkg_4);
        mTexyPkg5 = (TextView) mShowPkgView.findViewById(R.id.id_pkg_5);


        mIvpkgname.setOnClickListener(this);
        mIvAllPkgname.setOnClickListener(this);
        mIvScreenShot.setOnClickListener(this);
        mIvFunc4.setOnClickListener(this);

        mFloatingWindow = FloatingView.getInstance(this);//单例模式构造
        mFloatingWindow.setFloatingView(mFloatView);
        mFloatingWindow.setPopupView(mPopupView);
        mFloatingWindow.setShowPkgView(mShowPkgView);
    }

    private void createVirtualEnvironment(){
        mMediaProjectionManager1 = (MediaProjectionManager)getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager1 = (WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager1.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager1.getDefaultDisplay().getHeight();
        metrics = new DisplayMetrics();
        mWindowManager1.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565
    }

    public void show() {
        if(null != mFloatingWindow)
            mFloatingWindow.show();
    }

    public void dimiss() {
        if(null != mFloatingWindow){
            mFloatingWindow.dismiss();
        }
    }


    /**
     * 获取程序的名字
     */
    public String getAppName(String packname, View v){
        PackageManager pm = v.getContext().getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(packname, 0);
            return info.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }


    public String getpkg(String pkginfo){
        String[] strarr = pkginfo.split("[/]");
        return strarr[0];
    }


    public void ShowSinglePkg(View v){
        String actinfo = getAppName(getpkg(getPopup()), v) + "-" + getPopup();
        copyClipboard(actinfo);
        mTexyPkg1.setText(actinfo);
        mTexyPkg2.setText("");
        mTexyPkg3.setText("");
        mTexyPkg4.setText("");
        mTexyPkg5.setText("");
        mFloatingWindow.showPkgView();
    }


    public void ShowAllPkg(View v){
        HashMap hm = getAllPopup();
        String pkginfo1 = getAppName(getpkg(hm.get("pkg1").toString()), v) + "-" + hm.get("pkg1");
        String pkginfo2 = getAppName(getpkg(hm.get("pkg2").toString()), v) + "-" + hm.get("pkg2");
        String pkginfo3 = getAppName(getpkg(hm.get("pkg3").toString()), v) + "-" + hm.get("pkg3");
        String pkginfo4 = getAppName(getpkg(hm.get("pkg4").toString()), v) + "-" + hm.get("pkg4");
        String pkginfo5 = getAppName(getpkg(hm.get("pkg5").toString()), v) + "-" + hm.get("pkg5");

        copyClipboard(pkginfo1 + "\n" + pkginfo2 + "\n" + pkginfo3 + "\n" + pkginfo4 + "\n" + pkginfo5);

        mTexyPkg1.setText(pkginfo1);
        mTexyPkg2.setText(pkginfo2);
        mTexyPkg3.setText(pkginfo3);
        mTexyPkg4.setText(pkginfo4);
        mTexyPkg5.setText(pkginfo5);

        mFloatingWindow.showPkgView();
    }

    public void startScreenShot(){
        mFloatingWindow.turnMini();
        mFloatingWindow.mFloatingView.setVisibility(View.INVISIBLE);

        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {
                //start virtual
                Log.d(TAG, "run startVirtual ......");
                startVirtual();
            }
        }, 500);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {
                Log.d(TAG, "run startCapture ......");
                startCapture();
            }
        }, 1500);

        Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            public void run() {
                Log.d(TAG, "run stopVirtual ......");
                mFloatingWindow.mFloatingView.setVisibility(View.VISIBLE);
                stopVirtual();
            }
        }, 1000);
    }

    public void startVirtual(){
        Log.d(TAG, "in startVirtual ......");
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    private void virtualDisplay(){
        Log.d(TAG, "in virtualDisplay ......");
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    public void setUpMediaProjection(){
        Log.d(TAG, "in setUpMediaProjection ......");
        try{
            mResultData = ((StubApplication)getApplication()).getIntent();
            mResultCode = ((StubApplication)getApplication()).getResult();
            mMediaProjectionManager1 = ((StubApplication)getApplication()).getMediaProjectionManager();
            mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData);
            //  此处会报错：Media projections require a foreground service of type ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            //  需要将targetSdkVersion 设置为29以下
        }catch(Exception e){
            Log.d(TAG, "in setUpMediaProjection error : " + e);
        }
    }

    private void startCapture() {
        Log.d(TAG, "in startCapture ......");
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        strDate = dateFormat.format(new java.util.Date());
        nameImage = "/sdcard/Download/"+ strDate + ".png";
        Log.d("yooha", "image path -> "+ nameImage);
        Image image = mImageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();

        if (bitmap != null) {
            try {
                File fileImage = new File(nameImage);
                if (!fileImage.exists()) {
                    fileImage.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(fileImage);
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(fileImage);
                    media.setData(contentUri);
                    this.sendBroadcast(media);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopVirtual() {
        Log.d(TAG, "in stopVirtual ......");
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.id_pop_show_pkgname: //功能按钮一
                Log.d("yooha", "id_pop_show_pkgname" );
                ShowSinglePkg(v);
                break;
            case R.id.id_pop_show_all_pkgname:  //功能按钮二
                Log.d("yooha", "id_pop_show_all_pkgname");
                ShowAllPkg(v);
                break;
            case R.id.id_pop_screenshot:  //功能按钮三
                Log.d("yooha", "id_pop_screenshot");
                startScreenShot();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mFloatingWindow.mContext, "截图已保存至 /sdcard/Download/", Toast.LENGTH_LONG).show();
                    }
                }, 2000);
                break;
            case R.id.id_pop_function4:   //功能按钮四
                Log.d("yooha", "id_pop_function4 -> " + this);
                Toast.makeText(this, "预留功能接口", Toast.LENGTH_LONG).show();
                mFloatingWindow.turnMini();
                break;
            default:
                break;
        }
    }


    /**
     * 单例模式 防止出现多个悬浮球
     */
    public HashMap getAllPopup(){
        HashMap hm=new HashMap();
        SharedPreferences sp = getSharedPreferences("monitor", 0);
        hm.put("pkg1", sp.getString("pkg1", ""));
        hm.put("pkg2", sp.getString("pkg2", ""));
        hm.put("pkg3", sp.getString("pkg3", ""));
        hm.put("pkg4", sp.getString("pkg4", ""));
        hm.put("pkg5", sp.getString("pkg5", ""));
        return hm;
    }
    public String getPopup(){
        SharedPreferences sp = getSharedPreferences("monitor", 0);
        return sp.getString("pkg5", "");
    }


    /**
     * 复制到剪切板
     */
    public void copyClipboard(String content) {
        ClipboardManager myClipboard = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
        ClipData primaryClip = ClipData.newPlainText("text", content);
        assert myClipboard != null;
        myClipboard.setPrimaryClip(primaryClip);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        tearDownMediaProjection();
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }
}

