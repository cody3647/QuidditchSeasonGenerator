/*
 * Quidditch Season Generator
 * Copyright (C) 2022.  Cody Williams
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package info.codywilliams.qsg.util;


import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class DependencyInjector {
    private static final Map<Class<?>, Callback<Class<?>, Object>> injectionMethods = new HashMap<>();
    private static final String[] defaultIconPaths = new String[]{
            "/info/codywilliams/qsg/images/icon256.png", "/info/codywilliams/qsg/images/icon128.png",
            "/info/codywilliams/qsg/images/icon64.png", "/info/codywilliams/qsg/images/icon32.png"
    };
    private static ResourceBundle bundle = null;

    private static Object constructController(Class<?> controllerClass) {
        if (injectionMethods.containsKey(controllerClass))
            return loadControllerWithMethod(controllerClass);
        else
            return loadControllerWithDefault(controllerClass);
    }

    private static Object loadControllerWithMethod(Class<?> controllerClass) {
        try {
            return injectionMethods.get(controllerClass).call(controllerClass);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object loadControllerWithDefault(Class<?> controllerClass) {
        try {
            return controllerClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Parent load(String view) throws IOException {
        FXMLLoader loader = getLoader(view);
        return loader.load();
    }

    public static FXMLLoader getLoader(String view) {
        return new FXMLLoader(
                DependencyInjector.class.getResource("/info/codywilliams/qsg/views/" + view + ".fxml"),
                bundle,
                new JavaFXBuilderFactory(),
                DependencyInjector::constructController
        );
    }

    public static void addInjectionMethod(Class<?> controllerClass, Callback<Class<?>, Object> method) {
        injectionMethods.put(controllerClass, method);
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void setBundle(ResourceBundle bundle) {
        DependencyInjector.bundle = bundle;
    }

    public static void setUpAndShowStage(Stage window, Scene scene, String titleKey, String... iconPaths) {
        window.setTitle(bundle.getString(titleKey));

        if (iconPaths == null || iconPaths.length == 0)
            iconPaths = defaultIconPaths;

        List<Image> iconList = window.getIcons();
        for (String iconPath : iconPaths) {
            InputStream is = DependencyInjector.class.getResourceAsStream(iconPath);
            if (is == null)
                continue;
            iconList.add(new Image(is));
        }

        window.setScene(scene);
        window.show();
    }

    public static void addStylesheet(Scene scene, String... stylesheet) {
        ArrayList<String> stylesheets = new ArrayList<>();
        for (String name : stylesheet) {
            String stylesheetPath = DependencyInjector.class.getResource("/info/codywilliams/qsg/stylesheets/" + name).toExternalForm();

            stylesheets.add(stylesheetPath);
        }

        scene.getStylesheets().addAll(stylesheets);
    }


}
