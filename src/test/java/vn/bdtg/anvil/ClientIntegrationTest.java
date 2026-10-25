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
 }
}
