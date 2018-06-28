package cn.edu.zstu.smarthome.base;

import java.util.Comparator;
import java.util.Locale;

/**
 * @author LiangLiang.Dong<liangl.dong@qq.com>
 * @since 2018/6/25
 */

public class PinyinComparator implements Comparator<AdapterPYinItem> {

    public int compare(AdapterPYinItem o1, AdapterPYinItem o2) {
        return o1.getPyin().toUpperCase(Locale.getDefault())
                .compareTo(o2.getPyin().toUpperCase(Locale.getDefault()));
    }
}
