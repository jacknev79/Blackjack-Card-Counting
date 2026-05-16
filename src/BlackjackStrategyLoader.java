import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class BlackjackStrategyLoader {

    private HashMap<String, HashMap<String, String>> hardStrategy = new HashMap<>();
    private HashMap<String, HashMap<String, String>> softStrategy = new HashMap<>();
    private HashMap<String, HashMap<String, String>> splitStrategy = new HashMap<>();

    // The deviations array: Index is the True Count (0-5)
    private final HashMap<String, HashMap<String, String>>[] deviationStrategies;

    // 1. Private constructor: No one else can instantiate this
    private BlackjackStrategyLoader() {
        // Initialize the array to size 6
        this.deviationStrategies = new HashMap[6];
        for (int i = 0; i < 6; i++) {
            deviationStrategies[i] = new HashMap<>();
        }

        // Load standard files
        loadAllStrategies(".");

        // Populate the deviations array
        initializeDeviations();
    }

    // 2. The "Holder" class: Thread-safe singleton implementation
    private static class LoaderHolder {
        private static final BlackjackStrategyLoader INSTANCE = new BlackjackStrategyLoader();
    }

    // 3. Global access point
    public static BlackjackStrategyLoader getInstance() {
        return LoaderHolder.INSTANCE;
    }

    /**
     * Hardcodes the specific deviation rules provided.
     */
    // NB may change to .properties file to allow easy way to add
    // additional deviations and make persist
    private void initializeDeviations() {
        // True Count 0
        addDev(0, "10", "16", "S");
        addDev(0, "2", "13", "S");
        addDev(0, "3", "13", "S");
        addDev(0, "4", "12", "S");
        addDev(0, "5", "12", "S");
        addDev(0, "6", "12", "S");

        // True Count 1
        addDev(1, "1", "11", "D");
        addDev(1, "2", "9", "D");

        // True Count 2
        addDev(2, "3", "12", "S");

        // True Count 3
        addDev(3, "2", "12", "S");
        addDev(3, "7", "9", "D");

        // True Count 4
        addDev(4, "10", "15", "S");
        addDev(4, "6", "20", "split");
        addDev(4, "10", "10", "D");
        addDev(4, "A", "10", "D");

        // True Count 5
        addDev(5, "5", "20", "split");
        addDev(5, "9", "16", "S");

    }

    /**
     * Helper to insert data into the nested structure.
     */
    private void addDev(int tc, String dealer, String player, String move) {
        deviationStrategies[tc].putIfAbsent(dealer, new HashMap<>());
        deviationStrategies[tc].get(dealer).put(player, move);
    }

    private void loadAllStrategies(String rootDirectory) {
        this.hardStrategy = loadCategoryDirectory(new File(rootDirectory, "hard"));
        this.softStrategy = loadCategoryDirectory(new File(rootDirectory, "soft"));
        this.splitStrategy = loadCategoryDirectory(new File(rootDirectory, "split"));
        System.out.println("Global Strategy Loader Initialized.");
    }

    private HashMap<String, HashMap<String, String>> loadCategoryDirectory(File directory) {
        HashMap<String, HashMap<String, String>> categoryMap = new HashMap<>();

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory not found: " + directory.getAbsolutePath());
            return categoryMap;
        }

        File[] propertiesFiles = directory.listFiles((dir, name) -> name.endsWith(".properties"));

        if (propertiesFiles != null) {
            for (File file : propertiesFiles) {
                String dealerUpcardKey = file.getName().replace(".properties", "");
                HashMap<String, String> handMovesMap = loadPropertiesIntoHashMap(file);
                categoryMap.put(dealerUpcardKey, handMovesMap);
            }
        }
        return categoryMap;
    }

    private HashMap<String, String> loadPropertiesIntoHashMap(File file) {
        HashMap<String, String> movesMap = new HashMap<>();
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            for (String handScoreKey : properties.stringPropertyNames()) {
                String moveValue = properties.getProperty(handScoreKey);
                String cleanValue = moveValue.split("#")[0].trim();
                movesMap.put(handScoreKey, cleanValue);
            }
        } catch (IOException e) {
            System.err.println("Failed to read properties file: " + file.getName() + " due to: " + e.getMessage());
        }
        return movesMap;
    }

    // Getters
    public HashMap<String, HashMap<String, String>> getHardStrategy() {
        return hardStrategy;
    }

    public HashMap<String, HashMap<String, String>> getSoftStrategy() {
        return softStrategy;
    }

    public HashMap<String, HashMap<String, String>> getSplitStrategy() {
        return splitStrategy;
    }

    public HashMap<String, HashMap<String, String>>[] getDeviationStrategies() {
        return deviationStrategies;
    }
}