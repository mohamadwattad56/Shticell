package Spreadsheet.impl;

import dto.VersionDTO;

import java.io.Serializable;

public class Version implements Serializable, Cloneable {
    private final int versionNumber;
    private final Spreadsheet spreadsheetSnapshot;
    private final int changedCellsCount;

    //Ctor
    public Version(int versionNumber, Spreadsheet spreadsheetSnapshot, int changedCellsCount) {
        this.versionNumber = versionNumber;
        this.spreadsheetSnapshot = spreadsheetSnapshot;
        this.changedCellsCount = changedCellsCount;
    }

    //Getter
    public int getVersionNumber() {
        return versionNumber;
    }

    public int getChangedCellsCount() {
        return changedCellsCount;
    }

    //Functions
    @Override
    public Version clone() {
        Spreadsheet clonedSpreadsheetSnapshot = this.spreadsheetSnapshot.clone();
        return new Version(this.versionNumber, clonedSpreadsheetSnapshot, this.changedCellsCount);

    }

    public VersionDTO toDTO() {
        return new VersionDTO(versionNumber, spreadsheetSnapshot.toDTO(), changedCellsCount);
    }
}
