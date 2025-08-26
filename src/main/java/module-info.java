module life.pharmacy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens life.pharmacy to javafx.fxml;
    exports life.pharmacy;
}