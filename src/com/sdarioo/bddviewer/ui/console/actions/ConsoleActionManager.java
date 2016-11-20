package com.sdarioo.bddviewer.ui.console.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.sdarioo.bddviewer.ui.console.LauncherConsole;

import java.util.Arrays;
import java.util.List;

public class ConsoleActionManager {

    private final LauncherConsole console;

    private final ClearAction clearAction;
    private final ShowLogsAction showLogsAction;

    public ConsoleActionManager(LauncherConsole console) {
        this.console = console;
        this.clearAction = new ClearAction(console);
        this.showLogsAction = new ShowLogsAction(console);
    }

    public List<AnAction> getToolbarActions() {
        return Arrays.asList(clearAction, showLogsAction);
    }
}
