import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationHelper {

    public interface OnLocationChangeListener {
        void onLocationChanged(Location location);
    }

    private LocationManager locationManager;
    private OnLocationChangeListener locationChangeListener;

    public LocationHelper(Context context, OnLocationChangeListener listener) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationChangeListener = listener;
    }

    public void requestLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public Location getLastKnownLocation() {
        try {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (locationChangeListener != null) {
                locationChangeListener.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
}
