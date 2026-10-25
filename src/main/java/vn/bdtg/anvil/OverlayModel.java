package vn.bdtg.anvil;
import java.util.*;
/** Snapshot cache avoids running search every rendered frame. */
public final class OverlayModel {
 public record Snapshot(int work, int target, List<Solver.Action> history, List<String> rules,
                        Optional<List<Solver.Action>> route) {}
 private Snapshot previous;
 public Snapshot update(int work,int target,List<Solver.Action> history,List<String> rules) {
  if(previous!=null && previous.work()==work && previous.target()==target
    && previous.history().equals(history) && previous.rules().equals(rules)) return previous;
  previous=new Snapshot(work,target,List.copyOf(history),List.copyOf(rules),Solver.solve(work,target,history,rules));
  return previous;
 }
}
