package vn.bdtg.anvil;

import java.io.*;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.resources.ResourceLocation;

/** Native Minecraft loader probe of the FINAL packaged JAR, not source files. */
public final class ResourcePackProbe {
 public static void main(String[] args) throws Exception {
  File artifact=new File(args[0]);
  Pack.Info info=Pack.m_246334_("mod:tfg_anvil_helper", name -> new FilePackResources(name,artifact,false));
  if(info==null) throw new AssertionError("JAR failed to load a valid ResourcePackInfo: "+artifact.getName());
  if(info.f_244194_()!=15) throw new AssertionError("Wrong client resource-pack format");
  try(var resources=new FilePackResources("mod:tfg_anvil_helper",artifact,false)) {
   for(String lang:new String[]{"en_us","vi_vn"}) {
    var asset=resources.m_214146_(PackType.CLIENT_RESOURCES,new ResourceLocation("tfg_anvil_helper","lang/"+lang+".json"));
    if(asset==null) throw new AssertionError("Missing native language asset: "+lang);
    try(var input=asset.m_247737_()) {
     if(input.readAllBytes().length==0) throw new AssertionError("Empty language asset");
    }
   }
  }
  System.out.println("PASS final JAR: valid native ResourcePackInfo, format=15, en_us+vi_vn readable");
 }
}
