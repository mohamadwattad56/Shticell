package function.impl;

import cell.api.Cell;


public class BIGGER extends AbstractFunctionsUtils {

    @Override
    public Object apply(Cell... args) {
        Object firstValue = args[0].evaluate();
        Object secondValue = args[1].evaluate();

        if (isNumeric(firstValue.toString()) && isNumeric(secondValue.toString())) {
            return (Double.parseDouble(firstValue.toString())) >= (Double.parseDouble(secondValue.toString()));
        }

        return "UNKNOWN";
    }

    @Override
    public String getName() {
        return "BIGGER";
    }

}