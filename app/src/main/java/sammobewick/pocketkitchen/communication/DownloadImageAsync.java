package sammobewick.pocketkitchen.communication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Using the comment shown below, this class allows for images to be loaded Asynchronously, so the
 * UI thread is not blocked!
 * http://stackoverflow.com/a/10868126
 * Created by Sam on 09/02/2017.
 */
public final class DownloadImageAsync extends AsyncTask<String, Void, Bitmap> {
    ImageView bitmapImg;

    public DownloadImageAsync(ImageView bitmapImg) {
        this.bitmapImg = bitmapImg;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String urlDisplay   = urls[0];
        Bitmap img          = null;

        try {
            InputStream in = new URL(urlDisplay).openStream();
            img            = BitmapFactory.decodeStream(in);

        } catch (MalformedURLException e) {
            e.printStackTrace();
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
