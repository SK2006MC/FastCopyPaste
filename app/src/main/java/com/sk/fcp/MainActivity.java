package com.sk.fcp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.sk.fcp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

	ActivityMainBinding binding;
	ActivityResultLauncher<Intent> activityResultLauncher;
    private static final int PERMISSION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    binding = ActivityMainBinding.inflate(getLayoutInflater());
	    setContentView(binding.getRoot());

	    Button startButton = binding.startFloatingButton;
	    startButton.setOnClickListener(v -> {
		    if (checkOverlayPermission()) {
			    startFloatingWindowService();
		    } else {
			    requestOverlayPermission();
		    }
	    });

	    binding.b1.setOnClickListener(v -> {
		    Intent intent = new Intent(this, FloatingTextService.class);
		    startService(intent);
		    finish();
	    });

	    activityResultLauncher = registerForActivityResult(
			    new ActivityResultContracts.StartActivityForResult()
			    , r -> {
				    //pass
			    }
	    );
    }

    private boolean checkOverlayPermission() {
	    return Settings.canDrawOverlays(this);
    }

    private void requestOverlayPermission() {
	    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
			    Uri.parse("package:" + getPackageName()));
	    startActivityForResult(intent, PERMISSION_REQUEST_CODE);
//	    activityResultLauncher.launch(intent);
	    Toast.makeText(this, R.string.overlay_permission_request_message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
	        if (Settings.canDrawOverlays(this)) {
		        startFloatingWindowService();
	        } else {
		        Toast.makeText(this, R.string.overlay_permission_denied, Toast.LENGTH_SHORT).show();
	        }
        }
    }

    private void startFloatingWindowService() {
        Intent serviceIntent = new Intent(this, FloatingWindowService.class);
        startService(serviceIntent);
	    finish();
    }
}
