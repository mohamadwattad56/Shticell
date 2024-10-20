package dto;

import java.util.List;

public class VersionDTO {
    private final int versionNumber;
    private final SpreadsheetDTO spreadsheetDTO;
    private final int changedCellsCount;

    //ctor
    public VersionDTO(int versionNumber, SpreadsheetDTO spreadsheetDTO, int changedCellsCount) {
        this.versionNumber = versionNumber;
        this.spreadsheetDTO = spreadsheetDTO;
        this.changedCellsCount = changedCellsCount;
    }

    //getters
    public int getVersionNumber() {
        return versionNumber;
    }

    public SpreadsheetDTO getSpreadsheetDTO() {
        return spreadsheetDTO;
    }

    public int getChangedCellsCount() {
        return changedCellsCount;
    }

    public List<CellDTO> getCells()
    {
        return spreadsheetDTO.getCells();
    }

}
