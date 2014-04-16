package org.eclipse.cdt.pasta.plugin;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class ASTView extends ViewPart {

    @Override
    public void createPartControl(final Composite parent) {
        final ASTWidget treeView = new ASTWidget(parent);
        getViewSite().getActionBars().getToolBarManager().add(new Action() {
            @Override
            public void run() {
                treeView.drawAST(getAST()); 
            }
        });
        treeView.drawAST(getAST());
        treeView.setListener(new NodeSelectionListener() {
            
            @Override
            public void nodeSelected(IASTNode node) {
                Map<String, Object> map = new HashMap<>();
                map.put("ASTNODE", node);
                doPostEvent("ASTNODE", map);
            }
        });
    }

    private IASTTranslationUnit getAST() {
        try {
            IEditorInput editorInput = CUIPlugin.getActivePage().getActiveEditor().getEditorInput();
            IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
            IIndex index;
            index = CCorePlugin.getIndexManager().getIndex(workingCopy.getCProject());
            return workingCopy.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void doPostEvent (String topic, Map<String, Object> map) {
        Event event = new Event (topic, map);
        BundleContext ctx = FrameworkUtil.getBundle(ASTView.class).getBundleContext();
        ServiceReference ref = ctx.getServiceReference(EventAdmin.class.getName());
        if( ref != null){
            EventAdmin admin = (EventAdmin) ctx.getService(ref);
            admin.sendEvent(event);
            ctx.ungetService(ref);
        }
      }
    
    @Override
    public void setFocus() { }
}