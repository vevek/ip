package handlers;

import exceptions.DukeException;
import tasks.Deadline;
import tasks.Event;
import tasks.Task;
import tasks.Todo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Scanner;

public class StorageHandler {
    private final String path;

    public StorageHandler(String path) {
        this.path = path;
    }

    public ArrayList<Task> openFile() throws DukeException {
        File file = new File(path);
        ArrayList<Task> taskList = new ArrayList<>();
        int lineIndex = 1;

        if(!file.exists()) {
            createFile(file);
        }

        try {
            Scanner scanner = new Scanner(file);

            while(scanner.hasNext()) {
                String line = scanner.nextLine();
                Task task = processFile(line, lineIndex);
                taskList.add(task);
                lineIndex++;
            }
        } catch (FileNotFoundException e) {
            throw new DukeException("File not found!");
        }

        return taskList;
    }

    private void createFile(File file) throws DukeException {
        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            throw new DukeException("Could not create a new file.");
        }
    }

    private Task processFile(String line, int lineIndex) throws DukeException {
        String[] lineArr = line.split(" \\| ");

        String typeOfTask;
        String taskStatus;
        String taskDescription;
        Boolean isTaskDone;
        Task task;

        try {
            typeOfTask = lineArr[0];
            taskStatus = lineArr[1];
            taskDescription = lineArr[2];
        } catch (IndexOutOfBoundsException e) {
            throw new DukeException("Failed to read from saved file!");
        }

        if(taskStatus.equals("0")) {
            isTaskDone = false;
        } else if (taskStatus.equals("1")) {
            isTaskDone = true;
        } else {
            throw new DukeException("Incorrect file format!");
        }

        switch (typeOfTask) {
        case "T":
            task = new Todo(taskDescription, isTaskDone);
            return task;
        case "D":
            try {
                String by = lineArr[3];
                task = new Deadline(taskDescription, isTaskDone, by);
            } catch (IndexOutOfBoundsException e) {
                throw new DukeException("Failed to read from saved file!");
            }
            return task;
        case "E":
            try {
                String at = lineArr[3];
                task = new Event(taskDescription, isTaskDone, at);
            } catch (IndexOutOfBoundsException e) {
                throw new DukeException("Failed to read from saved file!");
            }
            return task;
        default:
            throw new DukeException("Incorrect file format!");
        }
    }

    public void writeToFile(ArrayList<Task> taskList) throws IOException, DukeException {
        FileWriter fileWriter = new FileWriter(path);
        StringBuilder stringToWrite = new StringBuilder();

        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            stringToWrite.append(formatTask(task));
            stringToWrite.append("\n");
        }
        fileWriter.write(stringToWrite.toString());
        fileWriter.close();
    }

    private String formatTask(Task task) throws DukeException {
        String line;
        String description = task.getDescription();
        int isDone;

        if(task.getIsDone()) {
            isDone = 1;
        } else if (task.getIsDone() == false) {
            isDone = 0;
        } else {
            throw new DukeException("Failed to get if Task isDone!");
        }

        if (task.getTaskType().equals("Todo")) {
            line = String.format("T | %d | %s", isDone, description);
        } else if (task.getTaskType().equals("Deadline")) {
            String by = ((Deadline) task).getBy();
            line = String.format("D | %d | %s | %s", isDone, description, by);
        } else if (task.getTaskType().equals("Event")) {
            String at = ((Event) task).getAt();
            line = String.format("E | %d | %s | %s", isDone, description, at);
        } else {
            throw new DukeException("Task type unknown, could not write to file!");
        }

        return line;
    }
}