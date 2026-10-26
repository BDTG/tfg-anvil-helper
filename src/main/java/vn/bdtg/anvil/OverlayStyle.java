package vn.bdtg.anvil;

import java.util.List;

/** Relative, immutable UI geometry; allocated once, never on the render hot path. */
final class OverlayStyle {
 static final int ACCENT=0xffff2da6, OUTLINE=0xff070b12;
 record Rect(int x1,int y1,int x2,int y2,int color) {}
 // Two-pixel combined frame around the native 16x16 icon. No adjacent-button bleed.
 static final List<Rect> HIGHLIGHT=List.of(
  new Rect(-2,-2,18,-1,OUTLINE),new Rect(-2,17,18,18,OUTLINE),
  new Rect(-2,-1,-1,17,OUTLINE),new Rect(17,-1,18,17,OUTLINE),
  new Rect(-1,-1,17,0,ACCENT),new Rect(-1,16,17,17,ACCENT),
  new Rect(-1,0,0,16,ACCENT),new Rect(16,0,17,16,ACCENT)
 );
 private OverlayStyle() {}
}
