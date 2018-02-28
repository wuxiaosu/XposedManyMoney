package com.wuxiaosu.fakebalance.hook;

import android.widget.TextView;

import com.wuxiaosu.fakebalance.BuildConfig;
import com.wuxiaosu.fakebalance.util.NumberUtils;
import com.wuxiaosu.widget.SettingLabelView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;


/**
 * Created by su on 2018/2/05.
 * tim hook
 */

public class TimHook {
    private static XSharedPreferences xsp;

    private static boolean fakeBalance;
    private static String balance;

    private String qvipPayWalletActivityCallbackClazzName;
    private String qvipPayAccountActivityCallbackClazzName;

    public TimHook(String versionName) {
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
        xsp.makeWorldReadable();
        switch (versionName) {
            case "2.0.0":
                qvipPayWalletActivityCallbackClazzName = "axn";
                qvipPayAccountActivityCallbackClazzName = "avr";
                break;
            case "2.0.1":
                qvipPayWalletActivityCallbackClazzName = "axn";
                qvipPayAccountActivityCallbackClazzName = "avr";
                break;
            case "2.0.5":
                qvipPayWalletActivityCallbackClazzName = "axn";
                qvipPayAccountActivityCallbackClazzName = "avr";
                break;
            case "2.1.0":
                qvipPayWalletActivityCallbackClazzName = "axo";
                qvipPayAccountActivityCallbackClazzName = "avs";
                break;
            default:
            case "2.1.5":
                qvipPayWalletActivityCallbackClazzName = "axo";
                qvipPayAccountActivityCallbackClazzName = "avs";
                break;
        }
    }

    private void reload() {
        xsp.reload();
        balance = NumberUtils.num2num00(xsp.getString("tenpay", "0.00"));
        fakeBalance = xsp.getBoolean("fake_tenpay", false);
    }

    public void hook(ClassLoader classLoader) {
        try {
            Class tenPayInfoClazz = XposedHelpers.findClass("com.qwallet.data.TenPayInfo", classLoader);
            final Class qvipPayWalletActivityClazz = XposedHelpers.findClass("com.qwallet.activity.QvipPayWalletActivity", classLoader);

            Class clazz = XposedHelpers.findClass(qvipPayWalletActivityCallbackClazzName, classLoader);
            XposedHelpers.findAndHookMethod(clazz, "a", tenPayInfoClazz,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            reload();
                            if (fakeBalance) {
                                Object activityObject = XposedHelpers.getObjectField(param.thisObject, "a");

                                TextView textView = (TextView) (XposedHelpers.getObjectField(activityObject, "d"));
                                textView.setText("￥" + balance);
                            }
                            super.afterHookedMethod(param);
                        }
                    });

            final Class qvipPayAccountActivityClazz = XposedHelpers.findClass("com.qwallet.activity.QvipPayAccountActivity", classLoader);

            clazz = XposedHelpers.findClass(qvipPayAccountActivityCallbackClazzName, classLoader);
            XposedHelpers.findAndHookMethod(clazz, "a", tenPayInfoClazz,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            reload();
                            if (fakeBalance) {
                                Object activityObject = XposedHelpers.getObjectField(param.thisObject, "a");

                                TextView textView = (TextView) XposedHelpers.callStaticMethod(qvipPayAccountActivityClazz, "h", activityObject);
                                textView.setText(balance + " 元");
                            }
                            super.afterHookedMethod(param);
                        }
                    });
        } catch (Error | Exception e) {
            e.printStackTrace();
        }

        try {
            Class clazz = XposedHelpers.findClass("com.tenpay.android.qqplugin.activity.en", classLoader);
            XposedHelpers.findAndHookMethod(clazz, "onResume",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            reload();
                            if (fakeBalance) {
                                TextView textView = (TextView) (XposedHelpers.getObjectField(param.thisObject, "b"));
                                textView.setText(balance);
                            }
                            super.afterHookedMethod(param);
                        }
                    });

            clazz = XposedHelpers.findClass("com.tenpay.android.qqplugin.activity.ep", classLoader);
            XposedHelpers.findAndHookMethod(clazz, "run",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            reload();
                            if (fakeBalance) {
                                Object en = XposedHelpers.getObjectField(param.thisObject, "a");
                                TextView textView = (TextView) (XposedHelpers.getObjectField(en, "b"));
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
