package com.nuopushi.sys.app.agedcare.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


import com.nuopushi.sys.app.agedcare.R;
import com.nuopushi.sys.app.agedcare.ftp.Ftp;
import com.nuopushi.sys.app.agedcare.ftp.FtpListener;
import com.nuopushi.sys.app.agedcare.tools.VersionXmlParse;
import com.nuopushi.sys.app.agedcare.view.CustomProgressDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nuopushi.sys.app.agedcare.model.Constant.FTP_DOWN_LOADING;
import static com.nuopushi.sys.app.agedcare.model.Constant.FTP_DOWN_SUCCESS;


/**
 * Author chzjy
 * Date 2016/12/19.
 */

public class Main extends Activity {
    private final String TAG = getClass().getName();
    @Bind(R.id.soft_update)
    Button softUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        ButterKnife.bind(this);
        SdCard= Environment.getExternalStorageDirectory().getAbsolutePath();
        apkPath=SdCard+"/AgedCare/download/apk/";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    //Ftp对象
    Ftp mFtp;
    //版本信息下载监听器
    FtpListener Dversion = new FtpListener() {
        @Override
        public void onStateChange(String currentStep) {
            Log.i(TAG, currentStep);
        }

        @Override
        public void onUploadProgress(String currentStep, long uploadSize, File targetFile) {

        }

        @Override
        public void onDownLoadProgress(String currentStep, long downProcess, File targetFile) {
            if (currentStep.equals(FTP_DOWN_SUCCESS)) {
                Log.i(TAG, currentStep);
            } else if (currentStep.equals(FTP_DOWN_LOADING)) {
                Log.i(TAG, "-----下载---" + downProcess + "%");
            }
        }

        @Override
        public void onDeleteProgress(String currentStep) {

        }
    };
    //版本apk下载监听器
    FtpListener Dapk=new FtpListener() {
        @Override
        public void onStateChange(String currentStep) {

        }

        @Override
        public void onUploadProgress(String currentStep, long uploadSize, File targetFile) {

        }

        @Override
        public void onDownLoadProgress(String currentStep, long downProcess, File targetFile) {
            Log.d(TAG, currentStep);
            if (currentStep.equals(FTP_DOWN_SUCCESS)) {
                downloadDialog.dismiss();
                Message message = new Message();
                message.what = 0x0002;
                handler.sendMessage(message);
            }
            if (currentStep.equals(FTP_DOWN_LOADING)) {
                downloadDialog.setProgress((int) downProcess);
            }
        }

        @Override
        public void onDeleteProgress(String currentStep) {

        }
    };
    //手机内存卡路径
    String SdCard;
    //当前版本
    String version;
    //FTP上的版本
    String FtpVersion;
    //用于版本xml解析
    HashMap<String, String> versionHashMap = new HashMap<>();
    //进度条
    CustomProgressDialog loading;
    //进度条消失类型
    String result;
    //下载进度条
    ProgressDialog downloadDialog;
    //apk路径
    String apkPath;
    @OnClick(R.id.soft_update)
    public void onClick() {
        result="Finished";
        loading=new CustomProgressDialog(this);
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        loading.show();
        //初始化FTP
        mFtp = new Ftp("101.69.255.132", 21, "ftpall", "123456", Dversion);
        //获取当前版本号
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo pi = packageManager.getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        new Thread(checkVersion).start();
    }

    Runnable checkVersion = new Runnable() {
        @Override
        public void run() {
            try {
                //下载版本信息xml文件
                mFtp.download("/apk/version_AgedCare.xml", SdCard + "/AgedCare/version/");
                File xml = new File(SdCard + "/AgedCare/version/version_AgedCare.xml");
                InputStream inputStream = new FileInputStream(xml);
                //解析xml文件
                versionHashMap = VersionXmlParse.parseXml(inputStream);
            } catch (Exception e) {
                result=e.getMessage();
            }
            //获取ftp上的版本号
            FtpVersion = versionHashMap.get("version");
            //根据result显示相应的对话框
            showVersionDialog(version,FtpVersion,result);
        }
    };

    private void showVersionDialog(String currentVersion, final String FtpVersion, final String result) {
        //取消进度条
        loading.dismiss();
        if (result.equals("Finished")) {
            Log.i(TAG, "当前版本为 " + version + "FTP上版本为 " + FtpVersion);
            if (!currentVersion.equals(FtpVersion)) {
                //版本不一致
                Message message = new Message();
                message.what = 0x0001;
                handler.sendMessage(message);
            } else {
                //版本一致
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog dialog = new AlertDialog.Builder(Main.this)
                                .setTitle("当前为最新版本")
                                .setPositiveButton("确定", null)
                                .create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                });
            }
        }else {
            //失败
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Main.this, result, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    //下载完成
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x0001:
                    AlertDialog dialog = new AlertDialog.Builder(Main.this)
                            .setTitle("有新版本")
                            .setMessage("当前版本为" + version + ",新版本为" + FtpVersion)
                            .setPositiveButton("下载并安装", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    downloadDialog = new ProgressDialog(Main.this);
                                    downloadDialog.setTitle("下载进度");
                                    downloadDialog.setMessage("已经下载了");
                                    downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    downloadDialog.setCancelable(false);
                                    downloadDialog.setIndeterminate(false);
                                    downloadDialog.setMax(100);
                                    downloadDialog.show();
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mFtp.setListener(Dapk);
                                            try {
                                                mFtp.download(versionHashMap.get("path"),apkPath);
                                            } catch (final Exception e) {
                                                downloadDialog.dismiss();
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(Main.this, "网络连接失败,请检查网络或重试", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }.start();
                                }
                            })
                            .setNegativeButton("取消", null).create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    break;
                case 0x0002:
                    //apk文件路径
                    String localApkPath = apkPath+versionHashMap.get("name");
                    File file = new File(localApkPath);
                    if (file.exists()){
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //设置intent的Action属性
                        intent.setAction(Intent.ACTION_VIEW);
                        //设置intent的data和Type属性。
                        intent.setDataAndType(Uri.fromFile(file),
                                "application/vnd.android.package-archive");
                        startActivity(intent);
                    }
                    break;
            }



        }
    };
}
