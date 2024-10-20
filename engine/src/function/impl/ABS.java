package function.impl;

import cell.api.Cell;

public class ABS extends AbstractFunctionsUtils {

    @Override
    public Object apply(Cell... args) {

        Object value1 = args[0].evaluate();
        double number = convertToNumber(value1);

        // Return NaN if the number is not valid
        if (isInvalidNumber(number)) {
            return Double.NaN;
        }
        return Math.abs(number); // Since number is already a double, casting to Number is unnecessary

    }


    @Override
    public String getName() {
        return "ABS";
    }
}
