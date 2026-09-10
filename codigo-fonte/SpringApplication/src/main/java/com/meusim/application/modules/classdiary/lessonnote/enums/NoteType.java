package com.meusim.application.modules.classdiary.lessonnote.enums;

public enum NoteType {
    INCIDENT("incidente"),
    OBSERVATION("observação"),
    FEEDBACK("feedback");

    private final String name;

    NoteType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
