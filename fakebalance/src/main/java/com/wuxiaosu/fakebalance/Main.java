package com.wuxiaosu.fakebalance;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.wuxiaosu.fakebalance.hook.AliPayHook;
import com.wuxiaosu.fakebalance.hook.QQHook;
import com.wuxiaosu.fakebalance.hook.QQPluginHook;
import com.wuxiaosu.fakebalance.hook.TimHook;
import com.wuxiaosu.fakebalance.hook.WeChatHook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.BaseDexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by su on 2017/12/29.
 */

public class Main implements IXposedHookLoadPackage {

    private static final String ALIPAY_PKG_NAME = "com.eg.android.AlipayGphone";
    private static final String TIM_PKG_NAME = "com.tencent.tim";
    private static final String QQ_PKG_NAME = "com.tencent.mobileqq";
    private static final String WECHAT_PKG_NAME = "com.tencent.mm";

    private static List<String> pkgList = new ArrayList<>();

    static {
        pkgList.add(ALIPAY_PKG_NAME);
        pkgList.add(TIM_PKG_NAME);
        pkgList.add(WECHAT_PKG_NAME);
        pkgList.add(QQ_PKG_NAME);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }
        final String packageName = lpparam.packageName;

        if (packageName.equals(BuildConfig.APPLICATION_ID)) {
            XposedHelpers.findAndHookMethod(BuildConfig.APPLICATION_ID + ".MainActivity", lpparam.classLoader,
                    "isModuleActive", XC_MethodReplacement.returnConstant(true));
            return;
        }

        if (pkgList.contains(packageName)) {
            XposedHelpers.findAndHookMethod(Application.class,
                    "attach",
                    Context.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Context context = (Context) param.args[0];
                            ClassLoader appClassLoader = context.getClassLoader();

                            switch (packageName) {
                                case ALIPAY_PKG_NAME:
                                    new AliPayHook(getVersionName(context, ALIPAY_PKG_NAME)).hook(appClassLoader);
                                    break;
                                case WECHAT_PKG_NAME:
                                    WeChatHook.hook(appClassLoader);
                                    break;
                                case QQ_PKG_NAME:
                                    QQHook.hook(appClassLoader);
                                    break;
                                default:
                            }
                        }
                    });

            if (packageName.equals(TIM_PKG_NAME) || packageName.equals(QQ_PKG_NAME)) {
                XposedHelpers.findAndHookConstructor("dalvik.system.BaseDexClassLoader",
                        lpparam.classLoader, String.class, File.class, String.class, ClassLoader.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {

                                if (param.args[0].toString().contains("qwallet_plugin.apk")) {
                                    ClassLoader classLoader = (BaseDexClassLoader) param.thisObject;
                                    if (packageName.equals(TIM_PKG_NAME)) {
                                        TimHook.hook(classLoader);
                                    } else {
                                        QQPluginHook.hook(classLoader);
                                    }
                                }
                            }
                        });
            }
        }
    }


    private String getVersionName(Context context, String pkgName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(pkgName, 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
