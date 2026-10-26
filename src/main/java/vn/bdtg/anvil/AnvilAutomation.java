package vn.bdtg.anvil;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.*;
import net.dries007.tfc.client.screen.AnvilScreen;
import net.dries007.tfc.client.screen.button.AnvilStepButton;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.capabilities.forge.*;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.glfw.GLFW;

/** Optional one-shot client automation. Only the original TFC button sends gameplay packets. */
@Mod.EventBusSubscriber(modid=TFGAnvilHelper.ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class AnvilAutomation {
 private static final AutoForgeController AUTO=new AutoForgeController();
 private static AnvilScreen boundScreen;
 private static Object boundLevel;
 private static Item expectedItem;
 private static int expectedCount;
 private static boolean keyDown;
 private static long tick;
 private static Field leftField,topField;
 private static WeakReference<Object> lastConnection=new WeakReference<>(null);
 private static int lastMenu=-1;
 private AnvilAutomation() {}
 public static boolean active() { return AUTO.active(); }
 public static String hint() {
  String status=AUTO.status().name().toLowerCase(Locale.ROOT);
  return I18n.m_118938_("tfg_anvil_helper.auto."+status);
 }
 public static void stop() { AUTO.cancel(); clearContext(); }
 private static void clearContext() { boundScreen=null; boundLevel=null; expectedItem=null; expectedCount=0; }
 static void manualKey(int key) { if(active() && CommonInputs.m_278691_(key)) stop(); }
 static boolean bindServerMenu(Object connection,int menu) {
  if(connection==null || menu<1) return false;
  // A server-issued new menu/connection is a round-trip barrier for earlier native packets.
  // Merely cancelling, waiting, or receiving a key repeat never clears quarantine.
  if(lastConnection.get()!=connection || lastMenu!=menu) {
   AUTO.resetAfterServerMenuHandshake();lastConnection=new WeakReference<>(connection);lastMenu=menu;
  }
  return true;
 }

 @SubscribeEvent
 public static void key(ScreenEvent.KeyPressed.Pre event) {
  if(!(event.getScreen() instanceof AnvilScreen screen)) return;
  manualKey(event.getKeyCode());
  if(event.getKeyCode()!=GLFW.GLFW_KEY_F8 || event.getModifiers()!=0) return;
  event.setCanceled(true);
  if(keyDown) return;
  keyDown=true;
  if(active()) { stop(); return; }
  try {
   Minecraft mc=Minecraft.m_91087_();
   if(mc.f_91073_==null || mc.f_91074_==null || !AnvilOverlay.usable()) return;
   if(!bindServerMenu(mc.m_91403_(),screen.m_6262_().f_38840_)) return;
   clearContext(); boundScreen=screen; boundLevel=mc.f_91073_;
   var forging=screen.m_6262_().getBlockEntity().getMainInputForging();
   var recipe=forging==null?null:forging.getRecipe(mc.f_91073_);
   if(recipe!=null) {
    var output=recipe.m_8043_(mc.f_91073_.m_9598_());
    if(!output.m_41619_()) { expectedItem=output.m_41720_(); expectedCount=output.m_41613_(); }
   }
   AUTO.start(readState(mc),tick);
   AnvilOverlay.show();
   if(!active()) clearContext();
  } catch(RuntimeException | LinkageError error) { fail(error); }
 }
 @SubscribeEvent
 public static void release(ScreenEvent.KeyReleased.Pre event) { if(event.getKeyCode()==GLFW.GLFW_KEY_F8) keyDown=false; }
 @SubscribeEvent
 public static void mouse(ScreenEvent.MouseButtonPressed.Pre event) {
  if(event.getScreen()==boundScreen && active()) stop();
 }
 @SubscribeEvent
 public static void close(ScreenEvent.Closing event) {
  if(event.getScreen() instanceof AnvilScreen) { keyDown=false; if(event.getScreen()==boundScreen) stop(); }
 }
 @SubscribeEvent
 public static void tick(TickEvent.ClientTickEvent event) {
  if(event.phase!=TickEvent.Phase.END) return;
  tick++;
  if(!active()) return; // No Minecraft access, solver, list allocation or GUI work while idle.
  try {
   Minecraft mc=Minecraft.m_91087_();
   if(mc.f_91080_!=boundScreen || mc.f_91073_!=boundLevel || mc.f_91074_==null) { stop(); return; }
   var next=AUTO.poll(readState(mc),tick);
   if(next.isPresent() && !pressNative(next.get())) stop();
   if(!active()) clearContext();
  } catch(ReflectiveOperationException | RuntimeException | LinkageError error) { fail(error); }
 }
 private static AutoForgeController.State readState(Minecraft mc) {
  var anvil=boundScreen.m_6262_().getBlockEntity();
  IItemHandler inventory=anvil.getInventory();
  var input=inventory.getStackInSlot(AnvilBlockEntity.SLOT_INPUT_MAIN);
  var forging=anvil.getMainInputForging();
  var recipe=forging==null?null:forging.getRecipe(mc.f_91073_);
  if(recipe==null) {
   boolean output=!input.m_41619_() && expectedItem!=null && input.m_41720_()==expectedItem && input.m_41613_()==expectedCount;
   return new AutoForgeController.State("",0,0,0,List.of(),List.of(),false,output);
  }
  var steps=forging.getSteps();
  List<Solver.Action> history=new ArrayList<>();
  boolean absent=false,historyValid=true;
  for(var step:new ForgeStep[]{steps.last(),steps.secondLast(),steps.thirdLast()}) {
   if(step==null) absent=true;
   else { if(absent) historyValid=false; history.add(Solver.Action.valueOf(step.name())); }
  }
  var hammer=inventory.getStackInSlot(AnvilBlockEntity.SLOT_HAMMER);
  if(hammer.m_41619_()) hammer=mc.f_91074_.m_21205_();
  if(hammer.m_41619_()) hammer=mc.f_91074_.m_21206_();
  var heat=HeatCapability.get(input);
  boolean workable=historyValid && Helpers.isItem(hammer,TFCTags.Items.HAMMERS)
      && (heat==null || heat.canWork()) && anvil.canInteractWith(mc.f_91074_)
      && recipe.matches((AnvilRecipe.Inventory)inventory,mc.f_91073_);
  String identity=boundScreen.m_6262_().f_38840_+"|"+input.m_41720_()+"|"+input.m_41613_()+"|"+recipe.m_6423_();
  return new AutoForgeController.State(identity,forging.getWork(),forging.getWorkTarget(),steps.total(),history,
      Arrays.stream(recipe.getRules()).map(Enum::name).toList(),workable,false);
 }
 private static boolean pressNative(Solver.Action action) throws ReflectiveOperationException {
  if(leftField==null) {
   leftField=AbstractContainerScreen.class.getDeclaredField("f_97735_");leftField.setAccessible(true);
   topField=AbstractContainerScreen.class.getDeclaredField("f_97736_");topField.setAccessible(true);
  }
  var step=ForgeStep.valueOf(action.name());
  int x=leftField.getInt(boundScreen)+step.buttonX(),y=topField.getInt(boundScreen)+step.buttonY();
  for(var child:boundScreen.m_6702_()) {
   if(child instanceof AnvilStepButton button && button.m_252754_()==x && button.m_252907_()==y
       && button.f_93623_ && button.f_93624_) {
    button.m_5691_(); // The exact original TFC OnPress, including server-side validation.
    return true;
   }
  }
  return false;
 }
 private static void fail(Throwable error) {
  stop();System.getLogger(TFGAnvilHelper.ID).log(System.Logger.Level.ERROR,"Auto forging stopped safely",error);
 }
}
