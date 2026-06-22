package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.CommandOutput;
import com.ddf.vodsystem.dto.ProgressTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandRunner {
    private static final Pattern timePattern = Pattern.compile("out_time_ms=(\\d+)");

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

    public static void setProgress(String line, ProgressTracker progress, float length) {
        Matcher matcher = timePattern.matcher(line);
        if (matcher.find()) {
            float timeInMs = Float.parseFloat(matcher.group(1)) / 1000000f;
            progress.setProgress(timeInMs / length);
        }
    }

    private CommandRunner() {
        // Private constructor to prevent instantiation
    }
}