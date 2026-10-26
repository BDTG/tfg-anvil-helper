package vn.bdtg.anvil;

import java.util.List;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exercise the real cancel callback and the exact visible-button hit region without host globals. */
public final class StopControlTest {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 public static void main(String[] args) throws Exception {
  Object screen=new Object();var button=new StopControl();int[] calls={0};
  Runnable cancel=()->calls[0]++;
  check(!button.press(screen,0,0,0,cancel),"hidden control cannot consume a click");
  button.show(screen,100,300,60);
  check(button.x()>=106 && button.x()+button.width()<=394,"button stays within toolbar padding");
  check(!button.press(new Object(),button.x()+1,button.y()+1,0,cancel),"another screen cannot activate stale button bounds");
  check(!button.press(screen,button.x()+1,button.y()+1,1,cancel),"right click is not a stop-button activation");
  check(!button.press(screen,button.x()+button.width(),button.y()+1,0,cancel),"right edge is exclusive");
  check(!button.press(screen,button.x()+1,button.y()+button.height(),0,cancel),"bottom edge is exclusive");
  check(button.press(screen,button.x()+1,button.y()+1,0,cancel) && calls[0]==1,"left click invokes stop once");
  check(button.press(screen,button.x()+1,button.y()+1,0,cancel) && calls[0]==1,"repeat click is consumed without callback or click-through");
  button.hide();check(!button.press(screen,button.x()+1,button.y()+1,0,cancel),"hidden/finished UI releases hitbox");
  button.show(screen,10,40,120);check(button.width()<=28,"narrow toolbar clamps button bounds");
  button.show(null,10,300,60);check(!button.press(null,20,10,0,cancel),"null screen may not own the control");
  var auto=new AutoForgeController();var state=new AutoForgeController.State("input",0,32,0,List.of(),List.of(),true,false);
  auto.start(state,0);auto.poll(state,1);button.show(screen,100,300,60);
  check(button.press(screen,button.x()+2,button.y()+2,0,auto::cancel),"visible STOP control handles click");
  check(!auto.active() && auto.poll(state,5).isEmpty(),"button stops all further auto requests");
  check(!auto.start(state,6),"STOP preserves the unconfirmed-request quarantine");
  var node=new ClassNode();
  try(var in=StopControlTest.class.getClassLoader().getResourceAsStream("vn/bdtg/anvil/AnvilOverlay.class")) { new ClassReader(in).accept(node,0); }
  boolean highPriority=false,pressWired=false,cancelEvent=false,drawn=false;
  for(var m:node.methods) {
   if(m.name.equals("mouse")) {
    if(m.visibleAnnotations!=null) for(var a:m.visibleAnnotations) if(a.desc.endsWith("SubscribeEvent;") && a.values!=null)
     for(int i=0;i<a.values.size();i+=2) if(a.values.get(i).equals("priority")) highPriority=((String[])a.values.get(i+1))[1].equals("HIGHEST");
    for(var insn:m.instructions) if(insn instanceof MethodInsnNode call) {
     if(call.owner.equals("vn/bdtg/anvil/StopControl") && call.name.equals("press")) pressWired=true;
     if(call.name.equals("setCanceled")) cancelEvent=true;
    }
   }
   if(m.name.equals("toolbar")) for(var insn:m.instructions) if(insn instanceof LdcInsnNode ldc && "stop".equals(ldc.cst)) drawn=true;
  }
  check(highPriority && pressWired && cancelEvent && drawn,"visible STOP control must be wired before native mouse dispatch and consume its click");
  var root=java.nio.file.Path.of(StopControlTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().getParent();
  for(String lang:new String[]{"en_us","vi_vn"}) {
   var json=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(root.resolve("src/main/resources/assets/tfg_anvil_helper/lang/"+lang+".json"))).getAsJsonObject();
   check(json.has("tfg_anvil_helper.stop") && !json.get("tfg_anvil_helper.stop").getAsString().isBlank(),"STOP button needs a visible label for "+lang);
  }
  System.out.println("PASS STOP control: visible hitbox, cancel callback, no click-through/restart, scoped highest-priority mouse wiring");
 }
}
