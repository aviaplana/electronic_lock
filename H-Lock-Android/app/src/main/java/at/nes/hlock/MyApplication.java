package at.nes.hlock;

import android.app.Application;
import android.os.Bundle;

import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

/**
 * Created by Andraz Pajtler on 27/01/15.
 */
public class MyApplication extends Application implements BootstrapNotifier {
    private static final String TAG = ".MyApplication";
    private RegionBootstrap regionBootstrap;

    public static final String SHARED_PREF_FIRST_RUN = "firstRun";

    @Override
    public void onCreate() {
        super.onCreate();

        // wake up the app when any beacon is seen (you can specify specific id filers in the parameters below)
//        Region region = new Region("com.example.myapp.boostrapRegion", null, null, null);
//        regionBootstrap = new RegionBootstrap(this, region);
    }

    @Override
    public void didEnterRegion(Region region) {

    }

    @Override
    public void didExitRegion(Region region) {

    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }
}
