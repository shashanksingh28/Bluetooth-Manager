import android.app.Application;
import android.content.Intent;

/*
 * Contains global adapters and other state variables required
 * Initialzes the service
 */
public class BluetoothManagerApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		startService(new Intent(this,BluetoothManagerService.class));
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		stopService(new Intent(this,BluetoothManagerService.class));
	}
	
	
	

}
