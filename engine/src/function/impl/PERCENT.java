package function.impl;

import cell.api.Cell;

public class PERCENT extends AbstractFunctioUtils{
    @Override
    public Object apply(Cell... args) {
        // Extract the evaluated values from the arguments


        Object partValue = args[0].evaluate();
        Object wholeValue = args[1].evaluate();
        String partStr = partValue.toString();
        String wholeStr = wholeValue.toString();
        // Check if both arguments are numbers
        if (!isNumeric(partStr) || !isNumeric(wholeStr)) {
            return Double.NaN;
        }


        double part = Double.parseDouble(partStr);
        double whole = Double.parseDouble(wholeStr);

        double result = (part * whole) / 100.0;
        return result;
    }

    @Override
    public String getName() {
        return "PERCENT";
    }

}
