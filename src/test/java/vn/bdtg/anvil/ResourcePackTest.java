package vn.bdtg.anvil;

import java.nio.file.*;
import java.io.*;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

/** Regression: a mods.toml does not replace pack.mcmeta for client assets. */
public final class ResourcePackTest {
 public static void main(String[] args) throws Exception {
  Path root=Path.of(ResourcePackTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().getParent();
  Path metadata=root.resolve("src/main/resources/pack.mcmeta");
  if(!Files.isRegularFile(metadata)) throw new AssertionError("Missing pack.mcmeta: Forge cannot load a valid ResourcePackInfo");
  try(InputStream in=Files.newInputStream(metadata)) {
   var pack=AbstractPackResources.m_10214_(PackMetadataSection.f_243696_,in);
   if(pack==null) throw new AssertionError("Minecraft rejected pack metadata");
   if(pack.m_10374_()!=15) throw new AssertionError("MC1.20.1 client assets need pack_format 15");
  }
  System.out.println("PASS Minecraft native resource-pack metadata parser, pack_format=15");
 }
}
