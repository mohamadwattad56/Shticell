package function.impl;

import Spreadsheet.impl.Spreadsheet;
import cell.api.Cell;
import cell.impl.CellImpl;
import exception.RangeDoesNotExist;

import java.util.Set;

public class AVERAGE extends AbstractFunctioUtils{

    private final Spreadsheet spreadsheet;

    public AVERAGE(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    @Override
    public Object apply(Cell... args) {
        // Extract the range name from the argument
        String rangeName = args[0].evaluate().toString();

        // Get the cells in the range from the rangeCellsMap
        try {
            Set<CellImpl> cellsInRange = spreadsheet.getCellsInRange(rangeName);

            if (cellsInRange == null || cellsInRange.isEmpty()) {
                throw new RangeDoesNotExist(" The specified range '" + rangeName + "' does not exist or is empty.");
            }


            double sum = 0;
            int count = 0;

            // Iterate through the cells and calculate the sum and count of numeric values
            for (CellImpl cell : cellsInRange) {
                Object value = cell.evaluate();

                if (isNumeric(value.toString())) {
                    sum += Double.parseDouble(value.toString());
                    count++;
                }
            }

            if (count == 0) {
                return 0;
            }

            // Calculate the average
            return sum / count;
        }
        catch (RangeDoesNotExist e) {
           throw new RangeDoesNotExist(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "AVERAGE";
    }

}
