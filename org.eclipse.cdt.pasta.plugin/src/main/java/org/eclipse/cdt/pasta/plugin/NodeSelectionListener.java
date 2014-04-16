package org.eclipse.cdt.pasta.plugin;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public interface NodeSelectionListener {
    
    void nodeSelected(IASTNode node);
}
