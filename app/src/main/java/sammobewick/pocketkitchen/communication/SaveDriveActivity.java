package sammobewick.pocketkitchen.communication;

import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;

/**
 * Created by Sam on 06/03/2017.
 */
public class SaveDriveActivity extends BaseDriveActivity {

    // TAG for logs:
    private static final String TAG = "SaveDriveActivity";

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);

        Drive.DriveApi.newDriveContents(getmGoogleApiClient())
                .setResultCallback(driveContentsCallback);
    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {

                    if (!result.getStatus().isSuccess()) {
                        // TODO: Error for an unknown reason really.
                        return;
                    }
                    final DriveContents contents = result.getDriveContents();

                    PocketKitchenData pkData = PocketKitchenData.getInstance();

                    SaveList saveInCupboards = new SaveList();

                    // Arguably dangerous casting, but as I can guarantee the usage then this will be fine:
                    saveInCupboards.setListToSave((List<Object>)(Object)pkData.getInCupboards());
                    saveInCupboards.setContents(contents);
                    saveInCupboards.run();

                    SaveList saveRecipesToCook = new SaveList();

                    // Again, arguable dangerous cast, but same as above applies:
                    saveRecipesToCook.setListToSave((List<Object>)(Object)pkData.getRecipesToCook());
                    saveRecipesToCook.setContents(contents);
                    saveRecipesToCook.run();

                    // Save the map of required ingredients:
                    SaveMap saveIngredientsRequired = new SaveMap();

                    saveIngredientsRequired.setMapToSave(pkData.getRecipe_ingredients());
                    saveIngredientsRequired.setContents(contents);
                    saveIngredientsRequired.run();
                }
            };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        // TODO: Error.
                        return;
                    }
                    // TODO: Success.
                    System.out.println("saved-to-drive: " + result.getDriveFile().getDriveId());
                }
            };

    /**
     * Inner class to do a thread for saving a list.
     */
    private class SaveList extends Thread {
        private DriveContents contents;
        private List<Object> listToSave;

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            System.out.println("run: saveList");
            if (listToSave != null && contents != null) {
                OutputStream oStream = contents.getOutputStream();

                // Establish filename based on type:
                String filename;
                if (listToSave.get(0).getClass() == Ingredient.class) {
                    filename = "inCupboards.pk";
                } else if (listToSave.get(0).getClass() == Recipe_Short.class) {
                    filename = "recipesToCook.pk";
                } else { return; }

                // Write to File:
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(oStream);
                    oos.writeObject(listToSave);
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Set up Meta-Data:
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(filename)
                        .setMimeType("text/plan")
                        .setStarred(false)
                        .build();

                // Set up file:
                /*
                Drive.DriveApi.getAppFolder(getmGoogleApiClient())
                        .createFile(getmGoogleApiClient(), changeSet, contents)
                        .setResultCallback(fileCallback);
                        */
                Drive.DriveApi.getRootFolder(getmGoogleApiClient())
                        .createFile(getmGoogleApiClient(), changeSet, contents)
                        .setResultCallback(fileCallback);
            }
        }

        public void setListToSave(List<Object> listToSave) {
            this.listToSave = listToSave;
        }

        public void setContents(DriveContents contents) {
            this.contents = contents;
        }
    }

    /**
     * Inner class to do a thread for saving the map.
     */
    private class SaveMap extends Thread {
        private DriveContents contents;
        private Map<Integer, List<Ingredient>> mapToSave;

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (mapToSave != null && contents != null) {
                OutputStream oStream = contents.getOutputStream();

                try {
                    ObjectOutputStream oos = new ObjectOutputStream(oStream);
                    oos.writeObject(mapToSave);
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Set up Meta-Data:
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("ingredientsRequired.pk")
                        .setMimeType("text/plan")
                        .setStarred(false)
                        .build();

                // Set up file:
                Drive.DriveApi.getAppFolder(getmGoogleApiClient())
                        .createFile(getmGoogleApiClient(), changeSet, contents)
                        .setResultCallback(fileCallback);
            }
        }

        public void setContents(DriveContents contents) {
            this.contents = contents;
        }

        public void setMapToSave(Map<Integer, List<Ingredient>> mapToSave) {
            this.mapToSave = mapToSave;
        }
    }
}
