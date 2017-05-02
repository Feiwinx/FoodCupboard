package sammobewick.pocketkitchen.aws_intents;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;

import java.io.File;
import java.io.IOException;

import sammobewick.pocketkitchen.supporting.Constants;

/**
 * S3 Uploader Intent.
 * Created by Sam on 30/03/2017.
 */
public class S3_Download_Image extends S3_Master_Image {
    private static final String TAG = "S3_Downloader";

    /**
     * Required empty constructor for manifest.
     */
    public S3_Download_Image() {
        super(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public S3_Download_Image(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        // TODO: Testing + Error Checking

        // Get Amazon Client:
        AmazonS3 s3 = getClient();
        TransferUtility transferUtility = new TransferUtility(s3, this.getApplicationContext());

        // Get key+receiver from Intent:
        String  key             = "";

        Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) {
            if (extras.containsKey(Constants.S3_OBJECT_KEY)) {
                key = extras.getString(Constants.S3_OBJECT_KEY);
            }
        }

        // Check key exists:
        if ((key != null ? key.length() : 0) > 0) {
            String error = "";
            Intent bcIntent = new Intent(Constants.BC_DOWNLOAD_NAME);

            try {
                File temp_dir = this.getCacheDir();
                File img_file = File.createTempFile("aws_temp_bmp", ".bmp", temp_dir);

                TransferObserver observer = transferUtility.download(
                        Constants.S3_BUCKET_NAME, key, img_file);

                Bitmap bitmap = BitmapFactory.decodeFile(img_file.getAbsolutePath());

                bcIntent.putExtra(Constants.RES_BUNDLE_KEY, bitmap);

            } catch (IOException e) {
                e.printStackTrace();
                bcIntent.putExtra(Constants.STATUS_KEY, false);
            } catch (AmazonServiceException ase) {
                error = "Amazon Error: " + ase.getLocalizedMessage();
                Log.d(TAG, error);
            }

            if (error.length() == 0)
                bcIntent.putExtra(Constants.STATUS_KEY, true);
            else
                bcIntent.putExtra(Constants.STATUS_KEY, false);

            LocalBroadcastManager.getInstance(this).sendBroadcast(bcIntent);
        }
    }
}
