package function.impl;

import Spreadsheet.impl.Spreadsheet;
import cell.api.Cell;
import cell.impl.CellImpl;
import function.api.Functions;

public class REF implements Functions {

    private Spreadsheet spreadsheet;

    public REF(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

   @Override
    public Object apply(Cell... args) {
        CellImpl referencedCell = (CellImpl) args[0];
        return referencedCell.evaluate();
    }

    @Override
    public String getName() {
        return "REF";
    }

}
