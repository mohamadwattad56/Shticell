package function.impl;

import cell.api.Cell;

public class TIMES extends AbstractFunctionsUtils {

    @Override
    public Object apply(Cell... args) {
        Object firstValue = args[0].evaluate();
        Object secondValue = args[1].evaluate();

        double firstNumber = convertToNumber(firstValue);
        double secondNumber = convertToNumber(secondValue);

        // Check if either number is NaN
        if (isInvalidNumber(firstNumber, secondNumber)) {
            return Double.NaN;
        }

        return Math.pow(firstNumber, secondNumber);
    }


    @Override
    public String getName() {
        return "TIMES";
    }
}
