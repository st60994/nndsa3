package cz.upce.nndsa3.gui;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class GuiUtils {
    public static void showErrorDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static void createCancelDialog(Node node) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("");
        alert.setContentText("Do you really want to close the window?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            Stage stage = (Stage) node.getScene().getWindow();
            stage.close();
        }
    }
}
