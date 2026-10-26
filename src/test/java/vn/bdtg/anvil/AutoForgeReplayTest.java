package vn.bdtg.anvil;

import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import net.dries007.tfc.common.capabilities.forge.*;

/** Replay the real solver/controller through actual installed TFC rules and delayed acknowledgements. */
public final class AutoForgeReplayTest {
 public static void main(String[] args) throws Exception {
  var patterns=new LinkedHashSet<List<String>>();int recipes=0;
  Path core=Path.of(ForgeRule.class.getProtectionDomain().getCodeSource().getLocation().toURI());
  try(var jar=new JarFile(core.toFile())) {
   for(var entries=jar.entries();entries.hasMoreElements();) {
    var entry=entries.nextElement();
    if(!entry.getName().startsWith("data/tfc/recipes/anvil/") || !entry.getName().endsWith(".json")) continue;
    try(var in=jar.getInputStream(entry)) {
     var json=JsonParser.parseString(new String(in.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject();
     var rules=new ArrayList<String>();
     for(var value:json.getAsJsonArray("rules")) rules.add(value.getAsString().toUpperCase(Locale.ROOT));
     patterns.add(List.copyOf(rules));recipes++;
    }
   }
  }
  if(recipes!=278) throw new AssertionError("Unexpected installed recipe inventory "+recipes);
  int jobs=0,clicks=0;
  for(var rules:patterns) for(int target:new int[]{45,75,95,125}) for(int delay:new int[]{1,7,29}) {
   var auto=new AutoForgeController();var steps=new ForgeSteps();int work=0;
   var initial=new AutoForgeController.State("phoi",0,target,0,List.of(),rules,true,false);
   var expected=Solver.solve(0,target,List.of(),rules);
   if(!auto.start(initial,0)) {
    if(expected.isPresent()) throw new AssertionError("Reachable real recipe rejected");
    continue;
   }
   Solver.Action inFlight=null;long applyAt=-1,lastSent=-100;boolean completed=false;int sent=0;
   for(long tick=0;tick<5000 && auto.active();tick++) {
    if(inFlight!=null && tick==applyAt) {
     ForgeStep nativeStep=ForgeStep.valueOf(inFlight.name());work+=nativeStep.step();steps.addStep(nativeStep);inFlight=null;
     if(work<0 || work>150) throw new AssertionError("Unsafe intermediate work");
     completed=work==target;
     for(String rule:rules) completed &= ForgeRule.valueOf(rule).matches(steps);
    }
    List<Solver.Action> history=new ArrayList<>();
    for(var step:new ForgeStep[]{steps.last(),steps.secondLast(),steps.thirdLast()}) if(step!=null) history.add(Solver.Action.valueOf(step.name()));
    var state=new AutoForgeController.State(completed?"output":"phoi",work,target,steps.total(),history,rules,!completed,completed);
    var request=auto.poll(state,tick);
    if(request.isPresent()) {
     if(inFlight!=null) throw new AssertionError("Multiple outstanding native actions");
     if(tick-lastSent<AutoForgeController.INTERVAL_TICKS) throw new AssertionError("Send rate exceeded");
     inFlight=request.get();applyAt=tick+delay;lastSent=tick;sent++;
    }
   }
   if(auto.status()!=AutoForgeController.Status.DONE || !completed) throw new AssertionError("Real recipe replay did not finish: "+rules+" target="+target+" delay="+delay+" status="+auto.status());
   if(sent!=expected.orElseThrow().size()) throw new AssertionError("Controller sent extra/duplicate clicks");
   if(auto.poll(initial,6000).isPresent()) throw new AssertionError("Automatic batch restart is forbidden");
   jobs++;clicks+=sent;
  }
  System.out.println("PASS auto replay: "+recipes+" installed recipes / "+patterns.size()+" rule patterns, "+jobs+" jobs, "+clicks+" native-step requests; 1/7/29-tick acks, no extra clicks");
 }
}
