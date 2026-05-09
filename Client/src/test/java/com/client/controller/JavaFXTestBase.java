package com.client.controller;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;

public abstract class JavaFXTestBase {
    @BeforeAll
    public static void initJavaFX() {
        new JFXPanel(); // инициализирует JavaFX toolkit
    }
}