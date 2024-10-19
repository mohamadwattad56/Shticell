package cell.impl;

import cell.api.Cell;

abstract public class CellImpl implements Cell, Cloneable {

    protected Object sourceValue;
    protected Object effectiveValue;

    protected String textColor;
    protected String backgroundColor;

    private String lastModifiedBy;

    // Default constructor - sets text and background color to black and white
    public CellImpl(Object sourceValue) {
        this(sourceValue, "black", "white");  // Calls the other constructor with default colors
    }

    // Constructor with color arguments
    public CellImpl(Object sourceValue, String textColor, String backgroundColor) {
        this.sourceValue = sourceValue;
        this.effectiveValue = null;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    protected abstract void updateEffectiveValue();

    @Override
    public Object evaluate() {
        updateEffectiveValue();
        return effectiveValue;
    }

    @Override
    public CellImpl clone() {
        try {
            return (CellImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }

    @Override
    public Object getSourceValue() {
        return sourceValue;
    }

    @Override
    public Object getEffectiveValue() {
        return effectiveValue;
    }

    public void setEffectiveValue(Object effectiveValue) {
        this.effectiveValue = effectiveValue;
    }

    public boolean dependsOn(String cellId) {
        return true; // Default implementation for non-function cells
    }

    @Override
    public abstract CellType getType();

    public void setSourceValue(Object sourceValue) {
        this.sourceValue = sourceValue;
    }

    // Getters and setters for the colors
    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
