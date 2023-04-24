package cz.upce.nndsa3.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FoundProductViewController {
    @FXML
    private Label labelProduct;

    public void initializeProduct(String text) {
        this.labelProduct.setText(text);
    }
}
