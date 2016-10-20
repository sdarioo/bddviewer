package com.sdarioo.bddtamer.ui.tree;


import com.sdarioo.bddtamer.Plugin;
import com.sdarioo.bddtamer.launcher.TestResult;
import com.sdarioo.bddtamer.model.Scenario;

import java.util.function.Function;

public enum BddTreeColumns {

    NAME("Name", modelObject -> modelObject),
    REQUIREMENT("L2", BddTreeColumns::getRequirement),
    RUN_TIME("Exec. Time", BddTreeColumns::getExecutionTime);

    private final String name;
    private final Function<Object, Object> valueProvider;

    BddTreeColumns(String name, Function<Object, Object> valueProvider) {
        this.name = name;
        this.valueProvider = valueProvider;
    }

    public String getName() {
        return name;
    }

    public Object getValue(Object modelObject) {
        return valueProvider.apply(modelObject);
    }

    private static Object getRequirement(Object modelObject) {
        return (modelObject instanceof Scenario) ? ((Scenario)modelObject).getMeta().getRequirements() : "";
    }

    private static Object getExecutionTime(Object modelObject) {
        if (modelObject instanceof Scenario) {
            Scenario scenario = (Scenario)modelObject;
            TestResult result = Plugin.getInstance().getSessionManager().getResult(scenario);
            if (result != null) {
                return String.valueOf(result.getTime());
            }
        }
        return "";
    }
}
