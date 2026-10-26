package vn.bdtg.anvil;
import java.util.*;

/** One explicitly started input; one outstanding native click; never retries a click. */
public final class AutoForgeController {
 public enum Status { IDLE, RUNNING, WAITING, DONE, CANCELLED, BLOCKED, DESYNC, TIMEOUT, NO_SOLUTION, UNCONFIRMED }
 public static final int INTERVAL_TICKS=4, ACK_TIMEOUT_TICKS=60;
 public record State(String identity,int work,int target,int totalSteps,List<Solver.Action> history,
                     List<String> rules,boolean workable,boolean outputMatches) {
  public State { history=List.copyOf(history); rules=List.copyOf(rules); }
 }
 private Status status=Status.IDLE;
 private List<Solver.Action> route=List.of();
 private int index;
 private State current, sentFrom;
 private Solver.Action pending;
 private long nextAllowedTick, sentTick;
 public Status status() { return status; }
 public boolean active() { return status==Status.RUNNING || status==Status.WAITING; }
 public void cancel() { if(active()) stop(Status.CANCELLED); }
 /** Only after a new server-opened menu or connection has retired prior native requests. */
 public void resetAfterServerMenuHandshake() {
  status=Status.IDLE;pending=null;route=List.of();current=null;sentFrom=null;index=0;
 }
 private void stop(Status reason) { status=reason; if(reason==Status.DONE) pending=null; }
 public boolean start(State state,long tick) {
  if(active()) return false;
  if(pending!=null) { stop(Status.UNCONFIRMED); return false; }
  if(state==null || !state.workable() || state.identity()==null || state.identity().isEmpty()) {
   stop(Status.BLOCKED); return false;
  }
  Optional<List<Solver.Action>> plan;
  try { plan=Solver.solve(state.work(),state.target(),state.history(),state.rules()); }
  catch(IllegalArgumentException error) { stop(Status.NO_SOLUTION); return false; }
  if(plan.isEmpty()) { stop(Status.NO_SOLUTION); return false; }
  route=plan.get(); index=0; pending=null; nextAllowedTick=Math.max(tick,nextAllowedTick); current=state;
  status=route.isEmpty()?Status.DONE:Status.RUNNING;
  return true;
 }
 private static boolean sameProgress(State a,State b) {
  return a.work()==b.work() && a.totalSteps()==b.totalSteps() && a.history().equals(b.history());
 }
 private boolean sameJob(State state) {
  return state!=null && Objects.equals(state.identity(),current.identity())
      && state.target()==current.target() && state.rules().equals(current.rules());
 }
 private List<Solver.Action> expectedHistory() {
  var result=new ArrayList<Solver.Action>();result.add(pending);
  result.addAll(sentFrom.history().subList(0,Math.min(2,sentFrom.history().size())));
  return result;
 }
 public Optional<Solver.Action> poll(State state,long tick) {
  if(!active()) return Optional.empty();
  if(pending!=null && tick-sentTick>=ACK_TIMEOUT_TICKS) { stop(Status.TIMEOUT); return Optional.empty(); }
  if(state!=null && state.outputMatches()) {
   stop(pending!=null && index==route.size()?Status.DONE:Status.CANCELLED);
   return Optional.empty();
  }
  if(!sameJob(state)) { stop(Status.CANCELLED); return Optional.empty(); }
  if(!state.workable()) { stop(Status.BLOCKED); return Optional.empty(); }
  if(pending!=null) {
   if(sameProgress(state,sentFrom)) {
    if(tick-sentTick>=ACK_TIMEOUT_TICKS) stop(Status.TIMEOUT);
    return Optional.empty();
   }
   if(state.work()!=sentFrom.work()+pending.delta || state.totalSteps()!=sentFrom.totalSteps()+1
      || !state.history().equals(expectedHistory())) {
    stop(Status.DESYNC); return Optional.empty();
   }
   current=state; pending=null; status=index==route.size()?Status.DONE:Status.RUNNING;
  } else if(!sameProgress(state,current)) { stop(Status.DESYNC); return Optional.empty(); }
  if(!active() || tick<nextAllowedTick) return Optional.empty();
  pending=route.get(index++); sentFrom=state; sentTick=tick;
  nextAllowedTick=tick+INTERVAL_TICKS; status=Status.WAITING;
  return Optional.of(pending);
 }
}
