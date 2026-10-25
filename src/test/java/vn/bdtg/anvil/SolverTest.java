package vn.bdtg.anvil;

import java.util.*;

public final class SolverTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        var plan = Solver.solve(0, 16, List.of(), List.of());
        check(plan.isPresent(), "reachable target");
        check(plan.get().equals(List.of(Solver.Action.SHRINK)), "one shrink is the shortest solution");
        System.out.println("PASS shortest single-step route");
        var constrained = Solver.solve(0, 16, List.of(), List.of("HIT_LAST"));
        check(constrained.isPresent(), "hit-last target reachable");
        var route = constrained.get();
        check(route.get(route.size()-1).name().startsWith("HIT_"), "HIT_LAST must end in a hit");
        check(route.size() == 4, "16 with HIT_LAST requires exactly four actions");
        System.out.println("PASS hit-last constraint");
    }
}
