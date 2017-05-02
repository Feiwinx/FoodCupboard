package sammobewick.pocketkitchen.communication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.drive.Metadata;

import sammobewick.pocketkitchen.R;

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
    private int     result_count;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        ((TextView)findViewById(R.id.drive_text)).setText("DELETING DATA...");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        result_count = 0;
        super.onConnected(bundle);
    }

    @Override
    public void queryResult(boolean success, String identifier) {
        result_count += 1;
        if (success) {
            Log.i(TAG, "Successfully deleted " + identifier);
        } else {
            Log.i(TAG, "Failed to find fine " + identifier);
        }

        if (result_count >= getMaxRequests())
            finish();
    }

    @Override
    public void handleMeta(Metadata meta) {
        String title = meta.getTitle();

        if (meta.isTrashable()) {
            if (!meta.isTrashed()) {

                meta.getDriveId().asDriveFile().trash(getmGoogleApiClient());

            } else { Log.i(TAG, "File already trashed: " + title); }
        } else { Log.i(TAG, "File cannot be trashed: " + title); }

        queryResult(true, title);
    }
}
