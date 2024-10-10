package dto;

import java.util.List;

public class CellUpdateDTO {
    private CellDTO updatedCell;
    private List<CellDTO> dependentCells;
    private List<CellDTO> dependencyCells;
    private String lastModifiedBy;  // New field to track the last user who modified the cell

    public CellUpdateDTO(CellDTO updatedCell, List<CellDTO> dependentCells, List<CellDTO> dependencyCells, String lastModifiedBy) {
        this.updatedCell = updatedCell;
        this.dependentCells = dependentCells;
        this.dependencyCells = dependencyCells;
        this.lastModifiedBy = lastModifiedBy;  // Set the user who modified the cell
    }

    public CellDTO getUpdatedCell() {
        return updatedCell;
    }

    public List<CellDTO> getDependentCells() {
        return dependentCells;
    }

    public List<CellDTO> getDependencyCells() {
        return dependencyCells;
    }
}
