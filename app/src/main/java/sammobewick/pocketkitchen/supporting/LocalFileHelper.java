package sammobewick.pocketkitchen.supporting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;

/**
 * Created by Sam on 07/03/2017.
 */
public class LocalFileHelper {
    private static final String TAG = "LocalFileHelper";
    private final Context context;

    /**
     * File manipulations will required the context.
     * @param context Context - required by file operations.
     */
    public LocalFileHelper(Context context) {
        this.context = context;
    }

    /**
     *
     * @param message
     * @param runnable
     */
    public void ConfirmRunnable(String message, final Runnable runnable) {
        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog)).create();

        dialog.setTitle(R.string.lbl_dialog_confirmation_title);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);

        // BUTTONS
        String lbl;
        lbl = context.getResources().getString(R.string.lbl_dialog_confirmation_yes);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, lbl, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runnable.run();
            }
        });

        lbl = context.getResources().getString(R.string.lbl_dialog_confirmation_no);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, lbl, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "Deletion aborted.");
            }
        });

        dialog.show();
    }

    /**
     * Collection of save threads in one place. Will save all local files.
     */
    public void saveAll() {
        RunSaveIngredients run1 = new RunSaveIngredients();
        run1.run();

        RunSaveRecipes run2 = new RunSaveRecipes();
        run2.run();

        RunSaveInCupboards run3 = new RunSaveInCupboards();
        run3.run();

        RunSaveMyRecipes run4 = new RunSaveMyRecipes();
        run4.run();
    }

    /**
     * Collection of load threads in one place. Used when only working with local data.
     */
    public void loadAll() {
        RunLoadIngredients run1 = new RunLoadIngredients();
        run1.run();

        RunLoadRecipes run2 = new RunLoadRecipes();
        run2.run();

        RunLoadInCupboards run3 = new RunLoadInCupboards();
        run3.run();

        RunLoadMyRecipes run4 = new RunLoadMyRecipes();
        run4.run();
    }

    /**
     * Deletion method which shows a dialog to the user.
     * @param revisitTutorial boolean - should the tutorial be considered visited already?
     */
    public void deleteAll(boolean revisitTutorial) {
        RunDeleteFiles run = new RunDeleteFiles(revisitTutorial);
        ConfirmRunnable("Are you sure you want to delete all locally-stored files?", run);
    }

    /**
     * Deletion method which does NOT show a dialog. Simply runs.
     * @param revisitTutorial boolean - should the tutorial be considered visited already?
     */
    public void deleteAllNoDialog(boolean revisitTutorial) {
        RunDeleteFiles run = new RunDeleteFiles(revisitTutorial);
        run.run();
    }

    /**
     * Runnable deletion process. The process is simpler than writing to files so all are done in
     * a single process.
     */
    private class RunDeleteFiles implements Runnable {
        private boolean tutorial;

        RunDeleteFiles(boolean tutorial) {
            this.tutorial = tutorial;
        }

        @Override
        public void run() {
            // Clear our preferences:
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().clear();

            // However, chuck back in the tutorial if required:
            prefs.edit().putBoolean("firstTimeUsage", tutorial).apply();

            // Clear the local data:
            PocketKitchenData pkData = PocketKitchenData.getInstance();
            pkData.setIngredientsRequired(null);
            pkData.setInCupboards(null);
            pkData.setRecipesToCook(null);

            context.deleteFile(Constants.INGREDIENTS);
            context.deleteFile(Constants.IN_CUPBOARDS);
            context.deleteFile(Constants.RECIPES);
            context.deleteFile(Constants.MY_RECIPES);
        }
    }

    private class RunSaveIngredients implements Runnable {

        private Map<Integer, List<Ingredient>> dataToSave;

        public RunSaveIngredients(Map<Integer, List<Ingredient>> dataToSave) {
            this.dataToSave = dataToSave;
        }

        RunSaveIngredients() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();
            this.dataToSave = pkData.getRecipe_ingredients();
        }

        @Override
        public void run() {
            if (dataToSave != null) {
                try {
                    FileOutputStream fos = context.openFileOutput(Constants.INGREDIENTS, Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(dataToSave);
                    oos.close();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error when saving data!", e);
                }
            }
        }
    }

    private class RunSaveRecipes implements Runnable {
        private List<Recipe_Short> dataToSave;

        public RunSaveRecipes(List<Recipe_Short> dataToSave) {
            this.dataToSave = dataToSave;
        }

        RunSaveRecipes() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();
            this.dataToSave = pkData.getRecipesToCook();
        }

        @Override
        public void run() {
            if (dataToSave != null) {
                try {
                    FileOutputStream fos = context.openFileOutput(Constants.RECIPES, Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(dataToSave);
                    oos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error when saving data!", e);
                }
            }
        }
    }

    private class RunSaveInCupboards implements Runnable {
        private List<Ingredient> dataToSave;

        public RunSaveInCupboards(List<Ingredient> dataToSave) {
            this.dataToSave = dataToSave;
        }

        RunSaveInCupboards() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();
            this.dataToSave = pkData.getInCupboards();
        }

        @Override
        public void run() {
            if (dataToSave != null) {
                try {
                    FileOutputStream fos = context.openFileOutput(Constants.IN_CUPBOARDS, Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(dataToSave);
                    oos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error when saving data!", e);
                }
            }
        }
    }

    private class RunSaveMyRecipes implements Runnable {
        private List<Recipe_Short> dataToSave;

        public RunSaveMyRecipes(List<Recipe_Short> dataToSave) {
            this.dataToSave = dataToSave;
        }

        RunSaveMyRecipes() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();
            this.dataToSave = pkData.getMyCustomRecipes();
        }

        @Override
        public void run() {
            if (dataToSave != null) {
                try {
                    FileOutputStream fos = context.openFileOutput(Constants.MY_RECIPES, Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(dataToSave);
                    oos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error when saving data!", e);
                }
            }
        }
    }

    private class RunLoadInCupboards implements Runnable {
        @Override
        public void run() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();
            try {
                FileInputStream fis = context.openFileInput(Constants.IN_CUPBOARDS);
                ObjectInputStream ois = new ObjectInputStream(fis);
                List<Ingredient> list = (List<Ingredient>) ois.readObject();

                if (list != null)
                    pkData.setInCupboards(list);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "Error when saving data!", e);
            }
        }
    }

    private class RunLoadRecipes implements Runnable {
        @Override
        public void run() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();

            try {
                FileInputStream fis = context.openFileInput(Constants.RECIPES);
                ObjectInputStream ois = new ObjectInputStream(fis);
                List<Recipe_Short> list = (List<Recipe_Short>) ois.readObject();

                if (list != null)
                    pkData.setRecipesToCook(list);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "Error when saving data!", e);
            }
        }
    }

    private class RunLoadIngredients implements Runnable {
        @Override
        public void run() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();

            try {
                FileInputStream fis = context.openFileInput(Constants.INGREDIENTS);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Map<Integer, List<Ingredient>> map = (Map<Integer, List<Ingredient>>) ois.readObject();

                if (map != null)
                    pkData.setIngredientsRequired(map);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    private class RunLoadMyRecipes implements Runnable {
        @Override
        public void run() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();

            try {
                FileInputStream fis = context.openFileInput(Constants.MY_RECIPES);
                ObjectInputStream ois = new ObjectInputStream(fis);
                List<Recipe_Short> list = (List<Recipe_Short>) ois.readObject();

                if (list != null)
                    pkData.setMyCustomRecipes(list);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }
}
