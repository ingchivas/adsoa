module com.adsoa.adsoa {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires spark.core;
    requires java.net.http;
    requires org.json;

    opens com.adsoa.adsoa to javafx.fxml;
    exports com.adsoa.adsoa;
}