package sammobewick.pocketkitchen.communication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import sammobewick.pocketkitchen.data_objects.PocketKitchenData;

/**
 * Using the comment shown below, this class allows for images to be loaded Asynchronously, so the
 * UI thread is not blocked!
 * http://stackoverflow.com/a/10868126
 * Created by Sam on 09/02/2017.
 */
public final class DownloadImageAsync extends AsyncTask<String, Void, Bitmap> {
    private ImageView   bitmapImg;

    public DownloadImageAsync(ImageView bitmapImg) {
        this.bitmapImg = bitmapImg;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String urlDisplay = urls[0];
        Bitmap img = null;

        try {
            InputStream in = new URL(urlDisplay).openStream();
            img = BitmapFactory.decodeStream(in);

            // Attempt to save a scaled copy:
            if (img != null) {
                img = Bitmap.createScaledBitmap(img, 250, 250, false);
                PocketKitchenData.putDrawable(urlDisplay, img);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        bitmapImg.setImageBitmap(bitmap);
    }
}
