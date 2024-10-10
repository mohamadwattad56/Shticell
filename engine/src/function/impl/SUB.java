package function.impl;

import cell.api.Cell;

public class SUB extends AbstractFunctioUtils {

  @Override
  public Object apply(Cell... args) {
      Object sourceValue = args[0].evaluate();
      Object startIndex = args[1].evaluate();
      Object endIndex = args[2].evaluate();
      double start = convertToNumber(startIndex);
      double end = convertToNumber(endIndex);
      if(isInvalidString(sourceValue) || isInvalidNumber(start,end)) {
          return "!UNDEFINED!";
      }

      String sourceString = (String) sourceValue;

      if (start < 0 || end >= sourceString.length() || start > end) {
              return "!UNDEFINED!";
      }
      return sourceString.substring(((Number)start).intValue(), ((Number)(end + 1)).intValue());
  }


    @Override
    public String getName() {
        return "SUB";
    }
}
