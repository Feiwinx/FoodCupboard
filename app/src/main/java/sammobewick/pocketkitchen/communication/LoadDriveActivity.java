package sammobewick.pocketkitchen.communication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;

import sammobewick.pocketkitchen.data_objects.PocketKitchenData;

/**
 * Created by Sam on 06/03/2017.
 */
public class LoadDriveActivity extends BaseDriveActivity {

    // TAG for logs:
    private static final String TAG = "LoadDriveActivity";

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);
    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull final DriveApi.DriveContentsResult results) {
                    if (!results.getStatus().isSuccess()) {
                        // TODO: Error for some unknown reason.
                        return;
                    }
                    final DriveContents contents = results.getDriveContents();

                    PocketKitchenData pkData = PocketKitchenData.getInstance();

                    // TODO: Load data into pkData:

                    new Thread() {
                        @Override
                        public void run() {

                        }
                    }.run();
                }
            };

}
