package org.eclipse.cdt.astview;

import org.eclipse.cdt.astview.plugin.ASTViewPlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.utils.EclipseObjects;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.ViewPart;

import ch.jbaum.lib.Node;
import ch.jbaum.lib.NodeVisitor;

public class ASTView extends ViewPart {

    private static final int NODE_HEIGHT = 40;

    public ASTView() {

    }

    @Override
    public void createPartControl(final Composite parent) {

        final Canvas treeCanvas = new Canvas(parent, SWT.NONE);

        IEditorInput editorInput = EclipseObjects.getActiveEditor().getEditorInput();
        IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
        IIndex index;
        try {
            index = CCorePlugin.getIndexManager().getIndex(workingCopy.getCProject());
            IASTTranslationUnit ast = workingCopy.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
            final Node<Button> root = constructTree(ast, treeCanvas);
            root.adjust();
            setButtonPositions(root);
            root.data().setVisible(true);
            parent.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseDown(MouseEvent e) {
                    Button button = new Button(parent, SWT.NONE);
                    button.setVisible(false);
                    super.mouseDown(e);
                }

            });
            treeCanvas.addPaintListener(new PaintListener() {

                @Override
                public void paintControl(final PaintEvent e) {
                   
                    root.visit(new NodeVisitor<Button>() {

                        @Override
                        public void visit(Node<Button> node) {
                            if (node.data().isVisible()) {
                                if (node.parent() != null) {
                                    drawLineToParent(e, node);
                                }
                            }
                        }
                    });

                }

                private void drawLineToParent(PaintEvent e, Node<?> node) {

                    int parentX = (int) (getXCoord(node.parent()) + ((node.parent().width() * 8) / 2));
                    int parentY = getYCoord(node.parent()) + (NODE_HEIGHT / 2);
                    int nodeX = (int) (getXCoord(node) + (node.width() * 9f / 2));
                    int nodeY = getYCoord(node) + (NODE_HEIGHT / 2);
                    e.gc.drawLine(nodeX, nodeY, parentX, parentY);

                }

            });
        } catch (CoreException e) {
            ASTViewPlugin.log(e);
        }

    }

    private void setButtonPositions(final Node<Button> node) {
        System.out.println("draw node" + node.data().getClass().getSimpleName());
        node.data().setBounds(getXCoord(node), getYCoord(node), (int) (node.width() * 9f), NODE_HEIGHT);
        for (Node<Button> child : node.children()) {
            setButtonPositions(child);
        }
    }

    private int getYCoord(Node<?> node) {
        return (int) (node.y() * 50f);
    }

    private int getXCoord(Node<?> node) {
        return (int) (node.x() * 10f);
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }

    public Node<Button> constructTree(IASTNode astNode, Composite parent) {

        final Node<Button> node = new Node<>(new Button(parent, SWT.NONE));
        // node.setWidth(node.data().getClass().getSimpleName().length());
        node.data().setText(astNode.getClass().getSimpleName());
        node.setWidth(astNode.getClass().getSimpleName().length());
        node.data().setVisible(false);
        node.data().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                for (Node<Button> child : node.children()) {
                    child.data().setVisible(true);
                }
            }
        });
        for (IASTNode child : astNode.getChildren()) {
            node.addChild(constructTree(child, parent));
        }
        return node;
    }
}
