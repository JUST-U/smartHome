package cn.edu.zstu.smarthome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import et.song.jni.ir.IRType;
import cn.edu.zstu.smarthome.base.IrKey;

public class IrTypeActivity extends AppCompatActivity {

    private static final String TAG = "IrTypeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_type);
        ButterKnife.bind(this);


    }

    @OnClick({
            R.id.ir_type_tv,
            R.id.ir_type_air,
            R.id.ir_type_fans
    })
    public void onClick(View view) {

        Intent brandIntent = new Intent(IrTypeActivity.this, IrBrandActivity.class);
        switch (view.getId()) {
            case R.id.ir_type_tv: {
                brandIntent.putExtra(IrKey.IR_KEY_TYPE, IRType.DEVICE_REMOTE_TV);
            }
            break;

            case R.id.ir_type_fans: {
                brandIntent.putExtra(IrKey.IR_KEY_TYPE, IRType.DEVICE_REMOTE_FANS);
            }
            break;

            case R.id.ir_type_air: {
                brandIntent.putExtra(IrKey.IR_KEY_TYPE, IRType.DEVICE_REMOTE_AIR);
            }
            break;
        }

        startActivity(brandIntent);
    }
}