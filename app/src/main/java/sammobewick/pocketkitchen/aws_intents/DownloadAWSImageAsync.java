package sammobewick.pocketkitchen.aws_intents;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.IOException;

import sammobewick.pocketkitchen.supporting.Constants;

/**
 * Using our original DownloadImageAsync, and combining it with the AWS IntentService code, this
 * class intends to allow an adapter to download an image for each row from S3.
 * Created by Sam on 09/02/2017.
 */
public final class DownloadAWSImageAsync extends AsyncTask<String, Void, Bitmap> {
    private Context     mContext;
    private ImageView   bitmapImg;
    private File        img_file;

    public DownloadAWSImageAsync(Context context, ImageView bitmapImg) {
        this.mContext   = context;
        this.bitmapImg  = bitmapImg;
    }

    @Override
    protected Bitmap doInBackground(String... keys) {
        String key = keys[0];
        Bitmap img = null;

        try {
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    mContext, Constants.AWS_ID_POOL, Regions.EU_WEST_1
            );
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            s3.setRegion(Region.getRegion(Regions.EU_WEST_1));

            TransferUtility transferUtility = new TransferUtility(s3, mContext);

            File temp_dir = mContext.getCacheDir();
            img_file = File.createTempFile("aws_temp_bmp", ".bmp", temp_dir);

            TransferObserver observer = transferUtility.download(
                    Constants.S3_BUCKET_NAME, key, img_file);

            observer.setTransferListener(new DownloadListener());

        } catch (AmazonServiceException ase) {
            ase.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        bitmapImg.setImageBitmap(bitmap);
    }

    private class DownloadListener implements TransferListener {

        @Override
        public void onStateChanged(int id, TransferState state) {
            if (state == TransferState.COMPLETED) {
                Bitmap img = BitmapFactory.decodeFile(img_file.getAbsolutePath());
                bitmapImg.setImageBitmap(img);
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

        }

        @Override
        public void onError(int id, Exception ex) {

        }
    }
}
