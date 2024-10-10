package Spreadsheet.impl;

import dto.VersionDTO;

import java.io.Serializable;

public class Version implements Serializable {
    private final int versionNumber;
    private final Spreadsheet spreadsheetSnapshot;
    private final int changedCellsCount;

    public Version(int versionNumber, Spreadsheet spreadsheetSnapshot, int changedCellsCount) {
        this.versionNumber = versionNumber;
        this.spreadsheetSnapshot = spreadsheetSnapshot;
        this.changedCellsCount = changedCellsCount;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public int getChangedCellsCount() {
        return changedCellsCount;
    }

    public VersionDTO toDTO() {
        return new VersionDTO(versionNumber, spreadsheetSnapshot.toDTO(), changedCellsCount);
    }
}
