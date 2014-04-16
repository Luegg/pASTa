package org.eclipse.cdt.pasta.plugin;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.jbaum.lib.Node;
import ch.jbaum.lib.NodeVisitor;

public class TreeView extends ScrolledComposite {

    private Canvas canvas;
    private Node<Button> root;
    private int treeHeight;
    private int treeWidth;
    private final int NODE_HEIGHT = 20;

    public TreeView(Composite parent) {
        super(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        init();
    }

    private void init() {
        this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        canvas = new Canvas(this, SWT.NO_BACKGROUND);

        this.setContent(canvas);
        this.setExpandHorizontal(true);
        this.setExpandVertical(true);

        canvas.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(final PaintEvent e) {
                TreeView.this.setMinWidth(treeWidth);
                TreeView.this.setMinHeight(treeHeight);
                if (root != null) {
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
            }
        });
    }

    public void drawAST(IASTTranslationUnit ast) {
        clear();
        root = constructTree(ast, canvas);
        root.adjust();
        updateNodePositions(root);
        root.data().setVisible(true);
        canvas.redraw();
        canvas.update();
    }

    private void clear() {
        for (Control child : canvas.getChildren()) {
            child.dispose();
        }
    }

    private void drawLineToParent(PaintEvent e, Node<?> node) {
        int parentX = (int) (getXCoord(node.parent()) + ((node.parent().width()) / 2));
        int parentY = getYCoord(node.parent()) + (NODE_HEIGHT / 2);
        int nodeX = (int) (getXCoord(node) + ((node.width()) / 2));
        int nodeY = getYCoord(node) + (NODE_HEIGHT / 2);
        e.gc.drawLine(nodeX, nodeY, parentX, parentY);
    }

    private void updateNodePositions(final Node<Button> node) {
        if (node.parent() != null && !node.parent().data().isVisible()) {
            node.data().setVisible(false);
        }
        treeWidth = (int) (getXCoord(node) + node.width() > treeWidth ? getXCoord(node) + node.width() : treeWidth);
        treeHeight = (getYCoord(node) > treeHeight + NODE_HEIGHT ? getYCoord(node) + NODE_HEIGHT : treeHeight);
        node.data().setBounds(getXCoord(node), getYCoord(node), (int) (node.width()), NODE_HEIGHT);
        for (Node<Button> child : node.getChildren()) {
            updateNodePositions(child);
        }
    }

    private int getYCoord(Node<?> node) {
        return (int) (node.y() * 60f);
    }

    private int getXCoord(Node<?> node) {
        return (int) node.x();
    }

    private Node<Button> constructTree(final IASTNode astNode, Composite parent) {
        Button button = createButton(astNode.getClass().getSimpleName(), parent);
        final Node<Button> node = createNode(button);
        button.addMouseMoveListener(new MouseMoveListener() {

            @Override
            public void mouseMove(MouseEvent e) {
                CUIPlugin.getActivePage().getActiveEditor().getEditorSite().getSelectionProvider().setSelection(new TextSelection(astNode.getFileLocation().getNodeOffset(), astNode.getFileLocation().getNodeLength()));
            }
        });
        for (IASTNode child : astNode.getChildren()) {
            node.addChild(constructTree(child, parent));

        }
        if (node.getChildren().size() == 0) {
            Button leafButton = createButton(astNode.getRawSignature(), parent);
            leafButton.setEnabled(false);
            final Node<Button> leafNode = createNode(leafButton);
            node.addChild(leafNode);
        }
        return node;
    }

    private Button createButton(String text, Composite parent) {
        final Button button = new Button(parent, SWT.FLAT);
        button.setText(text);
        FontData fontData = button.getFont().getFontData()[0];
        fontData.setHeight(10);
        button.setFont(new Font(parent.getDisplay(), fontData));
        button.setVisible(false);
        button.pack();
        return button;
    }

    private Node<Button> createNode(Button button) {
        final Node<Button> node = new Node<>(button);
        node.setWidth(button.getBounds().width);
        node.treatAsLeaf(true);
        node.data().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                node.treatAsLeaf(!node.isTreatedAsLeaf());
                for (Node<Button> child : node.getChildren()) {
                    if (!node.isTreatedAsLeaf()) {
                        child.treatAsLeaf(true);
                    }
                    child.data().setVisible(!node.isTreatedAsLeaf());
                }
                treeWidth = 0;
                treeHeight = 0;
                if (!node.isTreatedAsLeaf()) {
                    root.adjust();
                }
                updateNodePositions(root);
                canvas.redraw();
                canvas.update();
            }
        });
        return node;
    }
}
