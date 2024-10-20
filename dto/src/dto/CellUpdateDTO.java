package dto;

import java.util.List;

public class CellUpdateDTO {
    private final CellDTO updatedCell;
    private final List<CellDTO> dependentCells;
    private final String lastModifiedBy;  // New field to track the last user who modified the cell

    //ctor
    public CellUpdateDTO(CellDTO updatedCell, List<CellDTO> dependentCells, List<CellDTO> dependencyCells, String lastModifiedBy) {
        this.updatedCell = updatedCell;
        this.dependentCells = dependentCells;
        this.lastModifiedBy = lastModifiedBy;  // Set the user who modified the cell
    }

    //getters
    public CellDTO getUpdatedCell() {
        return updatedCell;
    }

    public List<CellDTO> getDependentCells() {
        return dependentCells;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }
}
