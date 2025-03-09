package com.client.injector;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class InjectorController {

    @FXML
    private Button dllSelector;

    @FXML
    private ComboBox<String> processSelector;

    @FXML
    private Button injectButton;

    @FXML
    private Label errorMsg;

    @FXML
    private Button clearButton;

    private String selectedProcess = null;

    private String dllAbsPath = null;

    @FXML
    private Button refreshProcesses;



    public void initialize() {
        dllSelector.setOnAction(this::handleDLLSelection);
        fillProcesses(processSelector);
        ObservableList<String> originalItems = FXCollections.observableArrayList(processSelector.getItems());
        processSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null)
                processSelector.getSelectionModel().clearSelection();
            else
                selectedProcess = newValue;
        });
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        processSelector.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!processSelector.isFocused()) return;

            if (newValue == null || newValue.isEmpty()) {
                selectedProcess = null;
            }

            pause.setOnFinished(e -> {
                if (newValue == null || newValue.isEmpty()) {
                    processSelector.setItems(originalItems);
                    processSelector.hide();
                } else {
                    ObservableList<String> filteredList = FXCollections.observableArrayList();
                    for (String item : originalItems) {
                        if (item.toLowerCase().contains(newValue.toLowerCase())) {
                            filteredList.add(item);
                        }
                    }
                    processSelector.setItems(filteredList);
                }
            });
            pause.playFromStart();
        });

        injectButton.setOnAction(this::handleInjection);
        clearButton.setOnAction(this::handleClear);
        refreshProcesses.setOnAction(event -> {
            processSelector.setItems(FXCollections.observableArrayList());
            fillProcesses(processSelector);
        });
    }


    private void handleClear(ActionEvent actionEvent) {
        selectedProcess = null;
        processSelector.getSelectionModel().clearSelection();

    }

    private void handleInjection(ActionEvent actionEvent) {
        if(selectedProcess != null && dllAbsPath != null ) {
            if(new File(dllAbsPath).exists()) {
                try {
                ProcessBuilder pb = new ProcessBuilder("abs path to injector exe", selectedProcess, dllAbsPath);
                //dir of exe file
                pb.directory(new File("abs path to the directory of injector"));
                    Process p = pb.start();
                } catch (IOException e) {
                    System.out.println("Injection failed");
                    throw new RuntimeException(e);
                }
                errorMsg.setVisible(true);
                errorMsg.setStyle("-fx-text-fill: green");
                errorMsg.setText("Successfully injected");

                Timeline timeline = new Timeline(new KeyFrame(
                        Duration.seconds(3),
                        event -> errorMsg.setVisible(false)
                ));
                timeline.setCycleCount(1);
                timeline.play();
            }
        }else{
            errorMsg.setVisible(true);
            errorMsg.setStyle("-fx-text-fill: red;");
            errorMsg.setText("Injection failed");

            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(3),
                    event -> errorMsg.setVisible(false)
            ));
            timeline.setCycleCount(1);
            timeline.play();
        }
    }

    private void fillProcesses(ComboBox<String> processSelector) {
        ObservableList<String> processNameList = FXCollections.observableArrayList();;
        try {
            String line;
            String processName;
            Process p = Runtime.getRuntime().exec
                    (System.getenv("windir") +"\\system32\\"+"tasklist.exe");
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            //skip over header
            input.readLine();
            input.readLine();
            processSelector.setPromptText("Select a process...");
            input.readLine();

            while ((line = input.readLine()) != null) {
                processName = line.substring(0,25).strip();
                //If you need anything from task list do it here
                processNameList.add(processName);
            }
            input.close();
            processSelector.setItems(processNameList);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private void handleDLLSelection(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DLL");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DLL", "*.dll"));
        File file = fileChooser.showOpenDialog(dllSelector.getScene().getWindow());
        if (file != null) {
            dllAbsPath = file.getAbsolutePath();
        } else{
            dllAbsPath = null;
        }
    }

}