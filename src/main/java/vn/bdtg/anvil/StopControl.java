package vn.bdtg.anvil;

import java.lang.ref.WeakReference;

/** Hitbox belongs only to the screen that actually rendered it, without retaining that screen. */
final class StopControl {
 private WeakReference<Object> owner=new WeakReference<>(null);
 private int x,width;
 private boolean shown,armed;
 void show(Object screen,int panelX,int panelWidth,int labelWidth) {
  if(screen==null || panelWidth<40) { hide();return; }
  if(owner.get()!=screen) owner=new WeakReference<>(screen);
  width=Math.min(panelWidth-12,Math.max(24,labelWidth+12));x=panelX+panelWidth-6-width;
  shown=true;armed=true;
 }
 void hide() { shown=false;armed=false;owner.clear(); }
 int x() { return x; }
 int y() { return 6; }
 int width() { return width; }
 int height() { return 14; }
 boolean press(Object screen,double px,double py,int mouseButton,Runnable stop) {
  if(!shown || screen==null || owner.get()!=screen || mouseButton!=0
      || px<x || px>=x+width || py<y() || py>=y()+height()) return false;
  if(armed) { armed=false;stop.run(); }
  // Keep consuming the still-visible rectangle until the next render hides it.
  return true;
 }
}
