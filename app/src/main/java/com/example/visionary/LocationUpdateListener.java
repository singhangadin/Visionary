package com.example.visionary;

import android.location.Location;

/**<p>
 * Created by Angad on 02-01-2017.
 * </p>
 */

public interface LocationUpdateListener {
    void onLocationChanged(Location location);
}