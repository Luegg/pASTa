package org.eclipse.cdt.astview.plugin;

import org.eclipse.cdt.core.CCorePlugin;
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

public class ASTView extends ViewPart {


    @Override
    public void createPartControl(final Composite parent) {

        final TreeView treeView = new TreeView(parent);
        treeView.drawAST(getAST());
        getViewSite().getActionBars().getToolBarManager().add(new Action() {
            @Override
            public void run() {
                treeView.drawAST(getAST()); 
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

    @Override
    public void setFocus() {
        
        
    }
}