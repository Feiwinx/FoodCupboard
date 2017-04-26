package sammobewick.pocketkitchen.communication;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.drive.Metadata;

/**
 * Delete Drive Activity.
 * Allows users to delete their Drive data from in-app (instead of from the web Drive client).
 *
 * Runs in a new activity - which is arguable strange, but shouldn't matter as the setting is
 * infrequent really.
 *
 * Created by Sam on 26/04/2017.
 */
public class DeleteDriveActivity extends BaseDriveActivity {
    private static final String TAG = "DeleteDriveAct";
    private boolean errors;
    private int     result_count;

    // TODO: All of this class!

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        errors = false;
        result_count = 0;
        super.onConnected(bundle);
    }

    @Override
    public void queryResult(boolean success, String identifier) {

    }

    @Override
    public void handleMeta(Metadata meta) {

    }
}
