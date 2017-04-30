package com.github.estebanwasinger;

/**
 * weave-playground
 *
 * @author Esteban Wasinger (http://github.com/estebanwasinger)
 */
public class InputModel {

    public InputModel(String input, String mimeType) {
        this.input = input;
        this.mimeType = mimeType;
    }

    public InputModel() {
    }

    @Override
    public String toString() {
        return "InputModel{" +
                "input='" + input + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }

    String input;
    String mimeType;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
