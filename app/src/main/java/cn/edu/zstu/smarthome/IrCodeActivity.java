package cn.edu.zstu.smarthome;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.threeman.android.remote.lib.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.edu.zstu.smarthome.base.IrKey;
import et.song.jni.ir.IRType;
import et.song.jni.ir.ETIR;
import et.song.jni.ir.IR;

public class IrCodeActivity extends AppCompatActivity {

    private static final String TAG = "IrCodeActivity";

    @BindView(R.id.ir_code_power)
    Button mKeyPower;
    @BindView(R.id.ir_code_pre_group)
    Button mPreGroup;
    @BindView(R.id.ir_code_next_group)
    Button mNextGroup;

    private int mKey;
    private int mRow;
    private int mType;
    private int mTotal;
    private int mCount;
    private int mIndex;
    private int[] mBrandArray;
    private byte[] mCode;
    private String mName;
    private IR mIR = null;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Service.ObserverBinder observerService;
    private ServiceConnection connecttion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            observerService = (Service.ObserverBinder) service;
            observerService.connect();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    observerService.controlDevice();
                }
            }, 1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_code);
        ButterKnife.bind(this);

        mRow = getIntent().getIntExtra(IrKey.IR_KEY_INDEX, 0);
        mType = getIntent().getIntExtra(IrKey.IR_KEY_TYPE, 0);
        mName = getIntent().getStringExtra(IrKey.IR_KEY_NAME);

        setKey();

        mIR = ETIR.Builder(mType);
        mTotal = mIR.GetBrandCount(mRow);
        mCount = 1;
        mBrandArray = mIR.GetBrandArray(mRow);
        for (int _bradn : mBrandArray) {
            Log.d(TAG, "[Ir]: brand " + _bradn);
        }
        Log.d(TAG, "[Ir]: brand codes " + mBrandArray.length);

        Intent intent = new Intent(this, Service.class);
        bindService(intent, connecttion, BIND_AUTO_CREATE);
    }

    @OnClick({
            R.id.ir_code_power,
            R.id.ir_code_fans_speed,
            R.id.ir_code_fans_yaotou,
            R.id.ir_code_next_group,
            R.id.ir_code_pre_group
    })
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.ir_code_power:
            case R.id.ir_code_fans_speed:
            case R.id.ir_code_fans_yaotou: {
                mKey = viewId ==
                        R.id.ir_code_power ? IRType.REMOTE_KEY_FANS.KEY_FANS_POWER : (viewId ==
                        R.id.ir_code_fans_speed ? IRType.REMOTE_KEY_FANS.KEY_FANS_WIND_SPEED : IRType.REMOTE_KEY_FANS.KEY_FANS_SHAKE_HEAD);
                try {
                    sendCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;

            case R.id.ir_code_next_group: {
                if (mCount < mTotal) {
                    mCount++;
                    mIndex++;
                }
                Log.d(TAG, "[Ir]: group " + mCount);
            }
            break;

            case R.id.ir_code_pre_group: {
                if (mCount > 1) {
                    mCount--;
                    mIndex--;
                }
                Log.d(TAG, "[Ir]: group " + mCount);
            }
            break;

            default:
        }
    }

    private void sendCode() throws Exception {
        if (mKey == 0) {
            Log.d(TAG, "[Ir]: key is null");
            return;
        }
        int[] brandArray = mIR.GetBrandArray(mRow);
        Log.d(TAG, "[Add]: code array[" + brandArray.length + "], count: " + mCount + ", index: " + mIndex);
        Log.d(TAG, "[Add]: paarams index:" + brandArray[mIndex] + "], key: " + mKey + ", type: " + mType);
        mCode = mIR.Search(brandArray[mIndex], mKey);
        if (mCode == null) {
            Log.d(TAG, "[Ir]: get null code");
            return;
        }
        String _code = StringUtil.BinToHex(mCode, 0, mCode.length);
        Log.d(TAG, "[Ir]: code " + _code);
        observerService.setProperty(generateCommand("ir_command", _code));
    }

    private void setKey() {
        if (mType == IRType.DEVICE_REMOTE_AIR) {
            mKey = IRType.REMOTE_KEY_AIR.KEY_AIR_POWER;
        } else if (mType == IRType.DEVICE_REMOTE_FANS) {
            mKey = IRType.REMOTE_KEY_FANS.KEY_FANS_POWER;
        } else if (mType == IRType.DEVICE_REMOTE_TV) {
            mKey = IRType.REMOTE_KEY_TV.KEY_TV_POWER;
        }
    }

    private String generateCommand(String name, String value) {
        JSONObject json = new JSONObject();
        try {
            json.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
