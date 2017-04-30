package com.github.estebanwasinger;

import java.util.List;

/**
 * weave-playground
 *
 * @author Esteban Wasinger (http://github.com/estebanwasinger)
 */
public class PlaygroundModel {

    String transformation;
    List<InputModel> inputs;

    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    public List<InputModel> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputModel> inputs) {
        this.inputs = inputs;
    }

    @Override
    public String toString() {
        return "PlaygroundModel{" +
                "transformation='" + transformation + '\'' +
                ", inputs=" + inputs +
                '}';
    }
}
