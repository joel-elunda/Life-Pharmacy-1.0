module life.pharmacy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires javafx.graphics;
    requires java.naming;

    // export pour permettre à FXMLLoader d'accéder aux contrôleurs
    opens life.pharmacy.controllers to javafx.fxml;



    opens life.pharmacy to javafx.fxml;
    exports life.pharmacy;
}