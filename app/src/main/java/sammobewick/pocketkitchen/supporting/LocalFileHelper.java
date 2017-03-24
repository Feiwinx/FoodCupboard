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
 * TODO: May want to add some sort of error handling for the user's end?
 * Created by Sam on 07/03/2017.
 */
public class LocalFileHelper {
    private static final String TAG             = "LocalFileHelper";
    private static final String IN_CUPBOARDS    = "inCupboards.pk";
    private static final String RECIPES         = "recipesToCook.pk";
    private static final String INGREDIENTS     = "ingredientsRequired.pk";

    private final Context context;

    public LocalFileHelper(Context context) {
        this.context = context;
    }

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

    public void saveAll() {
        this.saveInCupboards();
        this.saveIngredientsRequired();
        this.saveRecipesToCook();
    }

    public void loadAll() {
        this.loadInCupboards();
        this.loadIngredientsRequired();
        this.loadRecipesToCook();
    }

    // NOTE: So far as DRIVE data does not exist, this is only local!
    public void deleteAll() {
        RunDeleteFiles run = new RunDeleteFiles();
        ConfirmRunnable("Are you sure you want to delete all locally-stored files?", run);
    }

    public void deleteAllNoDialog() {
        RunDeleteFiles run = new RunDeleteFiles();
        run.run();
    }

    public void saveIngredientsRequired() {
        RunSaveIngredients run = new RunSaveIngredients();
        run.run();
    }

    public void saveRecipesToCook() {
        RunSaveRecipes run = new RunSaveRecipes();
        run.run();
    }

    public void saveInCupboards() {
        RunSaveInCupboards run = new RunSaveInCupboards();
        run.run();
    }

    public void loadIngredientsRequired() {
        RunLoadIngredients run = new RunLoadIngredients();
        run.run();
    }

    public void loadRecipesToCook() {
        RunLoadRecipes run = new RunLoadRecipes();
        run.run();
    }

    public void loadInCupboards() {
        RunLoadInCupboards run = new RunLoadInCupboards();
        run.run();
    }

    private class RunDeleteFiles implements Runnable {
        @Override
        public void run() {
            // Clear our preferences:
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().clear().apply();

            // Clear the local data:
            PocketKitchenData pkData = PocketKitchenData.getInstance();
            pkData.setIngredientsRequired(null);
            pkData.setInCupboards(null);
            pkData.setRecipesToCook(null);

            context.deleteFile(INGREDIENTS);
            context.deleteFile(IN_CUPBOARDS);
            context.deleteFile(RECIPES);
        }
    }

    private class RunSaveIngredients implements Runnable {
        @Override
        public void run() {
            Map<Integer, List<Ingredient>> data;

            PocketKitchenData pkData = PocketKitchenData.getInstance();
            data = pkData.getRecipe_ingredients();

            try {
                FileOutputStream fos = context.openFileOutput(INGREDIENTS, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(data);
                oos.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    private class RunSaveRecipes implements Runnable {
        @Override
        public void run() {
            List<Recipe_Short> data;

            PocketKitchenData pkData = PocketKitchenData.getInstance();
            data = pkData.getRecipesToCook();

            try {
                FileOutputStream fos = context.openFileOutput(RECIPES, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(data);
                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    private class RunSaveInCupboards implements Runnable {
        @Override
        public void run() {
            List<Ingredient> data;

            PocketKitchenData pkData = PocketKitchenData.getInstance();
            data = pkData.getInCupboards();

            try {
                FileOutputStream fos = context.openFileOutput(IN_CUPBOARDS, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(data);
                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    private class RunLoadInCupboards implements Runnable {
        @Override
        public void run() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();

            try {
                FileInputStream fis = context.openFileInput(IN_CUPBOARDS);
                ObjectInputStream ois = new ObjectInputStream(fis);
                List<Ingredient> list = (List<Ingredient>) ois.readObject();

                if (list != null)
                    pkData.setInCupboards(list);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    private class RunLoadRecipes implements Runnable {
        @Override
        public void run() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();

            try {
                FileInputStream fis = context.openFileInput(RECIPES);
                ObjectInputStream ois = new ObjectInputStream(fis);
                List<Recipe_Short> list = (List<Recipe_Short>) ois.readObject();

                if (list != null)
                    pkData.setRecipesToCook(list);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    private class RunLoadIngredients implements Runnable {
        @Override
        public void run() {
            PocketKitchenData pkData = PocketKitchenData.getInstance();

            try {
                FileInputStream fis = context.openFileInput(INGREDIENTS);
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
}
