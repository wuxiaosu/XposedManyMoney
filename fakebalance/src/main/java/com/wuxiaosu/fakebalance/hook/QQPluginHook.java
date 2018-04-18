package com.wuxiaosu.fakebalance.hook;

import android.widget.TextView;

import com.wuxiaosu.fakebalance.BuildConfig;
import com.wuxiaosu.fakebalance.util.NumberUtils;
import com.wuxiaosu.widget.SettingLabelView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


/**
 * Created by su on 2018/3/12.
 * QQ hook
 */

public class QQPluginHook {
    private static XSharedPreferences xsp;

    private static boolean fakeBalance;
    private static String balance;

    private String qvipPayAccountActivityCallbackClazzName;

    public QQPluginHook(String versionName) {
        switch (versionName) {
            case "7.3.8":
            case "7.5.0":
                qvipPayAccountActivityCallbackClazzName = "ddk";
                break;
            default:
            case "7.5.5":
            case "7.5.8":
                qvipPayAccountActivityCallbackClazzName = "ddw";
                break;
        }
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
        xsp.makeWorldReadable();
    }

    private void reload() {
        xsp.reload();
        balance = NumberUtils.num2num00(xsp.getString("tenpay", "0.00"));
        fakeBalance = xsp.getBoolean("fake_tenpay", false);
    }

    public void hook(ClassLoader classLoader) {
        try {
            Class clazz = XposedHelpers.findClass(qvipPayAccountActivityCallbackClazzName, classLoader);
            XposedBridge.hookAllMethods(clazz, "a",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            reload();
                            if (fakeBalance) {
                                Object a = XposedHelpers.getObjectField(param.thisObject, "a");
                                TextView textView = (TextView) (XposedHelpers.getObjectField(a, "ah"));
                                textView.setText(balance);
                            }
                            super.afterHookedMethod(param);
                        }
                    });
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }
}
