package vn.bdtg.anvil;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.jar.JarFile;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;

/** Check the dependency range against the TFC version actually loaded for tests. */
public final class ModCompatibilityTest {
 public static void main(String[] args) throws Exception {
  Path root=Path.of(ModCompatibilityTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().getParent();
  var parser=new TomlParser();
  var helper=parser.parse(new StringReader(Files.readString(root.resolve("src/main/resources/META-INF/mods.toml"))));
  Path core=Path.of(ForgeRule.class.getProtectionDomain().getCodeSource().getLocation().toURI());
  String actual=null;
  try(var jar=new JarFile(core.toFile());var input=jar.getInputStream(jar.getJarEntry("META-INF/mods.toml"))) {
   var nativeConfig=parser.parse(new StringReader(new String(input.readAllBytes(),StandardCharsets.UTF_8)));
   List<? extends UnmodifiableConfig> mods=nativeConfig.get("mods");
   for(var mod:mods) if("tfc".equals(mod.get("modId"))) actual=mod.get("version");
  }
  if(actual==null) throw new AssertionError("Installed Core does not declare TFC");
  List<? extends UnmodifiableConfig> dependencies=helper.get("dependencies.tfg_anvil_helper");
  String rangeText=null;
  for(var dependency:dependencies) if("tfc".equals(dependency.get("modId"))) rangeText=dependency.get("versionRange");
  if(rangeText==null) throw new AssertionError("Missing mandatory TFC dependency");
  var range=VersionRange.createFromVersionSpec(rangeText);
  if(!range.containsVersion(new DefaultArtifactVersion(actual))) {
   throw new AssertionError("Helper TFC range "+rangeText+" rejects installed TFC "+actual+" from "+core.getFileName());
  }
  if(!"3.2.28".equals(actual)) throw new AssertionError("This release must validate exactly TFC 3.2.28");
  for(String unverified:new String[]{"3.2.25","3.2.27","3.2.29","4.0.0"}) {
   if(range.containsVersion(new DefaultArtifactVersion(unverified))) throw new AssertionError("Unverified TFC version accepted: "+unverified);
  }
  System.out.println("PASS native TOML/Maven dependency check: installed TFC "+actual+" accepted; unverified versions rejected");
 }
}
