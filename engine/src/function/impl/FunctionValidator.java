package function.impl;

import cell.api.Cell;
import exception.InvalidNumberOfArgs;
import function.api.Functions;

public class FunctionValidator {
    public static final int UNARY_NUMBER_OF_ARGS = 1;
    public static final int BINARY_NUMBER_OF_ARGS = 2;
    public static final int TERNARY_NUMBER_OF_ARGS = 3;

    public static void validate(Functions function, Cell[] args) {
        String functionName = function.getName().toUpperCase();

        switch (functionName) {
            case "PLUS", "POW", "MOD", "DIVIDE", "TIMES", "MINUS", "ABS", "REF","EQUAL","AND", "BIGGER", "AVERAGE", "CONCAT", "SUB", "NOT", "IF", "LESS", "OR", "PERCENT","SUM" :
            CheckNumberOfArgs(args,functionName);
            break;
            default:
                throw new IllegalArgumentException("Unknown function: " + functionName);

        }

    }

    private static void CheckNumberOfArgs(Cell[] args, String functionName) {
        int numberOfArgs = determineNumberOfArgs(functionName);
        if (args.length != numberOfArgs) {
            throw new InvalidNumberOfArgs(functionName + " function requires exactly " + numberOfArgs + " arguments. Provided: " + args.length);
        }

    }

    private static int determineNumberOfArgs(String functionName) {
        return switch (functionName) {
            case "ABS", "REF", "AVERAGE", "SUM", "NOT" -> UNARY_NUMBER_OF_ARGS;
            case "SUB", "IF" -> TERNARY_NUMBER_OF_ARGS;
            default -> BINARY_NUMBER_OF_ARGS;
        };
    }

}
