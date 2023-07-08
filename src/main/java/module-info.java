module QuidditchSeasonGenerator {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires org.jetbrains.annotations;
    requires java.naming;
    requires java.net.http;

    opens info.codywilliams.qsg to javafx.fxml;
    exports info.codywilliams.qsg;
    opens info.codywilliams.qsg.controllers to javafx.fxml;
    opens info.codywilliams.qsg.models to com.fasterxml.jackson.databind;
    opens info.codywilliams.qsg.models.player to javafx.base, com.fasterxml.jackson.databind;
    opens info.codywilliams.qsg.models.tournament to javafx.base, com.fasterxml.jackson.databind;
    opens info.codywilliams.qsg.util to com.fasterxml.jackson.databind;
    opens info.codywilliams.qsg.models.tournament.type to javafx.base, com.fasterxml.jackson.databind;
    opens info.codywilliams.qsg.models.match to com.fasterxml.jackson.databind;
    opens info.codywilliams.qsg.service to com.fasterxml.jackson.databind;
    opens info.codywilliams.qsg.models.mediawiki to com.fasterxml.jackson.databind;
}