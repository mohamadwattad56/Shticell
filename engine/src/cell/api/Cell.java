package cell.api;

import cell.impl.CellType;

import java.io.Serializable;

public interface Cell extends Serializable {
    Object evaluate();
    CellType getType();
    Object getSourceValue();
    Object getEffectiveValue();
}