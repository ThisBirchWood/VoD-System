package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.CommandOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

public class CommandRunner {
    public static CommandOutput run(List<String> command, Consumer<String> onOutput) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        CommandOutput commandOutput = new CommandOutput();

        // Read the output and error streams
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            commandOutput.addLine(line);
            if (onOutput != null) {
                onOutput.accept(line);
            }
        }

        int exitCode = process.waitFor();
        commandOutput.setExitCode(exitCode);

        if (exitCode != 0) {
            throw new IOException("Command failed with exit code " + exitCode + ": " + String.join("\n", commandOutput.getOutput()));
        }

        return commandOutput;
    }

    public static CommandOutput run(List<String> command) throws IOException, InterruptedException {
        return run(command, null);
    }

    private CommandRunner() {
        // Private constructor to prevent instantiation
    }
}