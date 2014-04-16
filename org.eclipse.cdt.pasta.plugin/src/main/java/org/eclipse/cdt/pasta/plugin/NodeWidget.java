package org.eclipse.cdt.pasta.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class NodeWidget extends Composite {

    private Tree tree;

    public NodeWidget(Composite parent) {
        super(parent, SWT.NONE);
        init();
    }

    private void init() {
        this.setLayout(new FillLayout());
        this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        tree = new Tree(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setLayout(new FillLayout());
        tree.setHeaderVisible(true);
        tree.setBounds(getParent().getClientArea());
        TreeColumn nameCol = new TreeColumn(tree, SWT.LEFT);
        nameCol.setText("Name");
        nameCol.setWidth(200);
        TreeColumn valueCol = new TreeColumn(tree, SWT.LEFT);
        valueCol.setText("Value");
        valueCol.setWidth(200);
    }

    public void displayNode(IASTNode node) {
        for (TreeItem item : tree.getItems()) {
            item.dispose();
        }
        createTreeItem(tree, node.getClass().getSimpleName()+";");
        TreeItem interfaceHierarchy = createTreeItem(tree, "Interfaces;");
        for (Class<?> interfaceClass : node.getClass().getInterfaces()) {
            TreeItem interfaceItem = createTreeItem(interfaceHierarchy, interfaceClass.getSimpleName() + ";");
            for (Method method : interfaceClass.getMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    createTreeItem(interfaceItem, method.getName() + ";" + method.getReturnType().getSimpleName());
                }
            }
        }
        TreeItem typeHierarchy = createTreeItem(tree, "Superclasses;");
        addSuperclasses(typeHierarchy, node.getClass().getSuperclass());

        TreeItem fields = createTreeItem(tree, "Fields;");
        for (Field field : node.getClass().getDeclaredFields()) {
            makeFieldAccessible(field);
            createTreeItem(fields, field.getName() + ";" + getValue(field, node));
        }
        expandFirstLevel();
        tree.setVisible(true);
    }

    private void makeFieldAccessible(Field field) {
        try {
            field.setAccessible(true);
            int modifiers = field.getModifiers();
            Field modifierField = field.getClass().getDeclaredField("modifiers");
            modifiers = modifiers & ~Modifier.FINAL;
            modifierField.setAccessible(true);
            modifierField.setInt(field, modifiers);
        } catch (Exception e) {
            PastaPlugin.log(e);
        }
    }

    private void expandFirstLevel() {
        for (TreeItem item : tree.getItems()) {
            item.setExpanded(true);
        }
    }

    private void addSuperclasses(TreeItem superClasses, Class<?> clazz) {
        if (clazz != null) {
            addSuperclasses(createTreeItem(superClasses, clazz.getSimpleName() + ";"), clazz.getSuperclass());
        }
    }

    private Object getValue(Field field, IASTNode node) {
        try {
            return field.get(node);
        } catch (Exception e) {
            PastaPlugin.log(e);
            return "error loading field value";
        }
    }

    private TreeItem createTreeItem(Tree parent, String string) {
        TreeItem treeItem = new TreeItem(parent, SWT.NONE);
        return configureTreeItem(string, treeItem);
    }

    private TreeItem createTreeItem(TreeItem parent, String content) {
        TreeItem treeItem = new TreeItem(parent, SWT.NONE);
        return configureTreeItem(content, treeItem);
    }

    private TreeItem configureTreeItem(String string, TreeItem treeItem) {
        treeItem.setText(string.split(";"));
        treeItem.setExpanded(true);
        return treeItem;
    }
}
