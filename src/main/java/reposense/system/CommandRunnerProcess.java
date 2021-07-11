package reposense.system;

import java.nio.file.Path;

/**
 * Represents a process created by {@code CommandRunner}.
 */
public class CommandRunnerProcess {

    private Path path;
    private String command;
    private Process process;
    private StreamGobbler outputGobbler;
    private StreamGobbler errorGobbler;

    public CommandRunnerProcess(
            Path path, String command, Process process, StreamGobbler outputGobbler, StreamGobbler errorGobbler) {
        this.path = path;
        this.command = command;
        this.process = process;
        this.outputGobbler = outputGobbler;
        this.errorGobbler = errorGobbler;
    }

    /**
     * Waits for process to finish executing and returns the output from the execution.
     */
    public String waitForProcess() throws CommandRunnerProcessException {
        int exit = 0;
        try {
            exit = process.waitFor();
            outputGobbler.join();
            errorGobbler.join();
        } catch (InterruptedException e) {
            throw new CommandRunnerProcessException("Error Handling Thread.");
        }

        if (exit == 0) {
            return outputGobbler.getValue();
        } else {
            String errorMessage = "Error returned from command ";
            errorMessage += command + " on path ";
            errorMessage += path.toString() + " :\n" + errorGobbler.getValue();
            throw new CommandRunnerProcessException(errorMessage);
        }
    }
}
