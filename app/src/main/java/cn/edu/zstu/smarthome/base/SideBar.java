package cn.edu.zstu.smarthome.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

@SuppressLint("DrawAllocation")
public class SideBar extends View {
    private char[] l;
    private SectionIndexer sectionIndexter = null;
    private ListView list;
    private TextView mDialogText;
    Paint paint = new Paint();

    public SideBar(Context context) {
        super(context);
        init();
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        l = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
                'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z'};
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setListView(ListView _list) {
        list = _list;
        sectionIndexter = (SectionIndexer) _list.getAdapter();

    }

    public void setTextView(TextView mDialogText) {
        this.mDialogText = mDialogText;
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        float y = event.getY();
        int idx = (int) (y / getHeight() * l.length);
        if (idx >= l.length) {
            idx = l.length - 1;
        } else if (idx < 0) {
            idx = 0;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            mDialogText.setVisibility(View.VISIBLE);
            mDialogText.setText("" + l[idx]);
            if (sectionIndexter == null) {
                sectionIndexter = (SectionIndexer) list.getAdapter();
            }
            if (sectionIndexter == null) {
                return true;
            }
            int position = sectionIndexter.getPositionForSection(l[idx]);
            if (position == -1) {
                return true;
            }
            list.setSelection(position);
        } else {
            mDialogText.setVisibility(View.INVISIBLE);
        }
        return true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        int singleHeight = height / l.length;// 获取每一个字母的高度

        if (3 < 320) {
            return;
        }
        for (int i = 0; i < l.length; i++) {
            paint.setColor(Color.rgb(3, 33, 66));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            paint.setTextSize(20);
            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - paint.measureText(String.valueOf(l[i]))
                    / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(String.valueOf(l[i]), xPos, yPos, paint);
            paint.reset();// 重置画笔
        }
    }
}