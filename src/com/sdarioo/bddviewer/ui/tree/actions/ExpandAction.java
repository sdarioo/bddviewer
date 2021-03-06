package com.sdarioo.bddviewer.ui.tree.actions;


import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.sdarioo.bddviewer.ui.actions.ActionBase;
import de.sciss.treetable.j.TreeTable;

public class ExpandAction extends ActionBase {

    private static final String TEXT = "Expand All";

    private final TreeTable tree;

    public ExpandAction(TreeTable tree) {
        super(TEXT, AllIcons.Actions.Expandall);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        for (int i = 0; i < tree.getRowCount(); ++i) {
            tree.expandRow(i);
        }
    }
}
