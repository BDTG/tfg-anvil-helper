package vn.bdtg.anvil;

import java.util.List;
import org.lwjgl.glfw.GLFW;

/** Real native activation-key predicate plus adapter session/quarantine behavior, without host globals. */
public final class AutoInputIntegrationTest {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 private static AutoForgeController.State state() {
  return new AutoForgeController.State("input",0,32,0,List.of(),List.of(),true,false);
 }
 public static void main(String[] args) throws Exception {
  var field=AnvilAutomation.class.getDeclaredField("AUTO");field.setAccessible(true);
  var auto=(AutoForgeController)field.get(null);Object connection=new Object();
  long now=0;
  for(int key:new int[]{GLFW.GLFW_KEY_ENTER,GLFW.GLFW_KEY_KP_ENTER,GLFW.GLFW_KEY_SPACE}) {
   now+=10;auto.resetAfterServerMenuHandshake();auto.start(state(),now);auto.poll(state(),now+1);
   AnvilAutomation.manualKey(key);
   check(!auto.active(),"native keyboard activation must cancel before callback: "+key);
   check(!auto.start(state(),now+2),"keyboard cancellation preserves outstanding request quarantine");
  }
  auto.resetAfterServerMenuHandshake();auto.start(state(),40);
  AnvilAutomation.manualKey(GLFW.GLFW_KEY_A);
  check(auto.active(),"ordinary non-activation keys must not cancel");auto.cancel();
  check(AnvilAutomation.bindServerMenu(connection,11),"valid server menu binds");
  auto.start(state(),50);auto.poll(state(),51);auto.cancel();
  check(AnvilAutomation.bindServerMenu(connection,11),"same menu remains a valid context");
  check(!auto.start(state(),52),"same server menu must not clear quarantine");
  check(!AnvilAutomation.bindServerMenu(null,12),"null connection cannot grant a rearm");
  check(!AnvilAutomation.bindServerMenu(connection,0),"player inventory menu cannot grant a rearm");
  check(!auto.start(state(),53),"invalid binding cannot bypass quarantine");
  check(AnvilAutomation.bindServerMenu(connection,12),"new server-opened menu binds");
  check(!auto.active() && auto.start(state(),55),"new server menu allows explicit start, not automatic start");
  auto.poll(state(),56);auto.cancel();
  check(AnvilAutomation.bindServerMenu(new Object(),12),"new server connection can reuse menu ID");
  check(!auto.active() && auto.start(state(),60),"new connection retires old native requests");auto.cancel();
  System.out.println("PASS auto input: real Enter/keypad Enter/Space cancellation; same-menu quarantine; new-menu/connection rearm");
 }
}
