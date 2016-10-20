package com.sdarioo.bddtamer.ui.tree;

import com.intellij.openapi.project.Project;
import com.sdarioo.bddtamer.launcher.*;
import com.sdarioo.bddtamer.provider.StoryProvider;
import com.sdarioo.bddtamer.model.LocationHolder;
import com.sdarioo.bddtamer.model.Scenario;
import com.sdarioo.bddtamer.model.Story;
import com.sdarioo.bddtamer.ui.actions.ActionAdapter;
import com.sdarioo.bddtamer.ui.actions.BddActionManager;
import com.sdarioo.bddtamer.ui.util.IdeUtil;
import com.sdarioo.bddtamer.ui.util.TreeUtil;
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
    private DefaultTreeModel treeModel;
    private BddActionManager actionManager;


    public BddTree(Project project,
                   StoryProvider storyProvider,
                   SessionManager sessionManager) {
        this.project = project;
        this.storyProvider = storyProvider;

        initializeUI(sessionManager);
    }

    public TreeTable getTreeTable() {
        return tree;
    }

    public BddActionManager getActionManager() {
        return actionManager;
    }

    public void setActionManager(BddActionManager actionManager) {
        this.actionManager = actionManager;
    }

    public DefaultTreeTableNode getRootNode() {
        return (DefaultTreeTableNode)treeModel.getRoot();
    }

    public void reload() {
        DefaultTreeTableNode root = buildRoot();
        treeModel = createTreeModel(root);
        tree.setTreeModel(treeModel);
    }

    public void refreshNode(DefaultTreeTableNode node) {
        updateRowData(node);
        treeModel.nodeChanged(node);
    }

    private void initializeUI(SessionManager sessionManager) {
        DefaultTreeTableNode root = buildRoot();
        treeModel = createTreeModel(root);
        DefaultTreeColumnModel columnModel = new DefaultTreeColumnModel(root, getColumnNames());
        columnModel.setAllColumnsEditable(false);

        tree = new TreeTable(treeModel, columnModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setIconMap(new BddIconMap(sessionManager));

        tree.setAutoCreateRowSorter(true);
        ((DefaultTreeTableSorter)tree.getRowSorter()).setSortsOnUpdates(true);

        tree.setDragEnabled(false);
        tree.setAutoCreateRowHeader(false);
        tree.setRowSelectionAllowed(true);
        tree.setCellSelectionEnabled(false);
        tree.setColumnFocusEnabled(false);

        addTreeListeners();
        addLauncherListener(sessionManager.getLauncher());
    }

    private DefaultTreeModel createTreeModel(DefaultTreeTableNode root) {
        return new DefaultTreeModel(root);
    }

    private DefaultTreeTableNode buildRoot() {
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

    private static void updateRowData(DefaultTreeTableNode node) {
        Object[] data = getRowData(node.getUserObject());
        for (int i = 0; i < data.length; i++) {
            node.setValueAt(data[i], i);
        }
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new ActionAdapter(actionManager.getRunSelectedAction()));
        return menu;
    }

    private void addTreeListeners() {
        MouseListener listener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int row = tree.rowAtPoint(e.getPoint());
                if (row < 0) {
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (!tree.isRowSelected(row)) {
                        tree.setSelectionRow(row);
                    }
                    createPopupMenu().show(e.getComponent(), e.getX(), e.getY());

                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2) {
                        TreePath path = tree.getPathForRow(row);
                        tree.expandPath(path);
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

    private void addLauncherListener(Launcher launcher) {
        launcher.addListener(new LauncherListener() {
            @Override
            public void scenarioStarted(Scenario scenario) {
                refreshScenario(scenario, true);
            }

            @Override
            public void scenarioFinished(Scenario scenario, TestResult result) {
                refreshScenario(scenario, false);
            }

            @Override
            public void sessionStarted(List<Scenario> scope) {
                scope.forEach( scenario -> refreshScenario(scenario, false));
            }

            @Override
            public void sessionFinished() {
            }

            private void refreshScenario(Scenario scenario, boolean scrollTo) {
                DefaultTreeTableNode node = TreeUtil.findNode((DefaultTreeTableNode) treeModel.getRoot(), scenario);
                if (node != null) {
                    SwingUtilities.invokeLater(() -> {
                        if (scrollTo) {
                            tree.scrollPathToVisible(TreeUtil.pathToRoot(node));
                        }
                        refreshNode(node);
                    });
                } else {
                    LOGGER.warn("Cannot find node for scenario: " + scenario);
                }
            }
        });
    }

}
