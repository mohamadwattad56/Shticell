package cell.impl;

public class StringCell extends CellImpl {

    public StringCell(Object sourceValue) {
        super(sourceValue);
        updateEffectiveValue();
    }

    @Override
    protected void updateEffectiveValue() {
        if (sourceValue instanceof String) {
            effectiveValue = sourceValue.toString();
        } else {
            effectiveValue = sourceValue != null ? sourceValue.toString() : "";
        }
    }

    @Override
    public CellType getType() {
        return CellType.STRING;
    }
}
