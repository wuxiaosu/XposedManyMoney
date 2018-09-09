package com.wuxiaosu.fakebalance.hook;

import com.wuxiaosu.fakebalance.BuildConfig;
import com.wuxiaosu.fakebalance.util.NumberUtils;
import com.wuxiaosu.widget.SettingLabelView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;


/**
 * Created by su on 2018/3/12.
 * QQ hook
 */

public class QQHook {
    private static XSharedPreferences xsp;

    private static boolean fakeBalance;
    private static String balance;

    private static void reload() {
        xsp.reload();
        balance = NumberUtils.num2num00(xsp.getString("tenpay", "0.00"));
        fakeBalance = xsp.getBoolean("fake_tenpay", false);
    }

    public static void hook(ClassLoader classLoader) {
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
        xsp.makeWorldReadable();
        try {
            Class numAnimClazz = XposedHelpers.findClass("com.tencent.mobileqq.activity.qwallet.widget.NumAnim", classLoader);
            XposedHelpers.findAndHookMethod(numAnimClazz, "run",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object rulerObject = XposedHelpers.getObjectField(param.thisObject, "mRuler");
                            XposedHelpers.findAndHookMethod(rulerObject.getClass(), "getNumber", double.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    reload();
                                    if (fakeBalance) {
                                        param.args[0] = Double.valueOf(balance);
                                    }
                                    super.beforeHookedMethod(param);
                                }
                            });
                            super.beforeHookedMethod(param);
                        }
                    });

        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }
}
