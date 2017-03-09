package sammobewick.pocketkitchen.supporting;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;

/**
 * Created by Sam on 07/03/2017.
 */

public class LocalFileHelper {
    // FILENAMES:
    private static final String IN_CUPBOARDS    = "inCupboards.pk";
    private static final String RECIPES         = "recipesToCook.pk";
    private static final String INGREDIENTS     = "ingredientsRequired.pk";

    private Context context;

    public LocalFileHelper(Context context) {
        this.context = context;
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

    public void deleteAll() {
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
                FileOutputStream fos    = context.openFileOutput(RECIPES, Context.MODE_PRIVATE);
                ObjectOutputStream oos  = new ObjectOutputStream(fos);
                oos.writeObject(data);
                oos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
                FileOutputStream fos    = context.openFileOutput(IN_CUPBOARDS, Context.MODE_PRIVATE);
                ObjectOutputStream oos  = new ObjectOutputStream(fos);
                oos.writeObject(data);
                oos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
