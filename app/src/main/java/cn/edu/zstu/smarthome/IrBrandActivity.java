package cn.edu.zstu.smarthome;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.zstu.smarthome.base.AdapterBrandList;
import cn.edu.zstu.smarthome.base.AdapterPYinItem;
import et.song.jni.ir.IRType;
import cn.edu.zstu.smarthome.base.IrKey;
import cn.edu.zstu.smarthome.base.PinyinComparator;
import cn.edu.zstu.smarthome.base.SideBar;
import cn.edu.zstu.smarthome.common.PingYinUtil;

public class IrBrandActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "IrBrandActivity";

    @BindView(R.id.ir_brand_list_view)
    ListView mListView;
    @BindView(R.id.ir_brand_list_side_bar)
    SideBar mSideBar;
    @BindView(R.id.ir_brand_list_side_bar_text)
    TextView mSideBarText;

    private int mIrType = 0;
    private List<AdapterPYinItem> items = new ArrayList<>();
    private AdapterBrandList mAdapter;

    private IrBrandTask mBrandTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_brand);
        ButterKnife.bind(this);

        mSideBar.setListView(mListView);
        mSideBar.setTextView(mSideBarText);
        mListView.setOnItemClickListener(this);

        mIrType = getIntent().getIntExtra(IrKey.IR_KEY_TYPE, 0);
        Log.d(TAG, "[Ir]: type " + mIrType);

        if (mIrType == 0) {
            finish();
            return;
        }

        mBrandTask = new IrBrandTask();
        mBrandTask.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "[Ir]: position " + position);
        AdapterPYinItem item = (AdapterPYinItem) parent
                .getItemAtPosition(position);

        Intent codeIntent = new Intent(IrBrandActivity.this, IrCodeActivity.class);
        codeIntent.putExtra(IrKey.IR_KEY_TYPE, mIrType)
                .putExtra(IrKey.IR_KEY_NAME, item.getName())
                .putExtra(IrKey.IR_KEY_INDEX, item.getPos());
        startActivity(codeIntent);

    }

    private class IrBrandTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            String[] brands = getBrands(mIrType);
            int _index = 0;
            for (String brand : brands) {
                String brandPinyin = PingYinUtil.getPingYin(brand);
                Log.d(TAG, "[Ir]: type " + brand + "-" + brandPinyin);
                items.add(new AdapterPYinItem(brand, brandPinyin, _index));
                _index++;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (items.isEmpty()) {
                return;
            }
            Collections.sort(items, new PinyinComparator());
            mAdapter = new AdapterBrandList(getApplicationContext(), items);
            mListView.setAdapter(mAdapter);
        }
    }

    private String[] getBrands(int irType) {
        return getResources().getStringArray(irType
                == IRType.DEVICE_REMOTE_AIR ? R.array.strs_air_brand : (irType
                == IRType.DEVICE_REMOTE_TV ? R.array.strs_tv_brand : (irType
                == IRType.DEVICE_REMOTE_FANS ? R.array.strs_fans_brand : 0)));
    }
}
