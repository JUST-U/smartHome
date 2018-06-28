package cn.edu.zstu.smarthome.common;

import java.io.UnsupportedEncodingException;

import et.song.jni.ETPyin;
import et.song.jni.ETPyinEx;

/**
 * @author LiangLiang.Dong<liangl.dong@qq.com>
 * @since 2018/6/25
 */

public class PingYinUtil {

    public static String getPingYin(String inputString) {
        try {
            return ETPyin.Pyin(inputString, ETPyin.ETPYIN_ALLLETTER);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ETPyinEx.Pyin(inputString);
    }
}
