package function.impl;

import cell.api.Cell;

public class EQUAL extends AbstractFunctionsUtils {

    @Override
    public Object apply(Cell... args) {
        Object firstValue = args[0].evaluate();
        Object secondValue = args[1].evaluate();

        // Check if both values are of the same type
        if (firstValue.getClass().equals(secondValue.getClass())) {
            // Check if both values are equal
            if (firstValue.equals(secondValue)) {
                return true; // Return true if both type and value are the same
            }
        }

        return false; // Return false if either the type or value is different
    }


    @Override
    public String getName() {
        return "EQUAL";
    }
}
