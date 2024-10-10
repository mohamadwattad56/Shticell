package function.impl;

import cell.api.Cell;

public class NOT extends AbstractFunctioUtils{

    @Override
    public Object apply(Cell... args) {
        Object firstValue = args[0].evaluate();

        // Check if the first value is a Boolean
        if (firstValue.toString().equalsIgnoreCase("true")) {
            return "false";
        } else if (firstValue.toString().equalsIgnoreCase("false")) {
            return "true";

        }
        return "UNKNOWN";
    }

    @Override
    public String getName() {
        return "NOT";
    }
}
