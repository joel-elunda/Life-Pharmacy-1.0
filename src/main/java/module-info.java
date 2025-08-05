module life.pharmacy {
    requires javafx.controls;
    requires javafx.fxml;


    opens life.pharmacy to javafx.fxml;
    exports life.pharmacy;
}