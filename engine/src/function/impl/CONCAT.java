package function.impl;
import cell.api.Cell;

public class CONCAT extends AbstractFunctioUtils{

    @Override
    public Object apply(Cell... args) {
        Object value1 = args[0].evaluate();
        Object value2 = args[1].evaluate();
        if (isInvalidString(value1,value2)) {
            return "!UNDEFINED!";
        }
        StringBuilder result = new StringBuilder();
        for (Cell arg : args) {
            Object value = arg.evaluate();
            result.append((String) value); // Append without trimming
        }
        return result.toString(); // Return the concatenated result
    }
    @Override
    public String getName() {
        return "CONCAT";
    }


}
