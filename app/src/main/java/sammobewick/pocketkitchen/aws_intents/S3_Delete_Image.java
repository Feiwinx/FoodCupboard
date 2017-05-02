package sammobewick.pocketkitchen.aws_intents;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;

import sammobewick.pocketkitchen.supporting.Constants;

/**
 * S3 Deletion Intent.
 * Created by Sam on 27/04/2017.
 */
public class S3_Delete_Image extends S3_Master_Image {
    private static final String TAG = "S3_Deletion";

    /**
     * Required empty constructor for manifest.
     */
    public S3_Delete_Image() {
        super(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public S3_Delete_Image(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Get Amazon Client:
        AmazonS3 s3 = getClient();

        // Get File+Key from Intent:
        String  key         = "";

        Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) {
            if (extras.containsKey(Constants.S3_OBJECT_KEY)) {
                key = extras.getString(Constants.S3_OBJECT_KEY);
            }
        }

        // Make sure we have a File+Key:
        if ((key != null ? key.length() : 0) > 0) {
            String error = "";

            try {   // Perform deletion to S3 with error-checking:
                s3.deleteObject(new DeleteObjectRequest(Constants.S3_BUCKET_NAME, key));

            } catch (AmazonServiceException ase) {
                Log.e(TAG, "Error!", ase);
            }

            // Broadcast the result back:
            Intent bcIntent = new Intent(Constants.BC_DELETE_NAME);
            if (error.length() == 0)
                bcIntent.putExtra(Constants.BC_DELETE_ID, true);
            else
                bcIntent.putExtra(Constants.BC_DELETE_ID, false);

            LocalBroadcastManager.getInstance(this).sendBroadcast(bcIntent);
        }
    }
}
