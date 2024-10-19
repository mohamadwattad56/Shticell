package Spreadsheet.api;

import dto.*;

import java.util.List;

public interface Engine {
    void loadSpreadsheet(String path, String uploaderName);

    void updateCellValue(String cellId, String newValue, String oldValue,String modifiedBy, Boolean flag);

    CellDTO getDisplayCellValue(String cellId);

    List<String> getVersionHistory();

    VersionDTO getVersionDTO(int versionNumber);

    CellDTO getCellDTO(String cellId);
}