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
        String temp = num2num00(number);
        if (temp.equals("0.00")) {
            return temp;
        }
        DecimalFormat df = new DecimalFormat("#,###.00");
        return df.format(Double.valueOf(temp));
    }
}
