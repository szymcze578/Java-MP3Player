module mp3player {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jid3lib;

    exports pl.java.mp3player.main to javafx.graphics;
    opens pl.java.mp3player.controller to javafx.fxml;
    opens pl.java.mp3player.mp3 to javafx.base;
}