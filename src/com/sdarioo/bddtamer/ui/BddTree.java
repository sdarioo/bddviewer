package com.sdarioo.bddtamer.ui;

import com.intellij.openapi.project.Project;
import com.sdarioo.bddtamer.StoryProvider;
import com.sdarioo.bddtamer.model.LocationHolder;
import com.sdarioo.bddtamer.model.Story;
import com.sdarioo.bddtamer.ui.util.IdeUtil;
import de.sciss.treetable.j.DefaultTreeColumnModel;
import de.sciss.treetable.j.DefaultTreeTableNode;
import de.sciss.treetable.j.DefaultTreeTableSorter;
import de.sciss.treetable.j.TreeTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class BddTree {

    private static final Logger LOGGER = LoggerFactory.getLogger(BddTree.class);

    private final Project project;
    private final StoryProvider storyProvider;

    private TreeTable tree;
    private FilteredTreeModel treeModel;


    public BddTree(Project project, StoryProvider storyProvider) {
        this.project = project;
        this.storyProvider = storyProvider;

        initializeUI();
    }

    public TreeTable getTreeTable() {
        return tree;
    }

    public Project getProject() {
        return project;
    }

    public void refresh() {
        DefaultTreeTableNode root = buildRoot();
        treeModel = createTreeModel(root);
        tree.setTreeModel(treeModel);
    }

    private void initializeUI() {
        DefaultTreeTableNode root = buildRoot();
        treeModel = createTreeModel(root);
        DefaultTreeColumnModel columnModel = new DefaultTreeColumnModel(root, getColumnNames());
        columnModel.setAllColumnsEditable(false);

        tree = new TreeTable(treeModel, columnModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setIconMap(new BddIconMap());

        tree.setAutoCreateRowSorter(true);
        ((DefaultTreeTableSorter)tree.getRowSorter()).setSortsOnUpdates(true);

        tree.setDragEnabled(false);
        tree.setAutoCreateRowHeader(false);
        tree.setRowSelectionAllowed(true);
        tree.setCellSelectionEnabled(false);
        tree.setColumnFocusEnabled(false);


        addTreeListeners();
    }

    private FilteredTreeModel createTreeModel(DefaultTreeTableNode root) {
        return new FilteredTreeModel(new DefaultTreeModel(root));
    }

    private DefaultTreeTableNode buildRoot()
    {
        DefaultTreeTableNode root = createNode(project);

        List<Story> stories = storyProvider.getStories(project);
        stories.forEach(story -> {
            DefaultTreeTableNode storyNode = createNode(story);
            root.add(storyNode);
            story.getScenarios().forEach(scenario -> storyNode.add(createNode(scenario)));
        });
        return root;
    }

    private static DefaultTreeTableNode createNode(Object modelObject) {
        DefaultTreeTableNode node = new DefaultTreeTableNode(getRowData(modelObject));
        node.setUserObject(modelObject);
        return node;
    }

    private static String[] getColumnNames() {
        return Arrays.stream(BddTreeColumns.values())
                .map(BddTreeColumns::getName)
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }

    private static Object[] getRowData(Object modelObject) {
        return Arrays.stream(BddTreeColumns.values())
                .map(c -> c.getValue(modelObject))
                .collect(Collectors.toList())
                .toArray(new Object[0]);
    }

    private void addTreeListeners() {
        MouseListener listener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int row = tree.rowAtPoint(e.getPoint());
                if (row < 0) {
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    tree.setSelectionRow(row);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2) {
                        TreePath path = tree.getPathForRow(row);
                        Object modelObject = ((DefaultTreeTableNode) path.getLastPathComponent()).getUserObject();
                        if (modelObject instanceof LocationHolder) {
                            IdeUtil.openInEditor(project, ((LocationHolder)modelObject).getLocation());
                        }
                    }
                }
            }
        };
        tree.addMouseListener(listener);
    }

}
