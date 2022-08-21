module QuidditchSeasonGenerator {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires ch.qos.logback.classic;

    opens info.codywilliams.qsg to javafx.fxml;
    exports info.codywilliams.qsg;
    opens info.codywilliams.qsg.controllers to javafx.fxml;
    opens info.codywilliams.qsg.models.player to javafx.base, com.fasterxml.jackson.databind;
    opens info.codywilliams.qsg.models to com.fasterxml.jackson.databind;
    opens info.codywilliams.qsg.util to com.fasterxml.jackson.databind;
}