package function.impl;

import Spreadsheet.impl.Spreadsheet;
import cell.api.Cell;
import cell.impl.CellImpl;
import exception.RangeDoesNotExist;

import java.util.Set;

public class SUM extends AbstractFunctioUtils {
    private final Spreadsheet spreadsheet;

    public SUM(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }


    @Override
    public Object apply(Cell... args) {
        String rangeName = args[0].evaluate().toString();

        // Get the cells in the range from the rangeCellsMap
        Set<CellImpl> cellsInRange = spreadsheet.getCellsInRange(rangeName);
        if (cellsInRange == null || cellsInRange.isEmpty()) {
            throw new RangeDoesNotExist("The specified range '" + rangeName + "' does not exist or is empty.");
        }
        double sum = 0;
        for (CellImpl cell : cellsInRange) {
            Object value = cell.evaluate();

            if (isNumeric(value.toString())) {
                sum += Double.parseDouble(value.toString());
            }
        }
        return sum;
    }

    @Override
    public String getName() {
        return "SUM";
    }

}
