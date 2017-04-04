package sammobewick.pocketkitchen.aws_intents;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

import sammobewick.pocketkitchen.supporting.Constants;

/**
 * Created by Sam on 30/03/2017.
 */

public class S3_Upload_Image extends IntentService {
    private static final String TAG = "Dynamo_UploadImage";

    /**
     * Helper class to initialise connection to AWS connection pool.
     * @return AmazonS3 - being out S3 client to use.
     */
    private AmazonS3 getClient() {
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                Constants.AWS_ID_POOL,
                Regions.EU_WEST_1
        );
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.EU_WEST_1));
        return s3;
    }

    /**
     * Required empty constructor for manifest.
     */
    public S3_Upload_Image() {
        super("S3UploadImageService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public S3_Upload_Image(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Get Amazon Client:
        AmazonS3 s3 = getClient();
        TransferUtility transferUtility = new TransferUtility(s3, this.getApplicationContext());

        // Get File+Key from Intent:
        File    img_file    = null;
        String  key         = "";

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(Constants.INTENT_S3_FILE)) {
                img_file = (File) extras.get(Constants.INTENT_S3_FILE);
            }
            if (extras.containsKey(Constants.S3_OBJECT_KEY)) {
                key = extras.getString(Constants.S3_OBJECT_KEY);
            }
        }

        // Make sure we have a File+Key:
        if (img_file != null & key.length() > 0) {
            String error = "";

            try {   // Perform uploading to S3 with error-checking:
                TransferObserver observer = transferUtility.upload(
                        Constants.S3_BUCKET_NAME, key, img_file);

            } catch (AmazonServiceException ase) {
                error = "Amazon Error" + ase.getLocalizedMessage();
                Log.d(TAG, error);
            }

            /*// Debug:
            System.out.println(TAG + " error: " + error.length());
            //*/

            // Broadcast the result back:
            Intent bcIntent = new Intent(Constants.BC_UPLOAD_NAME);
            if (error.length() == 0)
                bcIntent.putExtra(Constants.BC_UPLOAD_ID, true);
            else
                bcIntent.putExtra(Constants.BC_UPLOAD_ID, false);

            LocalBroadcastManager.getInstance(this).sendBroadcast(bcIntent);
        }
    }
}
