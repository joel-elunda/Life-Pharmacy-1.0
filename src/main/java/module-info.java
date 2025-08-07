module life.pharmacy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires javafx.swing;
    requires org.apache.pdfbox;


    opens life.pharmacy to javafx.fxml;
    exports life.pharmacy;
}