package vn.bdtg.anvil;

import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.google.gson.*;

public final class OverlayPresentationTest {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 public static void main(String[] args) throws Exception {
  int[][] pixels=new int[20][20];
  for(var rect:OverlayStyle.HIGHLIGHT) {
   check(rect.x1()>=-2 && rect.y1()>=-2 && rect.x2()<=18 && rect.y2()<=18,"highlight must stay in its existing 20x20 bounds");
   for(int y=rect.y1();y<rect.y2();y++) for(int x=rect.x1();x<rect.x2();x++) pixels[y+2][x+2]=rect.color();
  }
  check(OverlayStyle.ACCENT==0xffff2da6,"saturated magenta accent replaces the yellow highlight");
  for(int y=0;y<20;y++) for(int x=0;x<20;x++) {
   if(x==0 || x==19 || y==0 || y==19) check(pixels[y][x]==OverlayStyle.OUTLINE,"continuous dark outer keyline");
   else if(x==1 || x==18 || y==1 || y==18) check(pixels[y][x]==OverlayStyle.ACCENT,"continuous magenta inner keyline");
   else check(pixels[y][x]==0,"native 16x16 icon must remain completely unobscured");
  }
  var node=new ClassNode();
  try(var in=OverlayPresentationTest.class.getClassLoader().getResourceAsStream("vn/bdtg/anvil/AnvilOverlay.class")) { new ClassReader(in).accept(node,0); }
  Set<String> controlsPaths=new HashSet<>();int highlightCalls=0;
  for(var method:node.methods) for(var insn:method.instructions) {
   if(insn instanceof LdcInsnNode constant && "controls".equals(constant.cst)) controlsPaths.add(method.name);
   if(insn instanceof MethodInsnNode call && call.name.equals("highlight")) highlightCalls++;
  }
  check(controlsPaths.containsAll(Set.of("render","panel")),"fixed controls must appear in active route AND idle/completed/no-recipe panels");
  check(highlightCalls==2,"same contrast frame must mark next route icon and native button");
  Path root=Path.of(OverlayPresentationTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().getParent();
  for(String lang:new String[]{"en_us","vi_vn"}) {
   var json=JsonParser.parseString(Files.readString(root.resolve("src/main/resources/assets/tfg_anvil_helper/lang/"+lang+".json"))).getAsJsonObject();
   check(json.has("tfg_anvil_helper.controls"),"missing fixed control legend for "+lang);
   String text=json.get("tfg_anvil_helper.controls").getAsString();
   check(text.contains("F8") && text.contains("H") && text.contains("/"),"legend must explain both keys and start/cancel for "+lang);
  }
  System.out.println("PASS overlay presentation: persistent F8/H legend in both paths; magenta/dark double frame, zero icon occlusion");
 }
}
