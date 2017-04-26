package sammobewick.pocketkitchen.communication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.Metadata;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;
import sammobewick.pocketkitchen.supporting.Constants;
import sammobewick.pocketkitchen.supporting.LocalFileHelper;

/**
 * Load from Drive activity.
 * This class handles the download of data from Google Drive for this user. It overwrites the local
 * storage so that can be used during the application. This would then be uploaded again on app-exit.
 *
 * Loading is a bit more simple than Saving, so all operations are passed through to the Asynctask.
 *
 * Created by Sam on 06/03/2017.
 */
public class LoadDriveActivity extends BaseDriveActivity {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG = "LoadDriveAct";
    private boolean errors;
    private int     result_count;

    /**
     * On a connection, we just want to reset our counter (which would not be useful in the super).
     * @param bundle Bundle - can be null, often will be.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        result_count = 0;
        errors = false;
        super.onConnected(bundle);
    }

    /**
     * Inform the user before exiting. There is very little to be done if it errors, but as it's the
     * saving process then re-entering the application
     */
    @Override
    public void finish() {
        if (errors) {
            errors = false;
            ActivityHelper.displayKnownError(LoadDriveActivity.this,
                    "Warning: some files had some issues syncing. It's recommended you " +
                            "avoid making changes this session, although you can continue to search for recipes." +
                            "\nIf you continue to see this message, please report it.");
        } else
            super.finish();
    }

    /**
     * Inherited from super. Handles a successful or erroneous query from the super.
     *
     * On successful completion, the activity will perform a save of local data.
     *
     * @param success boolean - being whether the operation was successful or not.
     * @param identifier String - the identifier (either for usage or logging).
     */
    @Override
    public void queryResult(boolean success, String identifier) {
        if (!success) {
            // If erroneous, then log it and load NULL:
            Log.i(TAG, "Error finding file. Loading NULL for " + identifier);
            loadData(null, identifier);
        } else {
            // If successful, log it:
            result_count += 1;
            if (result_count >= getMaxRequests()) {
                if (!errors) {
                    LocalFileHelper hlpr = new LocalFileHelper(LoadDriveActivity.this);
                    hlpr.saveAll();
                }
                finish();
            }
        }
    }

    /**
     * Called from super. Create an asynctask to attempt our file editing process.
     * @param meta Metadata - being the result Metadata from the base queries.
     */
    @Override
    public void handleMeta(Metadata meta) {
        new DownloadContentsAsync(getmGoogleApiClient()).execute(meta);
    }

    /**
     * Method to load an object into our PocketKitchenData class.
     * @param o Object - our loaded data from the ASyncTask.
     * @param identifier String - the identifier for the data.
     */
    @SuppressWarnings("unchecked")
    private void loadData(Object o, String identifier) {
        Log.i(TAG, "LOADING: " + identifier + " o == null: " + String.valueOf(o == null));
        PocketKitchenData pkData = PocketKitchenData.getInstance();

        // As we are in full control of the data structure within each file, the following casts are safe.
        switch (identifier) {
            case Constants.MY_RECIPES:
                pkData.setMyCustomRecipes((List<Recipe_Short>) o);
                break;
            case Constants.IN_CUPBOARDS:
                pkData.setInCupboards((List<Ingredient>) o);
                break;
            case Constants.RECIPES:
                pkData.setRecipesToCook((List<Recipe_Short>) o);
                break;
            case Constants.INGREDIENTS:
                pkData.setIngredientsRequired((Map<Integer, List<Ingredient>>) o);
                break;
            default:
                Log.e(TAG, "Tried to load an object without recognised identifier!");
                break;
        }
        // Log a complete query:
        queryResult(true, identifier);
    }

    /**
     * ASyncTask to download data from Google Drive.
     */
    private class DownloadContentsAsync extends AsyncTask<Metadata, Boolean, Object> {
        private String identifier;
        private GoogleApiClient client;

        /**
         * Constructor. Requires the GoogleApiClient to be passed through though.
         * @param client GoogleApiClient - being the client.
         */
        DownloadContentsAsync(GoogleApiClient client) {
            this.client = client;
        }

        /**
         * doInBackground. Here we process the Metadata from the superclas, and load the file into
         * the local data-structure.
         * @param params Metadata - being our file data.
         * @return Object - being our loaded data (processed in LoadData).
         */
        @Override
        protected Object doInBackground(Metadata... params) {
            // Setup our data:
            Object data     = null;
            identifier      = params[0].getTitle();
            DriveFile file  = params[0].getDriveId().asDriveFile();

            // Fetch our DriveContentsResult (i.e. the actual file data):
            DriveApi.DriveContentsResult contentsResult =
                    file.open(client, DriveFile.MODE_READ_ONLY, null).await();

            // Check no errors were encountered:
            if (!contentsResult.getStatus().isSuccess()) {
                Log.e(TAG, "Loading: Status code: " + contentsResult.getStatus().getStatusCode() +
                        "\nStatus error - " + contentsResult.getStatus().getStatusMessage());
                errors = true;
                return null;
            }

            // Get our InputStream and write it to the local area:
            DriveContents driveContents = contentsResult.getDriveContents();
            try {
                ObjectInputStream ois = new ObjectInputStream(driveContents.getInputStream());
                data = ois.readObject();
                ois.close();

            } catch (EOFException e) {
                // This is NOT always a problem. This is in fact expected when reading a file to it's end.
                // However, we handle this here to prevent app-crashes, and inform the user in case anything
                // is amiss.
                Log.d(TAG, "EOFException for " + identifier);
                errors = true;
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "IOException while reading from the stream!");
                errors = true;
            }
            return data;
        }

        /**
         * Finally, handle our returned data:
         * @param o Object - being the returned data.
         */
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            // Perform a check on the data + log:
            if (o != null) {
                loadData(o, identifier);
                Log.i(TAG, "Successfully read object for " + identifier);
            } else {
                errors = true;
                queryResult(true, identifier);
                Log.i(TAG, "Failed to read object for " + identifier);
            }
        }
    }
}
