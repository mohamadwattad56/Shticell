package function.impl;

import cell.api.Cell;

public class AND extends AbstractFunctioUtils{

    @Override
    public Object apply(Cell... args) {
        Object firstValue = args[0].evaluate();
        Object secondValue = args[1].evaluate();
        if (!firstValue.toString().equalsIgnoreCase("true") &&!firstValue.toString().equalsIgnoreCase("false") ) {
            return "UNKNOWN";
        }
        if (!secondValue.toString().equalsIgnoreCase("true") &&!secondValue.toString().equalsIgnoreCase("false") ) {
            return "UNKNOWN";
        }

        return (Boolean) firstValue && (Boolean) secondValue;
    }

    @Override
    public String getName() {
        return "AND";
    }
}
