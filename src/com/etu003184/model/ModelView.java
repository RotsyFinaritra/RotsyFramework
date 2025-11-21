package com.etu003184.model;

import java.util.Map;

public class ModelView {

    private String view;
    private Map<String, Object> data;

    public ModelView() {
        this.setData(new java.util.HashMap<>());
    }

    public ModelView(String view) {
        this.view = view;
        this.setData(new java.util.HashMap<>());

    }

    public ModelView(String view, Map<String, Object> data) {
        this.setView(view);
        this.setData(data);
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

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

}