package com.github.estebanwasinger;

import static com.github.estebanwasinger.DWPlaygroundConstants.DATA_WEAVE_PLAYGROUND_APP_TITLE;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.InputStream;

import javafx.stage.Stage;

/**
 * weave-playground
 *
 * @author Esteban Wasinger (http://github.com/estebanwasinger)
 */
public class DWPlaygroundUtils {

    static void updateAppTitle(Stage primaryStage, String value) {
        primaryStage.setTitle(DATA_WEAVE_PLAYGROUND_APP_TITLE + " - " + value);
    }

    static String getTypedValueStringValue(TypedValue<?> evaluate) {
        Object value = evaluate.getValue();
        if(value instanceof CursorProvider){
            value = ((CursorProvider) value).openCursor();
        }
        String textToShow;
        if(value instanceof InputStream){
            textToShow = IOUtils.toString((InputStream) value);
        } else {
            textToShow = value.toString();
        }
        return textToShow;
    }

    static DataType getInputDataType(String mimeType) {
        return DataType.builder()
                .type(String.class)
                .mediaType(MediaType.parse(mimeType))
                .build();
    }
}
