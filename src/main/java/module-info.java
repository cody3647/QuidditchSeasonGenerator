module QuidditchSeasonGenerator {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.annotation;

    opens info.codywilliams to javafx.fxml;
    exports info.codywilliams;
    opens info.codywilliams.controllers to javafx.fxml;
}