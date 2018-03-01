package com.wuxiaosu.fakebalance.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by su on 2018/2/27.
 */

public class NumberUtils {

    /**
     * ex: 5.2 -> 5.20
     *
     * @param number
     * @return
     */
    public static String num2num00(String number) {
        BigDecimal b = new BigDecimal(number);
        return String.valueOf(
                b.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP));
    }

    /**
     * ex: 2018.2 -> 2,018.20
     *
     * @param number
     * @return
     */
    public static String num2num00WithComma(String number) {
        DecimalFormat df = new DecimalFormat("#,###.00");
        String result = df.format(Double.valueOf(num2num00(number)));
        if (result.startsWith(".")) {
            result = "0" + result;
        }
        return result;
    }
}
