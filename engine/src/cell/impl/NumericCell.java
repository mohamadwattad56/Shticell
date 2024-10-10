package cell.impl;

public class NumericCell extends CellImpl {

    public NumericCell(Object sourceValue) {
        super(sourceValue);
        updateEffectiveValue();
    }

    @Override
    protected void updateEffectiveValue() {
        if (sourceValue instanceof Number) {
            effectiveValue = sourceValue;
        } else if (sourceValue instanceof String) {
            effectiveValue = Double.parseDouble((String) sourceValue);
        } else {
            effectiveValue = 0;
        }
    }

    @Override
    public CellType getType() {
        return CellType.NUMERIC;
    }}
