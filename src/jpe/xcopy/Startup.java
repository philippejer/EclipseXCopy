package jpe.xcopy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class Startup implements IStartup {
  
  public static final boolean D = false;

  private class PartListener implements IPartListener2 {

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
      if (D) System.out.println("partVisible");
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
      if (D) System.out.println("partOpened");
      tryToAttach(partRef.getPart(false));
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
      if (D) System.out.println("partInputChanged");
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
      if (D) System.out.println("partHidden");
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
      if (D) System.out.println("partDeactivated");
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
      if (D) System.out.println("partClosed");
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
      if (D) System.out.println("partBroughtToTop");
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
      if (D) System.out.println("partActivated");
    }
  }

  private final PartListener mListener = new PartListener();

  @Override
  public void earlyStartup() {
    register();
  }

  private void register() {
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
    for (int i = 0; i < workbenchWindows.length; i++) {
      IWorkbenchPage[] pages = workbenchWindows[i].getPages();
      for (int j = 0; j < pages.length; j++) {
        IEditorReference[] editorReferences = pages[j].getEditorReferences();
        for (int k = 0; k < editorReferences.length; k++) {
          tryToAttach(editorReferences[k].getPart(false));
        }
      }

      IPartService partService = workbenchWindows[i].getPartService();
      partService.addPartListener(mListener);
    }
  }

  private TextViewer getTextViewer(AbstractTextEditor editor) {
    try {
      Method method = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer", new Class[]{});
      method.setAccessible(true);
      return (TextViewer) method.invoke(editor, new Object[]{});
    } catch (SecurityException e) {
      return null;
    } catch (NoSuchMethodException e) {
      return null;
    } catch (IllegalArgumentException e) {
      return null;
    } catch (IllegalAccessException e) {
      return null;
    } catch (InvocationTargetException e) {
      return null;
    } catch (ClassCastException e) {
      return null;
    }
  }

  private Object getEditor(final MultiPageEditorPart multiEditor, final int index) {
    final Object[] editor = new Object[1];
    IWorkbench workbench = PlatformUI.getWorkbench();
    workbench.getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        try {
          final Method getEditor = MultiPageEditorPart.class.getDeclaredMethod("getEditor", new Class[]{Integer.TYPE});
          getEditor.setAccessible(true);
          editor[0] = getEditor.invoke(multiEditor, new Object[]{(Object) new Integer(index)});
        } catch (SecurityException e) {
          // continue
        } catch (NoSuchMethodException e) {
          // continue
        } catch (IllegalArgumentException e) {
          // continue
        } catch (IllegalAccessException e) {
          // continue
        } catch (InvocationTargetException e) {
          // continue
        }
      }
    });
    return editor[0];
  }

  private TextViewer[] getTextViewers(final MultiPageEditorPart multiEditor) {
    ArrayList<TextViewer> textViewers = new ArrayList<TextViewer>();
    try {
      final Method getPageCount = MultiPageEditorPart.class.getDeclaredMethod("getPageCount", new Class[0]);
      getPageCount.setAccessible(true);
      int pageCount = ((Integer) getPageCount.invoke(multiEditor, new Object[0])).intValue();
      for (int i = 0; i < pageCount; i++) {
        final Object editor = getEditor(multiEditor, i);
        if (editor instanceof AbstractTextEditor) {
          TextViewer viewer = getTextViewer((AbstractTextEditor) editor);
          if (viewer != null) textViewers.add(viewer);
        }
      }
    } catch (SecurityException e) {
      // continue
    } catch (NoSuchMethodException e) {
      // continue
    } catch (IllegalArgumentException e) {
      // continue
    } catch (IllegalAccessException e) {
      // continue
    } catch (InvocationTargetException e) {
      // continue
    } catch (ClassCastException e) {
      // continue
    }
    return (TextViewer[]) textViewers.toArray(new TextViewer[0]);
  }

  private void tryToAttach(IWorkbenchPart part) {
    if (part instanceof AbstractTextEditor) {
      TextViewer viewer = getTextViewer((AbstractTextEditor) part);
      if (viewer != null) {
        final StyledText widget = viewer.getTextWidget();
        viewer.getControl().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            XCopy.addStyledText(widget);
          }
        });
      }
    }
    if (part instanceof MultiPageEditorPart) {
      TextViewer[] textViewers = getTextViewers((MultiPageEditorPart) part);
      for (int i = 0; i < textViewers.length; i++) {
        final StyledText widget = textViewers[i].getTextWidget();
        textViewers[i].getControl().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            XCopy.addStyledText(widget);
          }
        });
      }
    }
  }

}
