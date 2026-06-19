import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class BlackjackStrategyLoader {

    // Helper class to return multiple pieces of data from the parser
    private static record ParsedMoveResult(String standardMove, boolean shouldSurrender) {}

    // The single cached instance
    private static BlackjackStrategyLoader instance;

    private final GameConfig config;

    private HashMap<String, HashMap<String, String>> hardStrategy = new HashMap<>();
    private HashMap<String, HashMap<String, String>> softStrategy = new HashMap<>();
    private HashMap<String, HashMap<String, String>> splitStrategy = new HashMap<>();
    private HashMap<String, HashMap<String, Boolean>> surrenderStrategy = new HashMap<>();

    // The deviations array: Index is the True Count (0-5)
    private final HashMap<String, HashMap<String, String>>[] deviationStrategies;

    // 1. Private constructor: Executed once during initialize()
    private BlackjackStrategyLoader(GameConfig config) {
        this.config = config;

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

    /**
     * 2. Main Thread Initialization Point.
     * Call this ONCE at simulation startup before spawning any threads.
     */
    public static void initialize(GameConfig config) {
        if (instance == null) {
            instance = new BlackjackStrategyLoader(config);
        }
    }

    /**
     * 3. Global Thread Access Point.
     */
    public static BlackjackStrategyLoader getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Strategy loader has not been initialized yet! Call initialize(config) first.");
        }
        return instance;
    }

    /**
     * CORRECT & SAFE WAY TO ACCESS SURRENDER STRATEGY
     * Prevents NullPointerExceptions from unboxing missing values.
     */
    public boolean shouldSurrender(String dealerUpcard, String playerTotal) {
        HashMap<String, Boolean> innerMap = surrenderStrategy.get(dealerUpcard);
        if (innerMap == null) {
            return false;
        }
        Boolean result = innerMap.get(playerTotal);
        return result != null && result;
    }

    /**
     * Advanced parser distinguishing between universal surrender and H17-only surrender.
     * Expected formats:
     * "H | SURR"      -> Base move H, surrenders on BOTH S17 and H17.
     * "H | SURR_H17"  -> Base move H, surrenders ONLY on H17.
     * "S | H | SURR_H17" -> S17 is S, H17 is H, but surrenders ONLY on H17.
     */
    private ParsedMoveResult parseMove(String rawValue) {
        String[] parts = rawValue.split("\\|");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        String lastToken = parts[parts.length - 1];
        boolean hasUniversalSurrender = lastToken.equalsIgnoreCase("SURR");
        boolean hasH17OnlySurrender = lastToken.equalsIgnoreCase("SURR_H17");

        boolean shouldSurrender = false;

        // Verify global surrender permission before looking at specific table constraints
        if (config.earlySurrender()) {
            if (hasUniversalSurrender) {
                shouldSurrender = true;
            } else if (hasH17OnlySurrender && config.hitSoft17()) {
                shouldSurrender = true;
            }
        }

        // Isolate the regular playing choices (ignore trailing surrender parameters)
        boolean hasSurrenderToken = hasUniversalSurrender || hasH17OnlySurrender;
        int baseMoveCount = hasSurrenderToken ? parts.length - 1 : parts.length;

        String standardMove;
        if (baseMoveCount == 1) {
            standardMove = parts[0];
        } else if (baseMoveCount >= 2) {
            standardMove = config.hitSoft17() ? parts[1] : parts[0];
        } else {
            standardMove = rawValue; // Fallback rule protection
        }

        return new ParsedMoveResult(standardMove, shouldSurrender);
    }

    /**
     * Hardcodes the specific deviation rules provided.
     */
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
        ParsedMoveResult result = parseMove(move);
        deviationStrategies[tc].get(dealer).put(player, result.standardMove());
    }

    private void loadAllStrategies(String rootDirectory) {
        this.surrenderStrategy = new HashMap<>();
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
                HashMap<String, String> handMovesMap = loadPropertiesIntoHashMap(file, dealerUpcardKey);
                categoryMap.put(dealerUpcardKey, handMovesMap);
            }
        }
        return categoryMap;
    }

    private HashMap<String, String> loadPropertiesIntoHashMap(File file, String dealerUpcardKey) {
        HashMap<String, String> movesMap = new HashMap<>();
        Properties properties = new Properties();

        surrenderStrategy.putIfAbsent(dealerUpcardKey, new HashMap<>());
        HashMap<String, Boolean> dealerSurrenderMap = surrenderStrategy.get(dealerUpcardKey);

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            for (String handScoreKey : properties.stringPropertyNames()) {
                String moveValue = properties.getProperty(handScoreKey);
                String cleanValue = moveValue.split("#")[0].trim();

                ParsedMoveResult parsedResult = parseMove(cleanValue);
                movesMap.put(handScoreKey, parsedResult.standardMove());

                // Track surrender values. Keep 'true' status if loaded via prior directory scans.
                if (parsedResult.shouldSurrender() || !dealerSurrenderMap.containsKey(handScoreKey)) {
                    dealerSurrenderMap.put(handScoreKey, parsedResult.shouldSurrender());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read properties file: " + file.getName() + " due to: " + e.getMessage());
        }
        return movesMap;
    }

    // Getters
    public HashMap<String, HashMap<String, String>> getHardStrategy() { return hardStrategy; }
    public HashMap<String, HashMap<String, String>> getSoftStrategy() { return softStrategy; }
    public HashMap<String, HashMap<String, String>> getSplitStrategy() { return splitStrategy; }
    public HashMap<String, HashMap<String, Boolean>> getSurrenderStrategy() { return surrenderStrategy; }
    public HashMap<String, HashMap<String, String>>[] getDeviationStrategies() { return deviationStrategies; }
}