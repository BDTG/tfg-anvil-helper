package vn.bdtg.anvil;
import java.io.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Inspect real Forge metadata without starting the Minecraft/ModLauncher globals. */
public final class ClientIntegrationTest {
 private static ClassNode read(String name) throws IOException {
  var node=new ClassNode();
  try(var in=ClientIntegrationTest.class.getClassLoader().getResourceAsStream(name+".class")) {
   if(in==null) throw new AssertionError("Missing class "+name);
   new ClassReader(in).accept(node,0);
  }
  return node;
 }
 public static void main(String[] args) throws Exception {
  ClassNode mod=read("vn/bdtg/anvil/TFGAnvilHelper");
  boolean modId=false;
  for(var a:mod.visibleAnnotations) if(a.desc.equals("Lnet/minecraftforge/fml/common/Mod;") && a.values.contains("tfg_anvil_helper")) modId=true;
  if(!modId) throw new AssertionError("mod id annotation");
  ClassNode overlay=read("vn/bdtg/anvil/AnvilOverlay");
  boolean client=false, forge=false;
  for(var a:overlay.visibleAnnotations) {
   if(a.desc.endsWith("Mod$EventBusSubscriber;")) {
    for(int i=0;i<a.values.size();i+=2) {
     String key=(String)a.values.get(i); Object value=a.values.get(i+1);
     if(key.equals("value")) {
      var sides=(java.util.List<?>)value;
      client=sides.size()==1 && ((String[])sides.get(0))[1].equals("CLIENT");
     }
     if(key.equals("bus")) forge=((String[])value)[1].equals("FORGE");
    }
   }
  }
  if(!client || !forge) throw new AssertionError("client-only Forge event registration");
  int subscribers=0;
  for(var m:overlay.methods) {
   if(m.visibleAnnotations!=null) for(var a:m.visibleAnnotations) if(a.desc.endsWith("SubscribeEvent;")) subscribers++;
   for(var insn:m.instructions) if(insn instanceof MethodInsnNode call) {
    if(call.owner.contains("network/") && !call.owner.startsWith("net/minecraft/network/chat/")) throw new AssertionError("unexpected network API");
    if(call.name.equals("onButtonPress") || call.name.equals("addStep") || call.name.equals("setRecipe")) throw new AssertionError("must never forge automatically");
   }
  }
  if(subscribers!=2) throw new AssertionError("render and key event handlers");
  // Compile-time linked signatures resolved without initializing game singletons.
  Class.forName("vn.bdtg.anvil.AnvilOverlay",false,ClientIntegrationTest.class.getClassLoader()).getDeclaredMethods();
  System.out.println("PASS Forge metadata, client-only render/key subscribers, no gameplay-write or network calls");
  ClassNode automation=read("vn/bdtg/anvil/AnvilAutomation");
  int hooks=0,nativePresses=0,manualKeyHooks=0,menuBindings=0;
  for(var m:automation.methods) {
   if(m.visibleAnnotations!=null) for(var a:m.visibleAnnotations) if(a.desc.endsWith("SubscribeEvent;")) hooks++;
   for(var insn:m.instructions) if(insn instanceof MethodInsnNode call) {
    if(m.name.equals("key") && call.name.equals("manualKey")) manualKeyHooks++;
    if(m.name.equals("key") && call.name.equals("bindServerMenu")) menuBindings++;
    if(call.name.equals("m_5691_") && call.owner.equals("net/dries007/tfc/client/screen/button/AnvilStepButton")) nativePresses++;
    if(call.owner.startsWith("net/minecraftforge/network/") || call.owner.startsWith("net/dries007/tfc/network/")) throw new AssertionError("must reuse native button, not build custom packets");
    if(java.util.Set.of("work","workRemotely","addStep","setRecipe","setTemperature","setStackInSlot").contains(call.name)) throw new AssertionError("automation may not mutate forging/inventory state directly");
   }
  }
  if(hooks!=5 || nativePresses!=1 || manualKeyHooks!=1 || menuBindings!=1) throw new AssertionError("auto must register key/release/mouse/close/tick hooks and one native button dispatch site");
  if(AnvilAutomation.active()) throw new AssertionError("automation defaults off");
  for(int i=0;i<10000;i++) AnvilAutomation.tick(new net.minecraftforge.event.TickEvent.ClientTickEvent(net.minecraftforge.event.TickEvent.Phase.END));
  if(AnvilAutomation.active()) throw new AssertionError("idle ticks must never activate automation or access Minecraft singleton");
  System.out.println("PASS auto integration: normal TFC button only, five cancellation/input hooks, idle ticks stay off");
 }
}
