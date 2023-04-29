module cz.upce.nndsa3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;


    opens cz.upce.nndsa3 to javafx.fxml;
    exports cz.upce.nndsa3;
    exports cz.upce.nndsa3.data;
    opens cz.upce.nndsa3.data to javafx.fxml;
    exports cz.upce.nndsa3.structure;
    opens cz.upce.nndsa3.structure to javafx.fxml;
    exports cz.upce.nndsa3.util;
    opens cz.upce.nndsa3.util to javafx.fxml;
    exports cz.upce.nndsa3.gui;
    opens cz.upce.nndsa3.gui to javafx.fxml;
}