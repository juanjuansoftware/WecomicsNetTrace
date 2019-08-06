package com.net.wecomicsnettrace;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.LDNetDiagnoService.LDNetDiagnoListener;
import com.netease.LDNetDiagnoService.LDNetDiagnoService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LDNetDiagnoListener {
    private EditText hostEt;
    private Button statrButton;
    private ScrollView infoSl;
    private TextView info;

    private LDNetDiagnoService ldNetDiagnoService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hostEt = findViewById(R.id.host_et);
        statrButton = findViewById(R.id.start_bt);
        infoSl = findViewById(R.id.info_sl);
        info = findViewById(R.id.info);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        setTitle("网络情况扫描");
        statrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = hostEt.getText().toString();
                if (host != null && !host.isEmpty()) {
                    showOrHide(getApplicationContext(), MainActivity.this);
                    info.setText("");
                    List<String> hosts = new ArrayList<>();
                    hosts.add(host.replaceAll("/", ""));
                    ldNetDiagnoService = new LDNetDiagnoService(getApplicationContext(),
                            "", "", "", "", "", MainActivity.this);
                    ldNetDiagnoService.setIfUseJNICTrace(true);
                    ldNetDiagnoService.execute(hosts.toArray(new String[hosts.size()]));
                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int i = 0;
        while (i < permissions.length) {
            Log.e("aa", "permission:" + permissions[i] + " grantResult:" + grantResults[i]);
            i++;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ldNetDiagnoService != null) {
            ldNetDiagnoService.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /**
         * 此方法用于初始化菜单，其中menu参数就是即将要显示的Menu实例。 返回true则显示该menu,false 则不显示;
         * (只会在第一次初始化菜单时调用) Inflate the menu; this adds items to the action bar
         * if it is present.
         */
        getMenuInflater().inflate(R.menu.menu_net_info, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menuCopy == item.getItemId()) {
            ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("text", info.getText().toString()));
            Toast.makeText(getApplicationContext(), "复制成功",
                    Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void OnNetDiagnoFinished(String log) {
        infoSl.fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    public void OnNetDiagnoUpdated(String log) {
        if (info != null) {
            info.append(log);
            infoSl.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public static void showOrHide(Context context, Activity activity) {
        try {
            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {

        }

    }
}
