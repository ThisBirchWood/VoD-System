package com.ddf.vodsystem.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommandOutput {
    private List<String> output = new ArrayList<>();
    private int exitCode;

    public void addLine(String line) {
        output.add(line);
    }
}