package vn.bdtg.anvil;
import net.minecraftforge.fml.common.Mod;

@Mod(TFGAnvilHelper.ID)
public final class TFGAnvilHelper {
 public static final String ID = "tfg_anvil_helper";
 public TFGAnvilHelper() {
  System.getLogger(ID).log(System.Logger.Level.INFO, "TFG Anvil Helper 1.1.1: client forging advisor loaded (automation off until explicitly started)");
 }
}
