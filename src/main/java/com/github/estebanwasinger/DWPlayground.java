package com.github.estebanwasinger;

import static com.github.estebanwasinger.DWPlaygroundConstants.APPLICATION_CSV;
import static com.github.estebanwasinger.DWPlaygroundConstants.APPLICATION_JSON;
import static com.github.estebanwasinger.DWPlaygroundConstants.APPLICATION_XML;
import static com.github.estebanwasinger.DWPlaygroundConstants.BASE_TRANS;
import static com.github.estebanwasinger.DWPlaygroundConstants.DEFAULT_PROJECT_NAME;
import static com.github.estebanwasinger.DWPlaygroundConstants.DWP_EXT;
import static com.github.estebanwasinger.DWPlaygroundConstants.DWP_EXTENSION_NAME;
import static com.github.estebanwasinger.DWPlaygroundConstants.DWP_EXT_UPPER;
import static com.github.estebanwasinger.DWPlaygroundConstants.DW_LOGO_PNG;
import static com.github.estebanwasinger.DWPlaygroundConstants.EXAMPLE;
import static com.github.estebanwasinger.DWPlaygroundConstants.FILE_MENU;
import static com.github.estebanwasinger.DWPlaygroundConstants.INPUT_STREAM_JSON;
import static com.github.estebanwasinger.DWPlaygroundConstants.LOAD_DW;
import static com.github.estebanwasinger.DWPlaygroundConstants.LOAD_PROJECT;
import static com.github.estebanwasinger.DWPlaygroundConstants.PAYLOAD;
import static com.github.estebanwasinger.DWPlaygroundConstants.PLAYGROUND_MODEL_DATA_TYPE;
import static com.github.estebanwasinger.DWPlaygroundConstants.SAVE_PROJECT;
import static com.github.estebanwasinger.DWPlaygroundConstants.SAVE_PROJECT_AS;
import static com.github.estebanwasinger.DWPlaygroundUtils.getInputDataType;
import static com.github.estebanwasinger.DWPlaygroundUtils.getTypedValueStringValue;
import static com.github.estebanwasinger.DWPlaygroundUtils.updateAppTitle;
import static javafx.scene.layout.Priority.ALWAYS;

import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.internal.el.DefaultBindingContextBuilder;
import org.mule.weave.v2.el.WeaveExpressionLanguage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Supplier;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;

/**
 * weave-playground
 *
 * @author Esteban Wasinger (http://github.com/estebanwasinger)
 */
public class DWPlayground extends Application {

    public static final Font MENLO_FONT = new Font("Menlo", 14);
    private WeaveExpressionLanguage weaveEngine;
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

		weaveEngine = new WeaveExpressionLanguage(null, WeaveExpressionLanguage.$lessinit$greater$default$2());

        SplitPane splitPane = new SplitPane();
        AnchorPane inputPane = new AnchorPane();
        inputPane.setPrefWidth(300);
        TextArea inputText = new TextArea();
        inputText.setFont(MENLO_FONT);
//        CodeArea inputText = new CodeArea();
        VBox inputVBox = new VBox();
        VBox.setVgrow(inputText, Priority.ALWAYS);
        inputVBox.getChildren().add(inputText);
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(APPLICATION_JSON, APPLICATION_XML, APPLICATION_CSV);
        comboBox.setEditable(true);
        comboBox.setValue(APPLICATION_JSON);
        comboBox.editorProperty().get().setFont(MENLO_FONT);

        inputVBox.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                comboBox.setPrefWidth(Double.valueOf(newValue.toString()) - 20);
            }
        });

        ToolBar toolBar = new ToolBar(comboBox);

        inputVBox.getChildren().add(toolBar);
        Tab payloadTab = new Tab(PAYLOAD, inputVBox);
        TabPane tabPane = new TabPane(payloadTab);
        configureTextArea(tabPane);

        inputPane.getChildren().add(tabPane);

        AnchorPane transformation = new AnchorPane();
        TextArea transformationText = new TextArea();
        transformationText.setFont(MENLO_FONT);
        setUpTransformationPane(transformation, transformationText);

        TextArea outputTextArea = new TextArea();
        outputTextArea.setFont(MENLO_FONT);
        TitledPane titledPane = new TitledPane("Transformation Output - Type: ", outputTextArea);
        AnchorPane output = new AnchorPane();

        configureTextArea(titledPane);
        configureTextArea(outputTextArea);
        output.getChildren().add(titledPane);
        output.setPrefWidth(300);
        Debouncer<Object> objectDebouncer = new Debouncer<>(callback -> {
            evaluateAndUpdateUI(inputText, transformationText, outputTextArea, titledPane, comboBox.getValue());
            return null;
        }, 300);
        EventHandler evaluateAndUpdateUIAction = event -> objectDebouncer.call("");
        transformationText.setOnKeyReleased(evaluateAndUpdateUIAction);
        inputText.setOnKeyReleased(evaluateAndUpdateUIAction);
        comboBox.setOnAction(evaluateAndUpdateUIAction);


        initUI(inputText, comboBox, transformationText, outputTextArea, titledPane);

        splitPane.getItems().addAll(inputPane, transformation, output);

        EventHandler<ActionEvent> openProjectAction = getOpenProjectAction(inputText, comboBox, transformationText, outputTextArea, titledPane);
        EventHandler<ActionEvent> saveProjectAsEventHandler = event -> saveProjectAs(inputText, comboBox, transformationText);

        VBox vBox = new VBox(splitPane);
        VBox.setVgrow(splitPane, ALWAYS);
        Scene value = new Scene(vBox, 900, 600);
        splitPane.setDividerPositions(0.33, 0.66);

        MenuBar menuBar = createMenuBar(() -> openProjectAction, () -> saveProjectAsEventHandler, () -> saveProjectAsEventHandler);
        vBox.getChildren().add(menuBar);
        startApplication(primaryStage, value);
    }

    private void startApplication(Stage primaryStage, Scene value) {
        primaryStage.setScene(value);
        updateAppTitle(primaryStage, "Unsaved project");
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream(DW_LOGO_PNG)));
        primaryStage.show();
    }

    private void setUpTransformationPane(AnchorPane transformation, TextArea transformationText) {
        transformation.setPrefWidth(300);
        configureTextArea(transformationText);
        TextFlow textFlow = new TextFlow(transformationText);
        transformationText.prefHeightProperty().bind(transformation.heightProperty());
        transformationText.prefWidthProperty().bind(transformation.widthProperty());
        transformation.getChildren().add(textFlow);
    }

    private void initUI(TextArea inputText, ComboBox<String> comboBox, TextArea transformationText, TextArea outputTextArea, TitledPane titledPane) {
        inputText.setText(EXAMPLE);
//        inputText.appendText(EXAMPLE);
        transformationText.setText(BASE_TRANS);
        evaluateAndUpdateUI(inputText, transformationText, outputTextArea, titledPane, comboBox.getValue());
    }

    private EventHandler<ActionEvent> getOpenProjectAction(TextArea inputText, ComboBox<String> mimeTypeComboBox, TextArea transformationText, TextArea outputTextArea, TitledPane titledPane) {
        return event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new ExtensionFilter(DWP_EXTENSION_NAME, DWP_EXT, DWP_EXT_UPPER));
            File file = fileChooser.showOpenDialog(primaryStage);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                BindingContext payload = new DefaultBindingContextBuilder()
                        .addBinding(PAYLOAD, new TypedValue<>(fileInputStream, INPUT_STREAM_JSON)).build();
                TypedValue<?> evaluate = weaveEngine.evaluate(LOAD_DW, PLAYGROUND_MODEL_DATA_TYPE, payload);
                PlaygroundModel playgroundModel = (PlaygroundModel) evaluate.getValue();

                inputText.setText(playgroundModel.getInputs().get(0).getInput());
//                inputText.clear();
//                inputText.appendText(playgroundModel.getInputs().get(0).getInput());
                mimeTypeComboBox.setValue(playgroundModel.getInputs().get(0).getMimeType());
                transformationText.setText(playgroundModel.getTransformation());
                evaluateAndUpdateUI(inputText, transformationText, outputTextArea, titledPane, mimeTypeComboBox.getValue());
                updateAppTitle(primaryStage, file.getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        };
    }

    /**
     * Creates the menu bar for the application
     */
    private MenuBar createMenuBar(Supplier<EventHandler<ActionEvent>> loadProjectAction, Supplier<EventHandler<ActionEvent>> saveProjectAction, Supplier<EventHandler<ActionEvent>> saveProjectAsAction) {
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);

        Menu fileMenu = new Menu(FILE_MENU);

        MenuItem loadProject = new MenuItem(LOAD_PROJECT);
        loadProject.acceleratorProperty().setValue(new KeyCodeCombination(KeyCode.O, KeyCombination.META_DOWN));
        loadProject.setOnAction(loadProjectAction.get());

        MenuItem saveProject = new MenuItem(SAVE_PROJECT);
        saveProject.acceleratorProperty().setValue(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN));
        saveProject.setOnAction(saveProjectAction.get());

        MenuItem saveProjectAs = new MenuItem(SAVE_PROJECT_AS);
        saveProjectAs.acceleratorProperty().setValue(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN, KeyCombination.SHIFT_DOWN));
        saveProjectAs.setOnAction(saveProjectAsAction.get());

        fileMenu.getItems().addAll(loadProject, saveProject, saveProjectAs);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private void saveProjectAs(TextArea inputText, ComboBox<String> comboBox, TextArea transformationText) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(DEFAULT_PROJECT_NAME);

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            PlaygroundModel playgroundModel = new PlaygroundModel();
            playgroundModel.setTransformation(transformationText.getText());
            ArrayList<InputModel> inputs = new ArrayList<>();
            inputs.add(new InputModel(inputText.getText(), comboBox.getValue()));
            playgroundModel.setInputs(inputs);

            TypedValue playGround = new TypedValue<>(playgroundModel, PLAYGROUND_MODEL_DATA_TYPE);
            BindingContext payload = new DefaultBindingContextBuilder()
                    .addBinding(PAYLOAD, playGround).build();
            TypedValue<?> evaluate = weaveEngine.evaluate(BASE_TRANS, payload);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(((CursorStreamProvider) evaluate.getValue()).openCursor(), fileOutputStream);
                updateAppTitle(primaryStage, file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void evaluateAndUpdateUI(TextArea inputText, TextArea transformationText, TextArea outputTextArea, TitledPane titledPane, String mimeType) {
        TypedValue inputTypedValue = new TypedValue<>(inputText.getText(), getInputDataType(mimeType));

        BindingContext bindingContext = new DefaultBindingContextBuilder()
                .addBinding(PAYLOAD, inputTypedValue)
                .build();
        try {
            TypedValue<?> evaluate = weaveEngine.evaluate(transformationText.getText(), bindingContext);
            outputTextArea.setText(getTypedValueStringValue(evaluate));
            titledPane.setText("Transformation Output - Type: " + evaluate.getDataType().getMediaType());
        } catch (Exception e) {
            outputTextArea.setText(e.getMessage());
        }
    }

    private void configureTextArea(Node inputText) {
        AnchorPane.setLeftAnchor(inputText, 0d);
        AnchorPane.setRightAnchor(inputText, 0d);
        AnchorPane.setBottomAnchor(inputText, 0d);
        AnchorPane.setTopAnchor(inputText, 0d);
    }
}
