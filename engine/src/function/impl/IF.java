package function.impl;

import cell.api.Cell;

public class IF extends AbstractFunctioUtils{
    @Override
    public Object apply(Cell... args) {
        Object condition = args[0].evaluate();
        Object val2 = args[1].evaluate();
        Object val3 = args[2].evaluate();
        if (condition.toString().equalsIgnoreCase("true")) {
           return val2;
        } else if (condition.toString().equalsIgnoreCase("false")) {
            return val3;
        }
        return "UNKNOWN";

    }

    @Override
    public String getName() {
        return "IF";
    }
}
