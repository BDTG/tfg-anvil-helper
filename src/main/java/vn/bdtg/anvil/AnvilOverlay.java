package vn.bdtg.anvil;

import java.lang.reflect.Field;
import java.util.*;
import net.dries007.tfc.client.screen.AnvilScreen;
import net.dries007.tfc.common.capabilities.forge.*;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Forge event overlay; deliberately contains no click, packet, or inventory-write calls. */
@Mod.EventBusSubscriber(modid=TFGAnvilHelper.ID, value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class AnvilOverlay {
 private static final OverlayModel MODEL = new OverlayModel();
 private static boolean visible=true, failed=false;
 private static Field leftField, topField;
 private AnvilOverlay() {}

 @SubscribeEvent
 public static void key(ScreenEvent.KeyPressed.Pre event) {
  if(event.getScreen() instanceof AnvilScreen && event.getKeyCode()==72 && event.getModifiers()==0) {
   visible=!visible; event.setCanceled(true);
  }
 }

 @SubscribeEvent
 public static void render(ScreenEvent.Render.Post event) {
  if(!(event.getScreen() instanceof AnvilScreen screen) || !visible) return;
  Minecraft mc=Minecraft.m_91087_();
  if(mc.f_91073_==null) return;
  GuiGraphics graphics=event.getGuiGraphics();
  if(failed) { text(graphics,mc.f_91062_,tr("error"),8,6,0xffff7777); return; }
  try {
   var anvil=screen.m_6262_().getBlockEntity();
   Forging forging=anvil.getMainInputForging();
   AnvilRecipe recipe=forging==null ? null : forging.getRecipe(mc.f_91073_);
   if(recipe==null) { panel(graphics,mc.f_91062_,tr("choose"),tr("manual"),screen.f_96543_); return; }
   ForgeSteps steps=forging.getSteps();
   List<Solver.Action> history=new ArrayList<>();
   for(ForgeStep step : new ForgeStep[]{steps.last(),steps.secondLast(),steps.thirdLast()}) {
    if(step!=null) history.add(Solver.Action.valueOf(step.name()));
   }
   List<String> rules=Arrays.stream(recipe.getRules()).map(Enum::name).toList();
   var state=MODEL.update(forging.getWork(),forging.getWorkTarget(),history,rules);
   if(state.route().isEmpty()) { panel(graphics,mc.f_91062_,tr("no_route"),tr("manual"),screen.f_96543_); return; }
   List<Solver.Action> route=state.route().get();
   String title=tr("title",state.work(),state.target(),route.size());
   if(route.isEmpty()) { panel(graphics,mc.f_91062_,title,tr("ready"),screen.f_96543_); return; }
   int available=Math.max(120, screen.f_96543_-16);
   int columns=Math.max(1,Math.min(route.size(),available/25));
   int rows=(route.size()+columns-1)/columns;
   int width=Math.min(available,Math.max(240,columns*25+12));
   int x=Math.max(4,(screen.f_96543_-width)/2), y=4;
   graphics.m_280509_(x,y,x+width,y+34+rows*23,0xec15191f);
   graphics.m_280509_(x,y,x+width,y+1,0xffeeb85b);
   text(graphics,mc.f_91062_,title,x+6,y+5,0xffffd287);
   text(graphics,mc.f_91062_,tr("next",actionName(route.get(0))),x+6,y+17,0xffeeeeee);
   for(int i=0;i<route.size();i++) {
    Solver.Action action=route.get(i);
    ForgeStep step=ForgeStep.valueOf(action.name());
    int ix=x+6+(i%columns)*25, iy=y+31+(i/columns)*23;
    if(i==0) border(graphics,ix-2,iy-2,20,20,0xffffcd68);
    AnvilScreen.drawStepIcon(graphics,step,ix,iy);
    if(event.getMouseX()>=ix-2 && event.getMouseX()<ix+20 && event.getMouseY()>=iy-2 && event.getMouseY()<iy+20) {
     graphics.m_280557_(mc.f_91062_,net.minecraft.network.chat.Component.m_237113_((i+1)+". "+actionName(action)+" ("+(action.delta>0?"+":"")+action.delta+")"),event.getMouseX(),event.getMouseY());
    }
   }
   // SRG fields are pinned to the exact installed Minecraft 1.20.1 runtime.
   if(leftField==null) {
    leftField=AbstractContainerScreen.class.getDeclaredField("f_97735_"); leftField.setAccessible(true);
    topField=AbstractContainerScreen.class.getDeclaredField("f_97736_"); topField.setAccessible(true);
   }
   ForgeStep next=ForgeStep.valueOf(route.get(0).name());
   border(graphics,leftField.getInt(screen)+next.buttonX()-2,topField.getInt(screen)+next.buttonY()-2,20,20,0xffffcd68);
  } catch(ReflectiveOperationException | RuntimeException | LinkageError error) {
   failed=true;
   System.getLogger(TFGAnvilHelper.ID).log(System.Logger.Level.ERROR,"Anvil overlay disabled safely; no game data modified",error);
  }
 }
 private static String actionName(Solver.Action action) { return tr("action."+action.name().toLowerCase(Locale.ROOT)); }
 private static String tr(String key,Object... args) { return I18n.m_118938_("tfg_anvil_helper."+key,args); }
 private static void text(GuiGraphics graphics,Font font,String value,int x,int y,int color) { graphics.m_280488_(font,value,x,y,color); }
 private static void panel(GuiGraphics graphics,Font font,String line1,String line2,int screenWidth) {
  int width=Math.min(screenWidth-8,Math.max(font.m_92895_(line1),font.m_92895_(line2))+12);
  int x=Math.max(4,(screenWidth-width)/2);
  graphics.m_280509_(x,4,x+width,33,0xec15191f);
  text(graphics,font,line1,x+6,9,0xffffd287); text(graphics,font,line2,x+6,21,0xffcccccc);
 }
 private static void border(GuiGraphics g,int x,int y,int width,int height,int color) {
  g.m_280509_(x,y,x+width,y+1,color); g.m_280509_(x,y+height-1,x+width,y+height,color);
  g.m_280509_(x,y,x+1,y+height,color); g.m_280509_(x+width-1,y,x+width,y+height,color);
 }
}
