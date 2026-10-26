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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

/** Client overlay with a cancel-only UI button; no forging, packet or inventory writes. */
@Mod.EventBusSubscriber(modid=TFGAnvilHelper.ID, value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class AnvilOverlay {
 private static final OverlayModel MODEL = new OverlayModel();
 private static final StopControl STOP=new StopControl();
 private static boolean visible=true, failed=false;
 private static Field leftField, topField;
 private AnvilOverlay() {}
 public static void show() { visible=true; }
 public static boolean usable() { return !failed; }

 @SubscribeEvent
 public static void key(ScreenEvent.KeyPressed.Pre event) {
  if(event.getScreen() instanceof AnvilScreen && event.getKeyCode()==72 && event.getModifiers()==0) {
   AnvilAutomation.stop(); visible=!visible; event.setCanceled(true);
  }
 }

 @SubscribeEvent(priority=EventPriority.HIGHEST)
 public static void mouse(ScreenEvent.MouseButtonPressed.Pre event) {
  if(!visible || failed || !(event.getScreen() instanceof AnvilScreen)) { STOP.hide();return; }
  if(STOP.press(event.getScreen(),event.getMouseX(),event.getMouseY(),event.getButton(),AnvilAutomation::stop)) event.setCanceled(true);
 }

 @SubscribeEvent
 public static void render(ScreenEvent.Render.Post event) {
  if(!(event.getScreen() instanceof AnvilScreen screen) || !visible) { STOP.hide();return; }
  Minecraft mc=Minecraft.m_91087_();
  if(mc.f_91073_==null) { STOP.hide();return; }
  GuiGraphics graphics=event.getGuiGraphics();
  if(failed) { STOP.hide();text(graphics,mc.f_91062_,tr("error"),8,6,0xffff7777); return; }
  try {
   var anvil=screen.m_6262_().getBlockEntity();
   Forging forging=anvil.getMainInputForging();
   AnvilRecipe recipe=forging==null ? null : forging.getRecipe(mc.f_91073_);
   if(recipe==null) { panel(graphics,mc.f_91062_,tr("choose"),tr("manual"),screen.f_96543_,screen); return; }
   ForgeSteps steps=forging.getSteps();
   List<Solver.Action> history=new ArrayList<>();
   for(ForgeStep step : new ForgeStep[]{steps.last(),steps.secondLast(),steps.thirdLast()}) {
    if(step!=null) history.add(Solver.Action.valueOf(step.name()));
   }
   List<String> rules=Arrays.stream(recipe.getRules()).map(Enum::name).toList();
   var state=MODEL.update(forging.getWork(),forging.getWorkTarget(),history,rules);
   if(state.route().isEmpty()) { panel(graphics,mc.f_91062_,tr("no_route"),tr("manual"),screen.f_96543_,screen); return; }
   List<Solver.Action> route=state.route().get();
   String title=tr("title",state.work(),state.target(),route.size());
   if(route.isEmpty()) { panel(graphics,mc.f_91062_,title,tr("ready"),screen.f_96543_,screen); return; }
   int available=Math.max(120, screen.f_96543_-16);
   int columns=Math.max(1,Math.min(route.size(),available/25));
   int rows=(route.size()+columns-1)/columns;
   int textWidth=Math.max(toolbarWidth(mc.f_91062_,tr("controls")),Math.max(mc.f_91062_.m_92895_(title),mc.f_91062_.m_92895_(tr("next",actionName(route.get(0))))));
   textWidth=Math.max(textWidth,mc.f_91062_.m_92895_(AnvilAutomation.hint()));
   int width=Math.min(available,Math.max(textWidth+12,Math.max(240,columns*25+12)));
   int x=Math.max(4,(screen.f_96543_-width)/2), y=4;
   graphics.m_280509_(x,y,x+width,y+50+rows*23,0xec15191f);
   graphics.m_280509_(x,y,x+width,y+1,OverlayStyle.ACCENT);
   toolbar(graphics,mc.f_91062_,tr("controls"),x,width,screen);
   text(graphics,mc.f_91062_,title,x+6,y+17,0xffffd287);
   text(graphics,mc.f_91062_,tr("next",actionName(route.get(0))),x+6,y+29,0xffeeeeee);
   text(graphics,mc.f_91062_,AnvilAutomation.hint(),x+6,y+41,0xff87d9a0);
   for(int i=0;i<route.size();i++) {
    Solver.Action action=route.get(i);
    ForgeStep step=ForgeStep.valueOf(action.name());
    int ix=x+6+(i%columns)*25, iy=y+54+(i/columns)*23;
    if(i==0) highlight(graphics,ix,iy);
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
   highlight(graphics,leftField.getInt(screen)+next.buttonX(),topField.getInt(screen)+next.buttonY());
  } catch(ReflectiveOperationException | RuntimeException | LinkageError error) {
   STOP.hide();AnvilAutomation.stop(); failed=true;
   System.getLogger(TFGAnvilHelper.ID).log(System.Logger.Level.ERROR,"Anvil overlay disabled safely; no game data modified",error);
  }
 }
 private static String actionName(Solver.Action action) { return tr("action."+action.name().toLowerCase(Locale.ROOT)); }
 private static String tr(String key,Object... args) { return I18n.m_118938_("tfg_anvil_helper."+key,args); }
 private static void text(GuiGraphics graphics,Font font,String value,int x,int y,int color) { graphics.m_280488_(font,value,x,y,color); }
 private static void panel(GuiGraphics graphics,Font font,String line1,String line2,int screenWidth,Object screen) {
  String auto=AnvilAutomation.hint(),controls=tr("controls");
  int width=Math.min(screenWidth-8,Math.max(toolbarWidth(font,controls),Math.max(font.m_92895_(auto),Math.max(font.m_92895_(line1),font.m_92895_(line2))))+12);
  int x=Math.max(4,(screenWidth-width)/2);
  graphics.m_280509_(x,4,x+width,57,0xec15191f);
  graphics.m_280509_(x,4,x+width,5,OverlayStyle.ACCENT);
  toolbar(graphics,font,controls,x,width,screen);
  text(graphics,font,line1,x+6,21,0xffffd287); text(graphics,font,line2,x+6,33,0xffcccccc);
  text(graphics,font,auto,x+6,45,0xff87d9a0);
 }
 private static int toolbarWidth(Font font,String controls) {
  return font.m_92895_(controls)+(AnvilAutomation.active()?font.m_92895_(tr("stop"))+20:0);
 }
 private static void toolbar(GuiGraphics g,Font font,String controls,int x,int width,Object screen) {
  int room=width-12;
  if(AnvilAutomation.active()) {
   String label=tr("stop");STOP.show(screen,x,width,font.m_92895_(label));
   int bx=STOP.x(),by=STOP.y(),bw=STOP.width(),bh=STOP.height();room=Math.max(0,bx-x-12);
   g.m_280509_(bx,by,bx+bw,by+bh,0xffc62948);
   g.m_280509_(bx,by,bx+bw,by+1,0xffffafc1);g.m_280509_(bx,by+bh-1,bx+bw,by+bh,0xff6b1326);
   g.m_280509_(bx,by,bx+1,by+bh,0xffffafc1);g.m_280509_(bx+bw-1,by,bx+bw,by+bh,0xff6b1326);
   text(g,font,font.m_92834_(label,bw-8),bx+6,by+3,0xffffffff);
  } else STOP.hide();
  text(g,font,font.m_92834_(controls,Math.max(0,room)),x+6,9,0xffa5e7ff);
 }
 private static void highlight(GuiGraphics g,int x,int y) {
  for(var rect:OverlayStyle.HIGHLIGHT) g.m_280509_(x+rect.x1(),y+rect.y1(),x+rect.x2(),y+rect.y2(),rect.color());
 }
}
