package cz.upce.nndsa3.gui;

import cz.upce.nndsa3.data.Product;
import cz.upce.nndsa3.structure.IndexSequentialFileBuilder;
import cz.upce.nndsa3.util.BlockLogger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.WriterAppender;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    @FXML
    private TextArea tfLog;

    @FXML
    private ListView<Integer> listViewKeys;
    private ObservableList<Integer> observableKeyList;

    private IndexSequentialFileBuilder indexSequentialFileBuilder = new IndexSequentialFileBuilder("data.bin", "index.index");
    private static final int NUMBER_OF_RECORDS = 10000;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.observableKeyList = FXCollections.observableArrayList();
        listViewKeys.setItems(observableKeyList);
    }

    @FXML
    void handleBtnBuildOnAction(ActionEvent event) {
        indexSequentialFileBuilder.build(NUMBER_OF_RECORDS);
    }

    @FXML
    void handleBtnFindProductOnAction(ActionEvent event) throws IOException {
        BlockLogger.clearLog();
        FindViewController findViewController = new FindViewController(this.indexSequentialFileBuilder, this.tfLog);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/upce/nndsa3/find-view.fxml"));
        loader.setController(findViewController);
        Parent root = loader.load();

        Stage newWindow = new Stage();
        newWindow.initModality(Modality.APPLICATION_MODAL);
        newWindow.setTitle("Find a product");
        newWindow.setScene(new Scene(root));
        newWindow.show();
    }

    @FXML
    void handleBtnGetAllKeysOnAction(ActionEvent event) {
        BlockLogger.clearLog();
        observableKeyList.addAll(indexSequentialFileBuilder.getAllKeys());
        this.tfLog.setText(BlockLogger.getLog().toString());
    }
}
