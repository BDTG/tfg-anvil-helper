package vn.bdtg.anvil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.ForgeSteps;

/**
 * Standalone assertions against the exact installed TFC classes, not a copied rule parser.
 * Run with python3 build.py; neither JUnit nor the JVM's optional -ea flag is required.
 * The shortest-path oracle keeps all eight real ForgeStep identities in explicit tuples,
 * and uses TFC deltas/rules, never Solver.matches, Action.delta, or Action.category.
 */
public final class SolverValidationTest {
    private static final int MAX_WORK = 150;
    private static final int CATEGORY_HISTORIES = 7 * 7 * 7;
    private static final int UNCHECKED_DISTANCE = -2;
    private static final String RECIPE_PREFIX = "data/tfc/recipes/anvil/";
    private static final ForgeRule[] RULES = ForgeRule.values();
    private static final ForgeStep[] STEPS = ForgeStep.values();
    private static final List<String> FAILURES = new ArrayList<>();
    private static long checks;
    private static int solveCalls;
    private static int passedGroups;

    private SolverValidationTest() {}

    @FunctionalInterface
    private interface TestBody {
        void run() throws Exception;
    }

    /** Newest first; null is the independent oracle's absent-history marker. */
    private record State(int work, ForgeStep last, ForgeStep second, ForgeStep third) {}

    private record Recipe(String resource, List<String> names, List<ForgeRule> rules) {}

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void run(String name, TestBody body) {
        try {
            body.run();
            passedGroups++;
            System.out.println("PASS SolverValidationTest: " + name);
        } catch (AssertionError | Exception failure) {
            FAILURES.add(name + ": " + failure);
            System.err.println("FAIL SolverValidationTest: " + name);
            failure.printStackTrace(System.err);
        }
    }

    private static Path tfcJar() throws Exception {
        return Path.of(ForgeRule.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private static List<ForgeRule> enumRules(List<String> names) {
        return names.stream().map(ForgeRule::valueOf).toList();
    }

    private static int category(ForgeStep step) {
        if (step == null) return 0;
        return switch (step) {
            case HIT_LIGHT, HIT_MEDIUM, HIT_HARD -> 1;
            case DRAW -> 2;
            case PUNCH -> 3;
            case BEND -> 4;
            case UPSET -> 5;
            case SHRINK -> 6;
        };
    }

    private static ForgeStep representative(int category) {
        return switch (category) {
            case 0 -> null;
            case 1 -> ForgeStep.HIT_LIGHT;
            case 2 -> ForgeStep.DRAW;
            case 3 -> ForgeStep.PUNCH;
            case 4 -> ForgeStep.BEND;
            case 5 -> ForgeStep.UPSET;
            case 6 -> ForgeStep.SHRINK;
            default -> throw new AssertionError("Bad test category: " + category);
        };
    }

    private static ForgeSteps steps(ForgeStep last, ForgeStep second, ForgeStep third) {
        ForgeSteps result = new ForgeSteps();
        // Null padding intentionally exercises even artificial NONE holes among all 343 encodings.
        // ForgeRule.matches inspects the three slots, not ForgeSteps.total().
        result.addStep(third);
        result.addStep(second);
        result.addStep(last);
        return result;
    }

    private static ForgeSteps steps(int encoded) {
        return steps(representative(encoded % 7), representative(encoded / 7 % 7),
            representative(encoded / 49));
    }

    private static ForgeSteps steps(List<Solver.Action> newestFirst) {
        ForgeSteps result = new ForgeSteps();
        for (int i = newestFirst.size() - 1; i >= 0; i--) {
            result.addStep(ForgeStep.valueOf(newestFirst.get(i).name()));
        }
        return result;
    }

    private static boolean accepted(ForgeSteps history, List<ForgeRule> rules) {
        for (ForgeRule rule : rules) {
            if (!rule.matches(history)) return false;
        }
        return true;
    }

    private static Recipe recipe(JarFile jar, String resource) throws Exception {
        JarEntry entry = jar.getJarEntry(resource);
        check(entry != null, "Missing installed recipe: " + resource);
        try (var reader = new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            check(object.get("type").getAsString().equals("tfc:anvil"),
                "Not an actual TFC anvil recipe: " + resource);
            JsonArray array = object.getAsJsonArray("rules");
            check(array != null && !array.isEmpty(), "Missing recipe rules: " + resource);
            List<String> names = new ArrayList<>();
            array.forEach(value -> names.add(value.getAsString().toUpperCase(Locale.ROOT)));
            return new Recipe(resource, List.copyOf(names), enumRules(names));
        }
    }

    private static Recipe recipe(String resource) throws Exception {
        try (JarFile jar = new JarFile(tfcJar().toFile())) {
            return recipe(jar, resource);
        }
    }

    private static void installedArtifactAndActions() throws Exception {
        Path jar = tfcJar();
        check(jar.getFileName().toString().equals("TerraFirmaGreg-Core-Modern-0.9.30.jar"),
            "Parity must load exact installed TFG Core 0.9.30, actually " + jar);
        for (Class<?> type : List.of(ForgeStep.class, ForgeSteps.class)) {
            check(Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).equals(jar),
                "Mixed TFC artifacts for " + type.getName());
        }
        try (JarFile archive = new JarFile(jar.toFile());
             var input = archive.getInputStream(archive.getJarEntry("META-INF/mods.toml"))) {
            String metadata = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            check(metadata.contains("modId = \"tfc\"\nversion = \"3.2.28\""),
                "Installed artifact does not declare TFC 3.2.28");
        }
        // Reflection reads the runtime field instead of javac inlining the constant.
        check(ForgeStep.class.getField("LIMIT").getInt(null) == MAX_WORK, "TFC work limit changed");
        check(RULES.length == 30, "Expected all 30 installed ForgeRule values");
        check(STEPS.length == 8, "Expected all eight installed ForgeStep values");
        check(Solver.Action.values().length == STEPS.length, "Missing or extra Solver actions");
        Set<ForgeStep> mapped = new HashSet<>();
        for (Solver.Action action : Solver.Action.values()) {
            ForgeStep actual = ForgeStep.valueOf(action.name());
            check(mapped.add(actual), "Duplicate action mapping: " + action);
            check(action.delta == actual.step(), action + " delta " + action.delta
                + " differs from installed ForgeStep.step()=" + actual.step());
            check(action.category == category(actual), "Wrong category for " + action);
        }
        check(mapped.equals(Set.of(STEPS)), "Action names do not cover installed ForgeSteps");
        System.out.println("  Oracle artifact: " + jar + " (TFC 3.2.28, LIMIT=150)");
    }

    private static void exhaustiveCategoryRuleParity() {
        int comparisons = 0;
        for (int h = 0; h < CATEGORY_HISTORIES; h++) {
            ForgeSteps actual = steps(h);
            check(category(actual.last()) == h % 7, "Newest-first fixture last: " + h);
            check(category(actual.secondLast()) == h / 7 % 7, "Newest-first fixture second: " + h);
            check(category(actual.thirdLast()) == h / 49, "Newest-first fixture third: " + h);
            check(Solver.matches(h, List.of()), "No rules should accept history " + h);
            for (ForgeRule rule : RULES) {
                check(Solver.matches(h, List.of(rule.name())) == rule.matches(actual),
                    "TFC parity h=" + h + ", steps=" + actual + ", rule=" + rule);
                comparisons++;
            }
        }
        check(comparisons == CATEGORY_HISTORIES * RULES.length, "Incomplete rule parity coverage");
        System.out.println("  Category-rule comparisons: " + comparisons);
    }

    private static void allStepIdentityParity() {
        List<ForgeStep> includingNone = new ArrayList<>();
        includingNone.add(null);
        includingNone.addAll(Arrays.asList(STEPS));
        int histories = 0;
        int comparisons = 0;
        for (ForgeStep last : includingNone) {
            for (ForgeStep second : includingNone) {
                for (ForgeStep third : includingNone) {
                    int h = category(last) + 7 * category(second) + 49 * category(third);
                    ForgeSteps actual = steps(last, second, third);
                    for (ForgeRule rule : RULES) {
                        check(Solver.matches(h, List.of(rule.name())) == rule.matches(actual),
                            "Actual-step parity [" + last + ", " + second + ", " + third + "] " + rule);
                        comparisons++;
                    }
                    histories++;
                }
            }
        }
        check(histories == 9 * 9 * 9, "Not all actual-step histories were compared");
        System.out.println("  Actual-step histories: " + histories + ", rule comparisons: " + comparisons);
    }

    private static void orderedRulePairParity() {
        int comparisons = 0;
        for (int h = 0; h < CATEGORY_HISTORIES; h++) {
            ForgeSteps actual = steps(h);
            for (ForgeRule first : RULES) {
                for (ForgeRule second : RULES) {
                    boolean expected = first.matches(actual) && second.matches(actual);
                    check(Solver.matches(h, List.of(first.name(), second.name())) == expected,
                        "Rule conjunction parity h=" + h + ", rules=" + first + "/" + second);
                    comparisons++;
                }
            }
        }
        System.out.println("  Ordered rule-pair comparisons (including duplicates): " + comparisons);
    }

    private static void installedRecipeRuleParity() throws Exception {
        int recipes = 0;
        int comparisons = 0;
        try (JarFile jar = new JarFile(tfcJar().toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                String resource = entries.nextElement().getName();
                if (!resource.startsWith(RECIPE_PREFIX) || !resource.endsWith(".json")) continue;
                Recipe recipe = recipe(jar, resource);
                for (int h = 0; h < CATEGORY_HISTORIES; h++) {
                    check(Solver.matches(h, recipe.names()) == accepted(steps(h), recipe.rules()),
                        "Installed recipe parity h=" + h + ", resource=" + resource);
                    comparisons++;
                }
                recipes++;
            }
        }
        check(recipes == 278, "Incomplete exact-artifact recipe inventory: " + recipes);
        check(comparisons == recipes * CATEGORY_HISTORIES, "Incomplete recipe parity coverage");
        System.out.println("  Installed TFC recipes: " + recipes + ", history comparisons: " + comparisons);
    }

    /** Full level-order enumeration in the larger real-step graph, once per sample, not per target. */
    private static int[] oracleDistances(int work, List<Solver.Action> history, List<ForgeRule> rules) {
        ForgeSteps initial = steps(history);
        State start = new State(work, initial.last(), initial.secondLast(), initial.thirdLast());
        Set<State> visited = new HashSet<>();
        visited.add(start);
        List<State> frontier = List.of(start);
        int[] distances = new int[MAX_WORK + 1];
        Arrays.fill(distances, -1);
        for (int depth = 0; !frontier.isEmpty(); depth++) {
            List<State> nextFrontier = new ArrayList<>();
            for (State state : frontier) {
                if (distances[state.work()] < 0
                    && accepted(steps(state.last(), state.second(), state.third()), rules)) {
                    distances[state.work()] = depth;
                }
                for (ForgeStep action : STEPS) {
                    int nextWork = state.work() + action.step();
                    if (nextWork < 0 || nextWork > MAX_WORK) continue;
                    State next = new State(nextWork, action, state.last(), state.second());
                    if (visited.add(next)) nextFrontier.add(next);
                }
            }
            frontier = nextFrontier;
        }
        return distances;
    }

    /** Replay every returned action with real TFC deltas, maintaining real oldest-first ForgeSteps. */
    private static Optional<List<Solver.Action>> solveChecked(int work, int target,
            List<Solver.Action> history, List<String> names, int expectedDistance) {
        List<Solver.Action> originalHistory = List.copyOf(history);
        List<String> originalRules = List.copyOf(names);
        solveCalls++;
        Optional<List<Solver.Action>> result = Solver.solve(work, target, history, names);
        String context = "work=" + work + ", target=" + target + ", history=" + history + ", rules=" + names;
        check(history.equals(originalHistory), "Solver mutated history: " + context);
        check(names.equals(originalRules), "Solver mutated rules: " + context);
        if (expectedDistance == -1) {
            check(result.isEmpty(), "Expected impossible route: " + context + ", actual=" + result);
        } else if (expectedDistance >= 0) {
            check(result.isPresent(), "Missing reachable route of length " + expectedDistance + ": " + context);
            check(result.orElseThrow().size() == expectedDistance,
                "Not shortest: expected " + expectedDistance + ", actual=" + result + ", " + context);
        }
        if (result.isPresent()) {
            ForgeSteps actual = steps(history);
            int replayWork = work;
            for (Solver.Action action : result.get()) {
                ForgeStep step = ForgeStep.valueOf(action.name());
                replayWork += step.step();
                check(replayWork >= 0 && replayWork <= MAX_WORK,
                    "Intermediate work out of bounds at " + action + ": " + replayWork + ", " + context);
                actual.addStep(step);
            }
            check(replayWork == target, "Replay missed exact target: " + replayWork + ", " + context);
            check(accepted(actual, enumRules(names)), "Replay violates actual TFC rules: " + actual + ", " + context);
        }
        return result;
    }

    private static void recipeAllTargets(String resource, int work, List<Solver.Action> history) throws Exception {
        Recipe recipe = recipe(resource);
        int[] expected = oracleDistances(work, history, recipe.rules());
        int reachable = 0;
        int impossible = 0;
        for (int target = 0; target <= MAX_WORK; target++) {
            solveChecked(work, target, history, recipe.names(), expected[target]);
            if (expected[target] >= 0) reachable++;
            else impossible++;
        }
        check(reachable > 0, "Vacuous sample with no reachable target: " + resource);
        check(reachable + impossible == MAX_WORK + 1, "Incomplete sample targets: " + resource);
        System.out.println("  " + resource + ": all " + (MAX_WORK + 1) + " targets; "
            + reachable + " reachable, " + impossible + " impossible; shortest/replay checked");
    }

    private static void boundsAndSingleSteps() {
        for (int invalid : new int[] {-1, MAX_WORK + 1, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            solveChecked(invalid, 75, List.of(), List.of(), -1);
            solveChecked(75, invalid, List.of(), List.of(), -1);
            solveChecked(invalid, invalid, List.of(), List.of(), -1);
        }
        for (int endpoint : new int[] {0, MAX_WORK}) {
            solveChecked(endpoint, endpoint, List.of(), List.of(), 0);
        }
        int[] starts = {0, 1, 2, 3, 6, 9, 15, 16, 134, 137, 143, 148, 149, 150};
        for (int start : starts) {
            for (ForgeStep step : STEPS) {
                int target = start + step.step();
                String type = step.name().startsWith("HIT_") ? "HIT" : step.name();
                int expected = target >= 0 && target <= MAX_WORK ? 1 : -1;
                solveChecked(start, target, List.of(), List.of(type + "_LAST"), expected);
            }
        }
        // Both require a legal detour: a greedy exact delta is not available.
        solveChecked(0, 1, List.of(), List.of(), 2);
        solveChecked(MAX_WORK, MAX_WORK - 1, List.of(), List.of(), 2);
        // The target already equals current work, but an unmet rule still requires a nonempty loop.
        var loopRules = List.of("HIT_LAST");
        int expected = oracleDistances(0, List.of(), enumRules(loopRules))[0];
        check(expected > 0, "Independent zero-work loop must require actions");
        solveChecked(0, 0, List.of(), loopRules, expected);
    }

    private static void priorHistoryAndNoMutation() {
        List<Solver.Action> full = List.of(Solver.Action.PUNCH, Solver.Action.HIT_HARD,
            Solver.Action.SHRINK, Solver.Action.DRAW, Solver.Action.BEND);
        List<Optional<List<Solver.Action>>> threeStepResults = new ArrayList<>();
        for (int length : new int[] {0, 1, 2, 3, 5}) {
            List<Solver.Action> history = new ArrayList<>(full.subList(0, length));
            ForgeSteps actual = steps(history);
            for (ForgeRule rule : RULES) {
                List<String> rules = new ArrayList<>(List.of(rule.name()));
                Optional<List<Solver.Action>> result = solveChecked(75, 75, history, rules, UNCHECKED_DISTANCE);
                check(result.isPresent(), "Single TFC rule is reachable from middle work: " + rule);
                check(result.orElseThrow().isEmpty() == rule.matches(actual),
                    "Empty route must reflect newest-first prior history of length " + length + ": " + rule);
                if (length == 3) threeStepResults.add(result);
                if (length == 5) {
                    check(result.equals(threeStepResults.get(rule.ordinal())),
                        "Actions older than third-last changed solution: " + rule);
                }
            }
        }
    }

    private static void contradictionsAndImpossibleEndpoints() {
        List<List<String>> contradictions = List.of(
            List.of("HIT_LAST", "DRAW_LAST"),
            List.of("HIT_SECOND_LAST", "BEND_SECOND_LAST"),
            List.of("PUNCH_THIRD_LAST", "SHRINK_THIRD_LAST"),
            List.of("HIT_ANY", "DRAW_ANY", "PUNCH_ANY", "BEND_ANY"));
        for (List<String> names : contradictions) {
            List<ForgeRule> rules = enumRules(names);
            for (int h = 0; h < CATEGORY_HISTORIES; h++) {
                check(!accepted(steps(h), rules), "Contradiction fixture was satisfiable: " + names);
            }
            solveChecked(0, 75, List.of(), names, -1);
            solveChecked(75, 75, List.of(Solver.Action.HIT_HARD), names, -1);
        }
        // A positive final action cannot finish at 0; a negative one cannot finish at 150.
        solveChecked(75, 0, List.of(), List.of("PUNCH_LAST"), -1);
        solveChecked(75, 0, List.of(), List.of("SHRINK_LAST"), -1);
        solveChecked(75, MAX_WORK, List.of(), List.of("HIT_LAST"), -1);
        solveChecked(75, MAX_WORK, List.of(), List.of("DRAW_LAST"), -1);
        // A valid prior history needs no predecessor and can already satisfy those same endpoints.
        solveChecked(0, 0, List.of(Solver.Action.PUNCH), List.of("PUNCH_LAST"), 0);
        solveChecked(MAX_WORK, MAX_WORK, List.of(Solver.Action.HIT_HARD), List.of("HIT_LAST"), 0);
        solveChecked(75, 75, List.of(Solver.Action.PUNCH), List.of("PUNCH_LAST", "PUNCH_LAST"), 0);
    }

    private static void invalidRuleNames() {
        for (String name : List.of("", "HIT", "hit_last", "UNKNOWN_LAST", "HIT_FIRST", "HIT_", "HIT_LAST_EXTRA")) {
            boolean matchesRejected = false;
            try {
                Solver.matches(0, List.of(name));
            } catch (IllegalArgumentException expected) {
                matchesRejected = true;
            }
            check(matchesRejected, "Unknown rule accepted by matches: " + name);
            boolean solveRejected = false;
            try {
                solveCalls++;
                Solver.solve(75, 75, List.of(), List.of(name));
            } catch (IllegalArgumentException expected) {
                solveRejected = true;
            }
            check(solveRejected, "Unknown rule accepted by solve: " + name);
        }
    }

    public static void main(String[] args) {
        long started = System.nanoTime();
        run("exact installed artifact, all actions/deltas/categories, runtime limit", SolverValidationTest::installedArtifactAndActions);
        run("all 343 category histories x all 30 installed TFC rules", SolverValidationTest::exhaustiveCategoryRuleParity);
        run("all real hit strengths and absent-history permutations", SolverValidationTest::allStepIdentityParity);
        run("all ordered rule pairs, contradictions, and duplicates", SolverValidationTest::orderedRulePairParity);
        run("every installed TFC anvil recipe rule conjunction", SolverValidationTest::installedRecipeRuleParity);
        run("input/transition bounds, exact single steps, detours, and zero-work loop", SolverValidationTest::boundsAndSingleSteps);
        run("newest-first history lengths 0/1/2/3/5, truncation, and immutable inputs", SolverValidationTest::priorHistoryAndNoMutation);
        run("unsatisfiable rules, impossible endpoints, and prior-history exceptions", SolverValidationTest::contradictionsAndImpossibleEndpoints);
        run("unknown rule names rejected without normalization", SolverValidationTest::invalidRuleNames);
        run("all targets: axe, fixed three positions, empty history", () -> recipeAllTargets(
            RECIPE_PREFIX + "bismuth_bronze_axe_head.json", 0, List.of()));
        run("all targets: chisel, two NOT_LAST rules, prior history", () -> recipeAllTargets(
            RECIPE_PREFIX + "bismuth_bronze_chisel_head.json", 75,
            List.of(Solver.Action.HIT_HARD, Solver.Action.DRAW)));
        run("all targets: fish hook, ANY/NOT_LAST, upper-bound start", () -> recipeAllTargets(
            RECIPE_PREFIX + "bismuth_bronze_fish_hook.json", MAX_WORK,
            List.of(Solver.Action.BEND, Solver.Action.HIT_MEDIUM, Solver.Action.UPSET)));
        run("all targets: chain, duplicate ANY, history longer than three", () -> recipeAllTargets(
            RECIPE_PREFIX + "bismuth_bronze_chain.json", 37,
            List.of(Solver.Action.PUNCH, Solver.Action.HIT_LIGHT, Solver.Action.DRAW,
                Solver.Action.SHRINK, Solver.Action.BEND)));
        System.out.printf(Locale.ROOT,
            "SolverValidationTest summary: %d groups passed, %d failed; %d assertions; %d solve calls; %.3f s%n",
            passedGroups, FAILURES.size(), checks, solveCalls, (System.nanoTime() - started) / 1_000_000_000.0);
        if (!FAILURES.isEmpty()) {
            throw new AssertionError("Solver validation failures:\n" + String.join("\n", FAILURES));
        }
    }
}
