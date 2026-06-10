package com.qx.qylogsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qx.qylog.CanLog;
import com.qx.qylog.DeviceLog;
import com.qx.qylog.GpsLog;
import com.qx.qylog.XlogChannelManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements View.OnClickListener {
    private TextView statusView;
    private int canCounter;
    private int gpsCounter;
    private int deviceCounter;
    private boolean canClosed;

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("qylog-sample");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusView = (TextView) findViewById(R.id.status_text);
        initLogs();
        bindButtons();
        updateStatus("initialized, " + nativeSmokeTest());
    }

    private native String nativeSmokeTest();

    private void initLogs() {
        CanLog.init(this);
        GpsLog.init(this);
        DeviceLog.init(this);
        canClosed = false;
    }

    private void bindButtons() {
        bind(R.id.write_can);
        bind(R.id.write_gps);
        bind(R.id.write_device);
        bind(R.id.write_bytes);
        bind(R.id.flush_all);
        bind(R.id.close_reopen_can);
    }

    private void bind(int id) {
        Button button = (Button) findViewById(id);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.write_can) {
            writeCanText();
        } else if (id == R.id.write_gps) {
            writeGpsText();
        } else if (id == R.id.write_device) {
            writeDeviceText();
        } else if (id == R.id.write_bytes) {
            writeCanBytes();
        } else if (id == R.id.flush_all) {
            XlogChannelManager.flushAllSync();
            updateStatus("flushed all");
        } else if (id == R.id.close_reopen_can) {
            toggleCanChannel();
        }
    }

    private void writeCanText() {
        if (canClosed) {
            CanLog.init(this);
            canClosed = false;
        }
        canCounter++;
        CanLog.i("engine", "rpm=%d speed=%d", 1200 + canCounter, 8 + canCounter);
        updateStatus("wrote CAN text " + canCounter);
    }

    private void writeGpsText() {
        gpsCounter++;
        GpsLog.i("location", "lat=%.6f,lng=%.6f", 31.2304 + gpsCounter * 0.001, 121.4737);
        updateStatus("wrote GPS text " + gpsCounter);
    }

    private void writeDeviceText() {
        deviceCounter++;
        DeviceLog.i("state", "hydraulic=%s battery=%d", deviceCounter % 2 == 0 ? "on" : "off", 80 + deviceCounter);
        updateStatus("wrote DEVICE text " + deviceCounter);
    }

    private void writeCanBytes() {
        byte[] frame = new byte[] {(byte) 0x88, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55};
        CanLog.bytes("can_frame", frame);
        updateStatus("wrote CAN binary bytes=" + frame.length);
    }

    private void toggleCanChannel() {
        if (canClosed) {
            CanLog.init(this);
            canClosed = false;
            updateStatus("reopened CAN");
        } else {
            CanLog.close();
            canClosed = true;
            updateStatus("closed CAN");
        }
    }

    private void updateStatus(String action) {
        String day = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        File externalFilesDir = getExternalFilesDir(null);
        File logRoot = externalFilesDir != null ? externalFilesDir : getFilesDir();
        File logDir = new File(logRoot, "qylog");
        File cacheDir = new File(getCacheDir(), "qylog");
        String text = action
                + "\n\nnative backend:\n" + (XlogChannelManager.isNativeBackendAvailable() ? "marsxlog loaded" : "java fallback (marsxlog missing)")
                + "\n\nlogDir:\n" + logDir.getAbsolutePath()
                + "\n\ncacheDir:\n" + cacheDir.getAbsolutePath()
                + "\n\nexpected files:"
                + "\n" + new File(logDir, "CAN_BUS_" + day + ".xlog").getAbsolutePath()
                + "\n" + new File(logDir, "GPS_" + day + ".xlog").getAbsolutePath()
                + "\n" + new File(logDir, "DEVICE_" + day + ".xlog").getAbsolutePath()
                + "\n" + new File(cacheDir, "CAN_BUS.mmap3").getAbsolutePath();
        statusView.setText(text);
    }
}
