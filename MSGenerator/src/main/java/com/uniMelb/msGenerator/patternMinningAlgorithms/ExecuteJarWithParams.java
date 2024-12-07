package com.uniMelb.msGenerator.patternMinningAlgorithms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ExecuteJarWithParams {


    // public static void main(String[] args) {
    public void executePatternMinning(String libraryLocation, String[] libraryParameters) {
        // Specify the path to the JAR file
        String jarPath = libraryLocation;

        // Specify input parameters
        // String[] parameters = {"run", "GSP", "C:/Development/MSGeneratorSupportWork/contextPrefixSpan.txt", "C:/Development/MSGeneratorSupportWork/output.txt", "5%"};
        String[] parameters = libraryParameters;

        // Create the command
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("java", "-jar", jarPath);

        // Add parameters to the command
        for (String param : parameters) {
            processBuilder.command().add(param);
        }

        processBuilder.redirectErrorStream(true); // Redirect error stream to output stream

        try {
            // Start the process
            Process process = processBuilder.start();

            // Read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Pattern Minning Process exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
