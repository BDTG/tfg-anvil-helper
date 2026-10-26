package vn.bdtg.anvil;
import java.util.*;

public final class AutoForgeControllerTest {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 private static AutoForgeController.State state(int work,int target,int total,List<Solver.Action> history) {
  return new AutoForgeController.State("one-input",work,target,total,history,List.of(),true,false);
 }
 public static void main(String[] args) {
  var auto=new AutoForgeController();
  check(!auto.active(),"automatic forging must default to off");
  check(auto.start(state(0,16,0,List.of()),0),"explicit start accepts valid input");
  check(auto.poll(state(0,16,0,List.of()),1).orElseThrow()==Solver.Action.SHRINK,"first action follows real solver");
  check(auto.poll(state(0,16,0,List.of()),2).isEmpty(),"do not send while waiting for server ack");
  var output=new AutoForgeController.State("output",0,0,0,List.of(),List.of(),false,true);
  check(auto.poll(output,3).isEmpty(),"final output must not trigger another click");
  check(auto.status()==AutoForgeController.Status.DONE && !auto.active(),"one input only, then off");
  check(auto.poll(state(0,16,0,List.of()),4).isEmpty(),"new input must require another explicit start");
  System.out.println("PASS auto forging: explicit one-shot start, single in-flight action, completion stops");
  auto=new AutoForgeController();
  auto.start(state(0,32,0,List.of()),0);
  check(auto.poll(state(0,32,0,List.of()),1).orElseThrow()==Solver.Action.SHRINK,"first of two actions");
  var ack=state(16,32,1,List.of(Solver.Action.SHRINK));
  check(auto.poll(ack,2).isEmpty(),"ack must not bypass minimum click interval");
  check(auto.poll(ack,4).isEmpty(),"minimum interval is four client ticks");
  check(auto.poll(ack,5).orElseThrow()==Solver.Action.SHRINK,"confirmed action permits the next step");
  check(auto.poll(output,6).isEmpty() && !auto.active(),"two-step job stops on output");
  System.out.println("PASS auto forging: server ack and bounded send rate");
  auto=new AutoForgeController();
  var cold=new AutoForgeController.State("one-input",0,16,0,List.of(),List.of(),false,false);
  check(!auto.start(cold,0) && auto.status()==AutoForgeController.Status.BLOCKED,"cold or invalid inputs must not start");
  auto.start(state(0,32,0,List.of()),0);
  check(!auto.start(state(0,16,0,List.of()),0),"active job must not be overwritten by repeat input");
  auto.cancel();
  check(!auto.active() && auto.poll(state(0,32,0,List.of()),1).isEmpty(),"explicit cancel stops immediately");
  auto.start(state(0,32,0,List.of()),0);
  auto.poll(state(0,32,0,List.of()),1);
  var other=new AutoForgeController.State("different-input",16,32,1,List.of(Solver.Action.SHRINK),List.of(),true,false);
  check(auto.poll(other,5).isEmpty() && !auto.active(),"replacement input must cancel the entire job");
  auto=new AutoForgeController();
  auto.start(state(0,32,0,List.of()),0);
  auto.poll(state(0,32,0,List.of()),1);
  check(auto.poll(output,2).isEmpty() && auto.status()==AutoForgeController.Status.CANCELLED,"early output is not completion of planned job");
  auto=new AutoForgeController();
  auto.start(state(0,32,0,List.of()),0);
  auto.poll(state(0,32,0,List.of()),1);
  check(auto.poll(state(7,32,1,List.of(Solver.Action.BEND)),5).isEmpty()
      && auto.status()==AutoForgeController.Status.DESYNC,"unexpected manual/server progress must stop");
  auto=new AutoForgeController();
  auto.start(state(0,16,0,List.of()),0);
  auto.poll(state(0,16,0,List.of()),1);
  check(auto.poll(cold,5).isEmpty() && auto.status()==AutoForgeController.Status.BLOCKED,"loss of workability stops mid-job");
  auto=new AutoForgeController();
  auto.start(state(0,16,0,List.of()),0);
  auto.poll(state(0,16,0,List.of()),1);
  for(int t=2;t<=60;t++) check(auto.poll(state(0,16,0,List.of()),t).isEmpty(),"never resend an unacknowledged click");
  check(auto.active(),"timeout must not expire one tick early");
  check(auto.poll(state(0,16,0,List.of()),61).isEmpty() && auto.status()==AutoForgeController.Status.TIMEOUT,"bounded timeout at 60 ticks");
  check(auto.poll(output,62).isEmpty() && auto.status()==AutoForgeController.Status.TIMEOUT,"late ack cannot restart a timed-out job");
  System.out.println("PASS auto forging: workability, cancellation, input changes, desync, timeout, late ack");
  auto=new AutoForgeController(); auto.start(state(0,16,0,List.of()),0);
  auto.poll(state(0,16,0,List.of()),1);
  check(auto.poll(output,61).isEmpty() && auto.status()==AutoForgeController.Status.TIMEOUT,"an ack at the deadline is too late even without intermediate polls");
  System.out.println("PASS auto forging: timeout is checked before accepting output");
  auto=new AutoForgeController(); auto.start(state(0,32,0,List.of()),0);
  auto.poll(state(0,32,0,List.of()),1); auto.cancel();
  check(!auto.start(state(0,32,0,List.of()),2),"cancel must not forget an unacknowledged native request");
  check(auto.poll(state(0,32,0,List.of()),6).isEmpty(),"restart cannot create a second in-flight request");
  auto=new AutoForgeController(); auto.start(state(0,32,0,List.of()),0);
  auto.poll(state(0,32,0,List.of()),1); auto.poll(state(0,32,0,List.of()),61);
  check(!auto.start(state(0,32,0,List.of()),62),"timeout must not permit resending the uncertain native request");
  System.out.println("PASS auto forging: cancellation and timeout preserve the outstanding-request quarantine");
  auto.resetAfterServerMenuHandshake();
  check(!auto.active(),"new server menu may clear quarantine but never start a job");
  check(auto.start(state(0,16,0,List.of()),63),"new server menu permits a fresh explicit job");
  check(auto.poll(state(0,16,0,List.of()),64).isPresent(),"fresh menu can send its first action");
  auto=new AutoForgeController();auto.start(state(0,32,0,List.of()),0);
  auto.poll(state(0,32,0,List.of()),1);auto.poll(state(16,32,1,List.of(Solver.Action.SHRINK)),2);auto.cancel();
  check(auto.start(state(16,32,1,List.of(Solver.Action.SHRINK)),2),"cancel after confirmed ack permits explicit restart");
  check(auto.poll(state(16,32,1,List.of(Solver.Action.SHRINK)),3).isEmpty(),"restart cannot bypass previous send-rate deadline");
  check(auto.poll(state(16,32,1,List.of(Solver.Action.SHRINK)),5).isPresent(),"restart resumes at the original safe send deadline");
  System.out.println("PASS auto forging: server-menu rearm, no implicit restart, rate limit across jobs");
 }
}
