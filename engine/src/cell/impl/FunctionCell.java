package cell.impl;

import Spreadsheet.impl.Spreadsheet;
import cell.api.Cell;
import exception.InvalidCellReferenceException;
import exception.InvalidNumberOfArgs;
import exception.RangeDoesNotExist;
import function.api.Functions;

import java.util.Arrays;

import static function.impl.FunctionValidator.validate;

public class FunctionCell extends CellImpl {
    private Functions function;
    private Cell[] args;
    private final Spreadsheet spreadsheet;

    //ctor
    public FunctionCell(Object sourceValue, Functions function, Spreadsheet spreadsheet, Cell... args) {
        super(sourceValue);
        this.function = function;
        this.spreadsheet = spreadsheet;
        this.args = args;
    }


    //setters
    @Override
    public void setSourceValue(Object sourceValue) {
        this.sourceValue = sourceValue;
        if (sourceValue instanceof String) {
            String valueStr = (String) sourceValue;
            if (spreadsheet.isFunction(valueStr)) {
                this.function = spreadsheet.parseFunction(valueStr);
                this.args = spreadsheet.parseArguments(valueStr);
            } else {
                throw new IllegalArgumentException("Invalid function format: " + valueStr);
            }
        }

        try{
            updateEffectiveValue();
        }
        catch (InvalidCellReferenceException e)
        {
            throw new InvalidCellReferenceException("Invalid cell reference: " + e.getMessage());
        }
    }

    //getters

    public Functions getFunction() {
        return function;
    }

    public Cell[] getArgs() {
        return args;
    }

    @Override
    public CellType getType() {
        return CellType.FUNCTION;
    }

    //Functions
    @Override
    protected void updateEffectiveValue() {
        if (function != null && args != null) {
            // Validate the arguments before applying the function
            validate(function, args);

            //Check if numeric
            try{
                if (function.getName().equalsIgnoreCase("REF") && !isCellReference((String) args[0].getSourceValue().toString())) {
                    throw new InvalidCellReferenceException("REF function requires a valid cell reference in the format [A-Z]+[0-9], For example : 'A1'. Provided argument: " + args[0].getSourceValue());
                }

            }
            catch (Exception e) {
                throw new InvalidCellReferenceException("REF function requires a valid cell reference in the format [A-Z]+[0-9], For example : 'A1'. Provided argument: " + args[0].getSourceValue());
            }


            // Resolve the arguments
            Cell[] resolvedArgs = new Cell[args.length];
            for (int i = 0; i < args.length; i++) {
                resolvedArgs[i] = resolveArgument(args[i]);
            }

            // Handle REF to an empty cell
            if (function.getName().equalsIgnoreCase("REF")) {
                Object refValue = resolvedArgs[0].evaluate();
                if (refValue.equals("EMPTY")) {
                    effectiveValue = "EMPTY";  // The referenced cell is empty
                } else {
                    effectiveValue = refValue;
                }
            } else {
                try {
                    effectiveValue = function.apply(resolvedArgs);
                } catch (InvalidNumberOfArgs e) {
                    throw new InvalidNumberOfArgs(e.getMessage());  // Invalid function or argument types
                }catch (RangeDoesNotExist e){
                    throw new RangeDoesNotExist(e.getMessage());
                }catch (Exception e){
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        }
    }

    private Cell resolveArgument(Cell arg) {
            if (arg instanceof StringCell) {
            String cellReference = ((StringCell) arg).getSourceValue().toString();
            if (isCellReference(cellReference)) {
                CellImpl resolvedCell = spreadsheet.resolveReference(cellReference.trim());
                if (resolvedCell.getEffectiveValue() == null) {
                    return new StringCell("EMPTY"); // Handle empty cell references
                } else {
                    return resolvedCell;
                }
            } else {
                return new StringCell(cellReference);
            }
        }
        return arg;
    }

    @Override
    public boolean dependsOn(String cellId) {
        // Traverse each argument and check if it depends on the cellId
        return Arrays.stream(args).anyMatch(arg -> {
            if (arg instanceof StringCell) {
                // Check if the StringCell is a reference to the target cellId
                String refValue = ((StringCell) arg).getSourceValue().toString().trim();
                // Reference should be in the format {REF, cellId}, so extract the actual cellId
                return refValue.trim().matches(cellId);
            } else if (arg instanceof FunctionCell) {
                // Recursively check if the FunctionCell depends on the target cellId
                return ((FunctionCell) arg).dependsOn(cellId);
            }
            // Non-function and non-reference arguments do not depend on other cells
            return false;
        });
    }

    private boolean isCellReference(String arg) {
        return arg.toUpperCase().trim().matches("[A-Z][0-9]+");
    }


}
