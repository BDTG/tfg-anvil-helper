package vn.bdtg.anvil;
import java.util.*;
public final class OverlayModelTest {
 public static void main(String[] args) {
  var model = new OverlayModel();
  var state = model.update(0,16,List.of(),List.of());
  if (state.route().isEmpty() || state.route().get().size()!=1) throw new AssertionError("overlay exposes plan");
  if (model.update(0,16,List.of(),List.of()) != state) throw new AssertionError("unchanged frames must not solve again");
  var changed=model.update(0,7,List.of(),List.of());
  if(changed==state || changed.route().get().get(0)!=Solver.Action.BEND) throw new AssertionError("change recomputes");
  System.out.println("PASS overlay state and frame cache");
 }
}
