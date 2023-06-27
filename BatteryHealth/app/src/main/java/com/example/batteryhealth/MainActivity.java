package com.example.batteryhealth;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView batteryPercentageTextView;
    private TextView batteryStatusTextView;
    private TextView chargingStatusTextView;
    private TextView batteryHealthTextView;
    private int chargingCycleCount = 0;
    private int maxBatteryCapacity = 4500; // Placeholder value, replace with actual battery capacity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryPercentageTextView = findViewById(R.id.batteryPercentageTextView);
        batteryStatusTextView = findViewById(R.id.batteryStatusTextView);
        chargingStatusTextView = findViewById(R.id.chargingStatusTextView);
        batteryHealthTextView = findViewById(R.id.batteryHealthTextView);

        // Register a receiver to monitor battery level and charging status changes
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(batteryReceiver, filter);
    }

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {
                if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                    int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int batteryPercentage = (batteryLevel * 100) / batteryScale;
                    batteryPercentageTextView.setText(String.format("%d%%", batteryPercentage));

                    int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    String statusText = getBatteryStatusText(batteryStatus);
                    batteryStatusTextView.setText(statusText);

                    int chargingStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    String chargingText = getChargingStatusText(chargingStatus);
                    chargingStatusTextView.setText(chargingText);
                } else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                    chargingCycleCount++;
                }
            }

            // Update battery health estimation
            float batteryHealthEstimation = calculateBatteryHealthEstimation();
            batteryHealthTextView.setText(String.format("%.1f%%", batteryHealthEstimation));
        }
    };

    private String getBatteryStatusText(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL:
                return "Full";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return "Not Charging";
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
            default:
                return "Unknown";
        }
    }

    private String getChargingStatusText(int status) {
        switch (status) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return "Plugged to AC";
            case BatteryManager.BATTERY_PLUGGED_USB:
                return "Plugged to USB";
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return "Plugged Wirelessly";
            default:
                return "Not Plugged";
        }
    }

    private float calculateBatteryHealthEstimation() {
        // Calculate battery health estimation based on charging cycles and remaining capacity
        float remainingCapacity = maxBatteryCapacity - (chargingCycleCount * (maxBatteryCapacity / 100f));
        float batteryHealthEstimation = (remainingCapacity / maxBatteryCapacity) * 100;

        return batteryHealthEstimation;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the battery receiver to avoid memory leaks
        unregisterReceiver(batteryReceiver);
    }
}
