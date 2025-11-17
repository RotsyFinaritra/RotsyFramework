package com.etu003184.model;

import java.util.Map;

public class ModelView {

    private String view;
    private Map<String, Object> data;

    public ModelView() {
        this.data = new java.util.HashMap<>();
    }

    public ModelView(String view, Map<String, Object> data) {
        this.view = view;
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public ModelView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

}