import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class BlackjackStrategyLoader {

    private HashMap<String, HashMap<String, String>> hardStrategy = new HashMap<>();
    private HashMap<String, HashMap<String, String>> softStrategy = new HashMap<>();
    private HashMap<String, HashMap<String, String>> splitStrategy = new HashMap<>();

    // 1. Private constructor: No one else can instantiate this
    private BlackjackStrategyLoader() {
        // You can hardcode the load path here or load via a config
        loadAllStrategies(".");
    }

    // 2. The "Holder" class: This is the thread-safe secret sauce.
    // The JVM won't load this class until someone calls getInstance()
    private static class LoaderHolder {
        private static final BlackjackStrategyLoader INSTANCE = new BlackjackStrategyLoader();
    }

    // 3. Global access point
    public static BlackjackStrategyLoader getInstance() {
        return LoaderHolder.INSTANCE;
    }

    private void loadAllStrategies(String rootDirectory) {
        this.hardStrategy = loadCategoryDirectory(new File(rootDirectory, "hard"));
        this.softStrategy = loadCategoryDirectory(new File(rootDirectory, "soft"));
        this.splitStrategy = loadCategoryDirectory(new File(rootDirectory, "split"));
        System.out.println("Global Strategy Loader Initialized.");
    }
    /**
     * Reads a specific category directory and builds the outer HashMap.
     */
    private HashMap<String, HashMap<String, String>> loadCategoryDirectory(File directory) {
        // Outer Map: Key = Dealer Upcard (String), Value = Inner HashMap
        HashMap<String, HashMap<String, String>> categoryMap = new HashMap<>();

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory not found: " + directory.getAbsolutePath());
            return categoryMap;
        }

        // Only process .properties files
        File[] propertiesFiles = directory.listFiles((dir, name) -> name.endsWith(".properties"));

        if (propertiesFiles != null) {
            for (File file : propertiesFiles) {
                // Outer Key: Extract dealer upcard by removing ".properties" from the filename
                String dealerUpcardKey = file.getName().replace(".properties", "");

                // Inner Map: Load the contents of the file
                HashMap<String, String> handMovesMap = loadPropertiesIntoHashMap(file);

                // Put the inner map into the outer map
                categoryMap.put(dealerUpcardKey, handMovesMap);
            }
        }
        return categoryMap;
    }

    /**
     * Reads a single .properties file and builds the inner HashMap.
     */
    private HashMap<String, String> loadPropertiesIntoHashMap(File file) {
        // Inner Map: Key = Counter Hand Score (String), Value = Move (String)
        HashMap<String, String> movesMap = new HashMap<>();
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);

            // Iterate through the properties and populate the inner HashMap
            for (String handScoreKey : properties.stringPropertyNames()) {
                String moveValue = properties.getProperty(handScoreKey);
                // FIX: Split by '#' and take the first part, then trim spaces
                String cleanValue = moveValue.split("#")[0].trim();
                movesMap.put(handScoreKey, cleanValue);
            }
        } catch (IOException e) {
            System.err.println("Failed to read properties file: " + file.getName() + " due to: " + e.getMessage());
        }

        return movesMap;
    }

    // Getters returning the exact specified HashMap structure
    public HashMap<String, HashMap<String, String>> getHardStrategy() {
        return hardStrategy;
    }

    public HashMap<String, HashMap<String, String>> getSoftStrategy() {
        return softStrategy;
    }

    public HashMap<String, HashMap<String, String>> getSplitStrategy() {
        return splitStrategy;
    }
}