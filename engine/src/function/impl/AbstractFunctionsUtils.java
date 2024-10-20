package function.impl;

import cell.api.Cell;
import function.api.Functions;

abstract public class AbstractFunctionsUtils implements Functions {

    // Common logic for all subclasses
    protected double convertToNumber(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        } else {
            return Double.NaN;
        }
    }

    // Check if any number is NaN
    protected boolean isInvalidNumber(double... numbers) {
        for (double number : numbers) {
            if (Double.isNaN(number)) {
                return true;
            }
        }
        return false;
    }

    // Check if any number is NaN
    protected boolean isInvalidString(Object... args) {
        for (Object arg : args) {
            if (!(arg instanceof String) ||arg.equals("!UNDEFINED!") ||arg.equals("EMPTY") || isNumeric(arg.toString())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    // Abstract method to be implemented by subclasses
    @Override
    public abstract Object apply(Cell... args);

    // Get the name of the function
    @Override
    public abstract String getName();
}

