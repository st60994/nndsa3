package cz.upce.nndsa3.gui;

import cz.upce.nndsa3.data.Product;
import cz.upce.nndsa3.structure.IndexSequentialFileBuilder;
import cz.upce.nndsa3.util.BlockLogger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FindViewController {
    @FXML
    private TextField tfIdentifier;
    private IndexSequentialFileBuilder indexSequentialFileBuilder;
    private TextArea tfLog;

    public FindViewController(IndexSequentialFileBuilder indexSequentialFileBuilder, TextArea tfLog) {
        this.indexSequentialFileBuilder = indexSequentialFileBuilder;
        this.tfLog = tfLog;
    }
    @FXML
    void handleBtnCancelOnAction(ActionEvent event) {
        GuiUtils.createCancelDialog(tfIdentifier);
    }

    @FXML
    void handleBtnOkOnAction(ActionEvent event) {
        try {
            if (tfIdentifier.getText().equals("") || tfIdentifier.getText().trim().length() == 0) {
                GuiUtils.showErrorDialog("Please enter the identifier of the product!");
                return;
            }
            int id = Integer.parseInt(tfIdentifier.getText());
            if (id < 0) {
                GuiUtils.showErrorDialog("The identifier must be a positive number!");
                return;
            }
            Product product = indexSequentialFileBuilder.findProduct(id);
            if (product != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/upce/nndsa3/found-product-view.fxml"));
                FoundProductViewController foundProductViewController = new FoundProductViewController();
                loader.setController(foundProductViewController);
                Parent root = loader.load();
                product.setCode(product.getCode().trim());
                foundProductViewController.initializeProduct(product.toString());

                Stage newWindow = new Stage();
                newWindow.initModality(Modality.APPLICATION_MODAL);
                newWindow.setTitle("Found product");
                newWindow.setScene(new Scene(root));
                newWindow.show();
                tfLog.setText(BlockLogger.getLog().toString());
                Stage stage = (Stage) tfIdentifier.getScene().getWindow();
                stage.close();
            } else {
                GuiUtils.showErrorDialog("A product with this identifier was not found!");
            }

        } catch (Exception e) {
            GuiUtils.showErrorDialog("Please enter a number as the identifier of the product!");
        }

    }
}
