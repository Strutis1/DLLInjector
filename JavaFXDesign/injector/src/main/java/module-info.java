module com.client.injector {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.client.injector to javafx.fxml;
    exports com.client.injector;
}