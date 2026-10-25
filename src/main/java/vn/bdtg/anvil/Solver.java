package vn.bdtg.anvil;

import java.util.*;

/** Exact shortest route over (work, last three action categories). No game writes. */
public final class Solver {
    private Solver() {}
    private static final int HISTORIES = 343;
    public enum Action {
        HIT_LIGHT(-3, 1), HIT_MEDIUM(-6, 1), HIT_HARD(-9, 1), DRAW(-15, 2),
        PUNCH(2, 3), BEND(7, 4), UPSET(13, 5), SHRINK(16, 6);
        public final int delta, category;
        Action(int delta, int category) { this.delta = delta; this.category = category; }
    }
    /** History is newest first, exactly as TFC last/secondLast/thirdLast. */
    public static Optional<List<Action>> solve(int work, int target, List<Action> history, List<String> rules) {
        if (work < 0 || work > 150 || target < 0 || target > 150) return Optional.empty();
        boolean[] accepted = new boolean[HISTORIES];
        for (int h = 0; h < HISTORIES; h++) accepted[h] = matches(h, rules);
        int startHistory = 0, multiplier = 1;
        for (int i = 0; i < Math.min(3, history.size()); i++) {
            startHistory += history.get(i).category * multiplier; multiplier *= 7;
        }
        int start = work * HISTORIES + startHistory;
        int[] parent = new int[151 * HISTORIES];
        byte[] via = new byte[parent.length];
        Arrays.fill(parent, -1);
        int[] queue = new int[parent.length];
        int read = 0, write = 0;
        queue[write++] = start; parent[start] = start;
        Action[] actions = Action.values();
        while (read < write) {
            int here = queue[read++], position = here / HISTORIES, h = here % HISTORIES;
            if (position == target && accepted[h]) {
                var result = new ArrayList<Action>();
                while (here != start) { result.add(actions[via[here]]); here = parent[here]; }
                Collections.reverse(result);
                return Optional.of(List.copyOf(result));
            }
            for (Action action : actions) {
                int nextPosition = position + action.delta;
                if (nextPosition < 0 || nextPosition > 150) continue;
                int next = nextPosition * HISTORIES + action.category + 7 * (h % 49);
                if (parent[next] == -1) {
                    parent[next] = here; via[next] = (byte) action.ordinal(); queue[write++] = next;
                }
            }
        }
        return Optional.empty();
    }
    static boolean matches(int h, List<String> rules) {
        int last = h % 7, second = h / 7 % 7, third = h / 49;
        for (String rule : rules) {
            int split = rule.indexOf('_');
            if (split < 0) throw new IllegalArgumentException("Unknown TFC rule: " + rule);
            int category = switch (rule.substring(0, split)) {
                case "HIT" -> 1; case "DRAW" -> 2; case "PUNCH" -> 3;
                case "BEND" -> 4; case "UPSET" -> 5; case "SHRINK" -> 6;
                default -> throw new IllegalArgumentException("Unknown TFC rule: " + rule);
            };
            boolean valid = switch (rule.substring(split + 1)) {
                case "LAST" -> last == category;
                case "SECOND_LAST" -> second == category;
                case "THIRD_LAST" -> third == category;
                case "NOT_LAST" -> second == category || third == category;
                case "ANY" -> last == category || second == category || third == category;
                default -> throw new IllegalArgumentException("Unknown TFC rule: " + rule);
            };
            if (!valid) return false;
        }
        return true;
    }
}
