package com.sdarioo.bddviewer.ui.console;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.sdarioo.bddviewer.launcher.*;
import com.sdarioo.bddviewer.model.Scenario;
import com.sdarioo.bddviewer.model.Step;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class LauncherConsole extends AbstractConsole implements LauncherListener {

    private boolean showLogs;

    private Scenario runningScenario;
    private int currentStep;

    public LauncherConsole(Project project, SessionManager sessionManager) {
        super(project);
        sessionManager.getLauncher().addListener(this);
    }

    public boolean isShowLogs() {
        return showLogs;
    }

    public void setShowLogs(boolean value) {
        showLogs = value;
    }

    @Override
    public void sessionStarted(List<Scenario> scope) {
        clear();
    }

    @Override
    public void scenarioStarted(Scenario scenario) {
        runningScenario = scenario;
        currentStep = 0;
    }

    @Override
    public void scenarioFinished(Scenario scenario, TestResult result) {
        runningScenario = null;
        String text = result.getOutput();
        if (text != null) {
            processOutput(text);
        }
    }

    @Override
    public void sessionFinished() {
    }

    @Override
    public void outputLine(String line) {
        processLine(line);
    }

    private void processOutput(String text) {
        List<String> lines = toLines(text);
        lines.forEach(this::processLine);
    }

    private void processLine(String line) {
        if (line.trim().length() == 0) {
            return;
        }
        if (isLoggerLine(line)) {
            if (isLoggerError(line)) {
                appendText(line + LINE_SEPARATOR, ContentType.ERROR);
            } else if (showLogs) {
                appendText(line + LINE_SEPARATOR);
            }
            return;
        }
        if (runningScenario != null) {
            if (line.startsWith("Scenario: ")) {
                appendText(line.substring(0, 10), FontStyle.BOLD, JBColor.ORANGE);
                appendHyperlink(line.substring(10), project -> {
                });
            } else if (line.startsWith("Meta:")) {
                appendText(line, FontStyle.BOLD, JBColor.ORANGE);
            } else if (line.startsWith("@")) {
                int index = line.indexOf(' ');
                appendText(line.substring(0, index), FontStyle.BOLD, JBColor.ORANGE);
                appendText(line.substring(index));
            } else if (isStep(line)) {
                int index = line.indexOf(' ');
                appendText(line.substring(0, index), FontStyle.BOLD, JBColor.ORANGE);
                appendText(line.substring(index));

                Step step = getCurrentStep(line);
                if (step != null) {
                    currentStep++;
                    appendText(" [PASSED]", FontStyle.BOLD, JBColor.GREEN);
                }

            } else if (line.startsWith("|")) {
                appendText("    " + line, null, JBColor.GRAY);
            } else if (line.startsWith("Example: ")) {
                currentStep = 0;
                appendText(line, null, JBColor.YELLOW);
            } else {
                appendText(line, null, JBColor.YELLOW);
            }
        } else {
            appendText(line);
        }
        appendText(LINE_SEPARATOR);
    }

    private Step getCurrentStep(String text) {
        List<Step> steps = runningScenario.getSteps();
        if (currentStep >= steps.size()) {
            return null;
        }
        Step step = steps.get(currentStep);
        if (text.equals(step.getText())) {
            return step;
        }
        String pattern = step.getPattern();
        if (!pattern.equals(step.getText())) {
            MessageFormat format = new MessageFormat(step.getPattern());
            try {
                format.parse(text);
                return step;
            } catch (ParseException e) {
            }
        }
        return null;
    }

    private static List<String> toLines(String text) {
        return Arrays.stream(text.split("\n")).map(String::trim).collect(Collectors.toList());
    }

    private static boolean isLoggerLine(String line) {
        return isLoggerError(line) || line.contains("WARN") || line.contains("DEBUG") || line.contains("INFO");
    }

    private static boolean isLoggerError(String line) {
        return line.contains("ERROR");
    }

    public static boolean isStep(String line) {
        return line.startsWith("Given ") || line.startsWith("And ") || line.startsWith("When ") || line.startsWith("Then ");
    }
}