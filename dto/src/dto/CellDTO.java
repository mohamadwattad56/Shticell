package dto;

import cell.impl.CellType;

import java.util.List;

public class CellDTO {
    private final String cellId;
    private Object sourceValue;
    private Object effectiveValue;
    private final CellType type;
    private int lastModifiedVersion;
    private final List<String> dependencies;
    private final List<String> dependents;

    // New fields for text color and background color
    private String textColor; // Change to non-final to allow modification
    private String backgroundColor; // Change to non-final to allow modification

    // Constructor with all parameters
    public CellDTO(String cellId, Object sourceValue, Object effectiveValue, CellType type, int lastModifiedVersion, List<String> dependencies, List<String> dependents, String textColor, String backgroundColor) {
        this.cellId = cellId;
        this.sourceValue = sourceValue;
        this.effectiveValue = effectiveValue;
        this.type = type;
        this.lastModifiedVersion = lastModifiedVersion;
        this.dependencies = dependencies;
        this.dependents = dependents;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    // Overloaded constructor with default text and background colors
    public CellDTO(String cellId, Object sourceValue, Object effectiveValue, CellType type, int lastModifiedVersion, List<String> dependencies, List<String> dependents) {
        this(cellId, sourceValue, effectiveValue, type, lastModifiedVersion, dependencies, dependents, "black", "white"); // Default colors
    }

    // Getters
    public String getCellId() {
        return cellId;
    }

    public Object getEffectiveValue() {
        return effectiveValue;
    }

    public CellType getType() {
        return type;
    }

    public Object getSourceValue() {
        return sourceValue;
    }

    public int getLastModifiedVersion() {
        return lastModifiedVersion;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public List<String> getDependents() {
        return dependents;
    }

    // New getters for textColor and backgroundColor
    public String getTextColor() {
        return textColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    // Setters for textColor and backgroundColor
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setSourceValue(Object sourceValue) {
        this.sourceValue = sourceValue;
    }

    public void setEffectiveValue(Object effectiveValue) {
        this.effectiveValue = effectiveValue;
    }

    public void setLastModifiedVersion(int lastModifiedVersion) {
        this.lastModifiedVersion = lastModifiedVersion;
    }
}
