package com.github.estebanwasinger;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;

import java.io.InputStream;

/**
 * weave-playground
 *
 * @author Esteban Wasinger (http://github.com/estebanwasinger)
 */
public class DWPlaygroundConstants {


    public static final String LOAD_PROJECT = "Load project";
    public static final String SAVE_PROJECT = "Save project";
    public static final String SAVE_PROJECT_AS = "Save Project As...";
    public static final String DATA_WEAVE_PLAYGROUND_APP_TITLE = "DataWeave Playground";
    public static final String FILE_MENU = "File";
    public static final String DEFAULT_PROJECT_NAME = "dw-playground-project.dwp";
    public static final String DW_LOGO_PNG = "dw-logo.png";
    public static final String DWP_EXTENSION_NAME = "DataWeave Playground Project";
    public static final String DWP_EXT = "*.dwp";
    public static final String DWP_EXT_UPPER = "*.DWP";

    public static final String BASE_TRANS = "%dw 2.2\n" +
            "output application/json\n" +
            "---\n" +
            "payload";
    public static final String EXAMPLE = "{\n" +
            "    \"hello\" : \"world\"\n" +
            "}";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_CSV = "application/csv";
    public static final String SAVE_DW = "%dw 2.2\n" +
            "%output application/json\n" +
            "---\n" +
            "{\n" +
            " inputs : write(payload, \"application/java\"),\n" +
            " transformation: write(transformation, \"application/java\")\n" +
            "}";

    public static final String LOAD_DW = "%dw 2.2\n" +
            "output application/java\n" +
            "---\n" +
            "payload";
    public static final String PAYLOAD = "payload";
    public static final String TRANSFORMATION = "transformation";
    public static final DataType INPUT_STREAM_JSON = DataType.builder().type(InputStream.class).mediaType(MediaType.APPLICATION_JSON).build();
    public static final DataType PLAYGROUND_MODEL_DATA_TYPE = DataType.fromType(PlaygroundModel.class);
}
