package cell.impl;

public class BooleanCell extends CellImpl {

    public BooleanCell(Object sourceVal) {
        super(sourceVal);
    }

    @Override
    protected void updateEffectiveValue() {
        if (sourceValue instanceof String || sourceValue instanceof Boolean) {
            effectiveValue = (sourceValue instanceof String) ? Boolean.parseBoolean((String) sourceValue) : sourceValue;
        }
    }

    @Override
    public CellType getType() {
        return CellType.BOOLEAN;
    }}
