package dashboard.dashboardTables;
import dashboard.mainDashboardController.MainDashboardController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.UnsupportedEncodingException;

public class DashboardTablesController {

    @FXML
    private TableView<SheetRowData> tableView1;  // Table for displaying sheets

    @FXML
    private TableColumn<SheetRowData, String> uploaderColumn;  // Uploader's name

    @FXML
    private TableColumn<SheetRowData, String> sheetNameColumn;  // Sheet name

    @FXML
    private TableColumn<SheetRowData, String> sheetSizeColumn;  // Sheet size

    @FXML
    private TableColumn<SheetRowData, String> permissionColumn;  // Permission type (e.g., read, write)

    @FXML
    private TableView<PermissionRowData> tableView2;  // Table for user permissions

    @FXML
    private TableColumn<PermissionRowData, String> userNameColumn;  // User name

    @FXML
    private TableColumn<PermissionRowData, String> permissionTypeColumn;  // Permission type

    @FXML
    private TableColumn<PermissionRowData, String> permissionStatusColumn;  // Permission status (approved, pending)

    private final ObservableList<SheetRowData> sheetData = FXCollections.observableArrayList();

    private final ObservableList<PermissionRowData> permissionData = FXCollections.observableArrayList();

    private MainDashboardController mainDashboardController;


    public static class SheetRowData {

        private final String uploader;
        private final String sheetName;
        private final String sheetSize;
        private String permission;
        public SheetRowData(String uploader, String sheetName, String sheetSize, String permission) {
            this.uploader = uploader;
            this.sheetName = sheetName;
            this.sheetSize = sheetSize;
            this.permission = permission;
        }

        public String getUploader() {
            return uploader;
        }

        public String getSheetName() {
            return sheetName;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }
    }
    // Data class for tableView2 (permissions)
    public static class PermissionRowData {

        private final String userName;
        private String permissionType;
        private String status;
        public PermissionRowData(String userName, String permissionType, String status) {
            this.userName = userName;
            this.permissionType = permissionType;
            this.status = status;
        }

        public String getUserName() {
            return userName;
        }

        public String getPermissionType() {
            return permissionType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }

    public SheetRowData getSelectedSheet() {
        return tableView1.getSelectionModel().getSelectedItem();
    }

    public ObservableList<SheetRowData> getSheetData() {
        return sheetData;
    }

    public TableView<SheetRowData> getTableView1() {
        return tableView1;
    }


    @FXML
    public void initialize() {
        // Set up tableView1 (sheets)
        uploaderColumn.setCellValueFactory(new PropertyValueFactory<>("uploader"));  // Uploader's name
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));  // Sheet name
        sheetSizeColumn.setCellValueFactory(new PropertyValueFactory<>("sheetSize"));  // Sheet size
        permissionColumn.setCellValueFactory(new PropertyValueFactory<>("permission"));  // Permission

        // Set up tableView2 (permissions)
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));  // User's name
        permissionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("permissionType"));  // Permission type
        permissionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));  // Status (Approved)

        // Set the initial data for the tables
        tableView1.setItems(sheetData);
        tableView2.setItems(permissionData);

        // Add a selection listener for tableView1 to handle sheet selection
        tableView1.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String selectedSheetName = newSelection.getSheetName();
                try {
                    mainDashboardController.handleSheetSelection(selectedSheetName, newSelection.getUploader());
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void addSheet(String uploader, String sheetName, String sheetSize) {
        this.mainDashboardController.getDashboardCommandsController().fetchUserPermission(sheetName, this.mainDashboardController.getDashboardHeaderController().getDashUserName(), permission -> sheetData.add(new SheetRowData(uploader, sheetName, sheetSize, permission)));

        tableView1.refresh();
    }

    public TableView<PermissionRowData> getTableView2()
    {
        return tableView2;
    }

    public void selectSheetByName(String sheetName) {
        for (SheetRowData row : tableView1.getItems()) {
            if (row.getSheetName().equals(sheetName)) {
                tableView1.getSelectionModel().select(row);  // Re-select the matching row
                break;
            }
        }
    }

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }


}
