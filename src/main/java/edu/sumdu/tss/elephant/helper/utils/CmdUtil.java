package edu.sumdu.tss.elephant.helper.utils;

import edu.sumdu.tss.elephant.helper.exception.BackupException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdUtil {
    public static void exec(String command) {
        var result = new StringBuilder();
        try {
            System.out.println("Perform: " + command);
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command);

            BufferedReader input = new BufferedReader(new InputStreamReader(
                    pr.getErrorStream()));

            String line;

            while ((line = input.readLine()) != null) {
                result.append(line);
                System.out.println(line);
            }

            int exitVal = pr.waitFor();
            if (exitVal != 0) {
                new BackupException(result.toString());
            }
        } catch (IOException | InterruptedException ex) {
            throw (result.length() == 0) ? new BackupException(ex) : new BackupException(result.toString(), ex);
        }

    }
}