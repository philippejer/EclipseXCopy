package org.philippejer.eclipse.xcopy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javafx.scene.shape.Circle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class XCopy implements FocusListener, Listener, DisposeListener, SelectionListener {

  @Override
  public void focusGained(FocusEvent e) {
    // System.out.println("focusGained");
  }

  @Override
  public void focusLost(FocusEvent e) {
    // System.out.println("focusLost");
    deactivate();
  }

  private void handleMoveEvent(Event event) {
    if (registeredWidgets.contains(event.widget)) {
      if (isActive()) {
        updateToolPosition(event.widget.getDisplay());
      }
    }
  }

  private void handleMouseDownEvent(Event event) {
    if (registeredWidgets.contains(event.widget)) {
//      if (event.button == 2) {
//        Display display = event.display;
//        final StyledText widget = ((StyledText) (event.widget));
//        if (!isActive()) {
//          System.out.println("handleEvent: middle button down: click");
//          event = new Event();
//          event.display = display;
//          event.type = SWT.MouseDown;
//          event.button = 1;
//          display.post(event);
//          event = new Event();
//          event.display = display;
//          event.type = SWT.MouseUp;
//          event.button = 1;
//          display.post(event);
//        } else {
//          System.out.println("handleEvent: middle button down: deactivate");
//          deactivate();
//        }
//        System.out.println("handleEvent: middle button down: paste");
//        display.asyncExec(new Runnable() {
//          @Override
//          public void run() {
//            widget.insert(selection);
//            widget.setCaretOffset(widget.getCaretOffset() + selection.length());
//          }
//        });
//      }
    }
  }

  private void handleMouseUpEvent(Event event) {
    if (registeredWidgets.contains(event.widget)) {
      if (event.button == 2) {
        Display display = event.display;
        final StyledText widget = ((StyledText) (event.widget));
        if (!isActive()) {
          System.out.println("handleEvent: middle button up: click");
          event = new Event();
          event.display = display;
          event.type = SWT.MouseDown;
          event.button = 1;
          display.post(event);
          event = new Event();
          event.display = display;
          event.type = SWT.MouseUp;
          event.button = 1;
          display.post(event);
        } else {
          System.out.println("handleEvent: middle button up: deactivate");
          deactivate();
        }
        System.out.println("handleEvent: middle button up: paste");
        display.asyncExec(new Runnable() {
          @Override
          public void run() {
            widget.insert(selection);
            widget.setCaretOffset(widget.getCaretOffset() + selection.length());
          }
        });
      } else if (event.button == 1) {
        if (isActive()) {
          System.out.println("handleEvent: left button up while active: copy");
          deactivate();
        }
      }
    }
  }

  @Override
  public void handleEvent(Event event) {
    switch (event.type) {
      case SWT.MouseMove :
        handleMoveEvent(event);
        break;
      case SWT.MouseDown :
        handleMouseDownEvent(event);
        break;
      case SWT.MouseUp :
        handleMouseUpEvent(event);
        break;
    }
  }

  @Override
  public void widgetDisposed(DisposeEvent e) {
//		System.out.println("widgetDisposed");
    unscrollStyledText((StyledText) e.widget);
  }

  private String selection = null;

  @Override
  public void widgetSelected(SelectionEvent e) {
    System.out.println("widgetSelected: active=" + isActive() + " x=" + e.x + " y=" + e.y);
    StyledText widget = (StyledText) e.widget;
    if (e.x != e.y) {
      System.out.println("widgetSelected: selection started");
      activate(widget);
      selection = widget.getSelectionText();
    } else {
      System.out.println("widgetSelected: selection cleared");
      deactivate();
    }
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e) {
//		System.out.println("widgetDefaultSelected");
  }

  private static HashMap<Display, XCopy> registeredDisplays = new HashMap<Display, XCopy>();

  private HashSet<StyledText> registeredWidgets = new HashSet<StyledText>();
  private Display registeredDisplay;

  private XCopy(Display display) {
    registeredDisplay = display;
    display.addFilter(SWT.MouseMove, this);
    display.addFilter(SWT.MouseDown, this);
    display.addFilter(SWT.MouseUp, this);
  }

  public synchronized static void addStyledText(StyledText widget) {
    Display display = widget.getDisplay();
    XCopy xMouse = (XCopy) registeredDisplays.get(display);
    if (xMouse == null) {
      xMouse = new XCopy(display);
      registeredDisplays.put(display, xMouse);
    }
    xMouse.scrollStyledText(widget);
  }

  private void scrollStyledText(StyledText widget) {
    if (registeredWidgets.contains(widget)) return;
    registeredWidgets.add(widget);
    widget.addFocusListener(this);
    widget.addDisposeListener(this);
    widget.addSelectionListener(this);
  }

  public synchronized static void removeStyledText(StyledText widget) {
    XCopy xMouse = (XCopy) registeredDisplays.get(widget.getDisplay());
    if (xMouse != null) xMouse.unscrollStyledText(widget);
  }

  public synchronized static void disposeAll() {
    Set<Display> keySet = new HashSet<Display>(registeredDisplays.keySet());
    for (Iterator<Display> iterator = keySet.iterator(); iterator.hasNext();) {
      Display display = iterator.next();
      final XCopy xMouse = registeredDisplays.get(display);
      display.asyncExec(new Runnable() {
        @Override
        public void run() {
          xMouse.dispose();
        }
      });
    }
  }

  private void unscrollStyledText(StyledText widget) {
    if (!registeredWidgets.contains(widget)) return;
    registeredWidgets.remove(widget);
    widget.removeFocusListener(this);
    widget.removeDisposeListener(this);
    widget.removeSelectionListener(this);
    if (registeredWidgets.size() == 0) dispose();
  }

  private void dispose() {
    for (Iterator<StyledText> iterator = registeredWidgets.iterator(); iterator.hasNext();) {
      StyledText widget = (StyledText) iterator.next();
      widget.removeFocusListener(this);
      widget.removeDisposeListener(this);
      widget.removeSelectionListener(this);
    }
    registeredWidgets.clear();

    registeredDisplays.remove(registeredDisplay);
    registeredDisplay.removeFilter(SWT.MouseMove, this);
    registeredDisplay.removeFilter(SWT.MouseDown, this);
    registeredDisplay.removeFilter(SWT.MouseUp, this);

    if (cursorTool != null) {
      cursorTool.dispose();
      cursorTool = null;
    }
  }

  private boolean active = false;

  private boolean isActive() {
    return active;
  }

  private Shell cursorTool = null;

  private void createCursorTool(Display display) {
    cursorTool = new Shell(display, SWT.NO_TRIM | SWT.TOOL | SWT.ON_TOP | SWT.TRANSPARENT);
    cursorTool.setSize(8, 8);
    cursorTool.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
    Region region = new Region();
    region.subtract(0, 0, 7, 7);

//    region.add(new int[]{1, 0, 7, 6, 7, 7, 6, 7, 0, 1});
//    region.add(new int[]{6, 0, 0, 6, 0, 7, 1, 7, 7, 1});

    region.add(new int[]{3, 0, 3, 7, 4, 7, 4, 0, 3, 0});
    region.add(new int[]{0, 3, 0, 4, 7, 4, 7, 3, 0, 3});

    cursorTool.setRegion(region);
  }

  private void updateToolPosition(Display display) {
//    Point cursorLocation = display.getCursorLocation();
//    cursorTool.setLocation(cursorLocation.x + 8, cursorLocation.y + 8);
//    cursorTool.setVisible(true);
  }

  private void activate(StyledText text) {
    if (isActive()) {
      return;
    }

    Display display = text.getDisplay();
    if (cursorTool == null) {
      createCursorTool(display);
    }
    updateToolPosition(display);

    active = true;
  }

  private void deactivate() {
    if (!isActive()) {
      return;
    }

    cursorTool.dispose();
    cursorTool = null;

    active = false;
  }
}