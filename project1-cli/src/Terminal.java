import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

class Parser {

    String commandName;
    String[] args;

    public void parse(String input) {
        String[] parts = input.trim().split(" ", 2);
        commandName = parts.length > 0 ? parts[0].toLowerCase() : "";
        args = parts.length > 1 ? parts[1].split(" ") : new String[0];
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

public class Terminal {

    private final Parser parser = new Parser();
    private final Scanner scanner = new Scanner(System.in);
    private Path currentDirectory = Paths.get(System.getProperty("user.dir"));

    public String ReadFile(String fileName) {
        Path path = Paths.get(fileName);

        if (!path.isAbsolute()) {
            path = currentDirectory.resolve(fileName);
        }

        if (!Files.exists(path)) {
            System.out.println("File does not exist: " + path);
            return null;
        }

        try {
            return Files.readString(path);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

    private int CountLines(String str) {
    if (str == null || str.isEmpty()) return 0;
    int counter = 0;
    for (int i = 0; i < str.length(); i++) {
        if (str.charAt(i) == '\n') counter++;
    }
    // If the string does not end with a newline, there's one more line
    if (str.charAt(str.length() - 1) != '\n') {
        return counter + 1;
    } else {
        return counter;
    }
}


    private int CountWords(String str) {
        if (str.equals("")) {
            return 0;
        }
        return str.trim().split("\\s+").length;
    }

    private int CountChars(String str) {
        int counter = 0;
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == '\n') {
                continue;
            }
            counter++;
        }
        return counter;
    }

    // Helper functions to handle pwd and ls redirection and check whether the args
    // are in a correct format
    private boolean hasRedirection(String[] args) {
        return (args.length == 2 && (args[0].equals(">") || args[0].equals(">>")));
    }

    private void executeWithRedirection(String commandName, String[] args, Function<String[], String> commandFunction) {
        if (hasRedirection(args)) {
            String[] commandArgs = Arrays.copyOf(args, args.length - 2);
            String output = commandFunction.apply(commandArgs);
            writeFile(args[1], output, args[0].equals(">>"));
        } else if (args.length == 0) {
            String res = commandFunction.apply(args);
            System.out.println(res);
        } else {
            System.out.println(commandName + " takes no arguments.");
        }
    }

    // === pwd ===
    public String pwd(String[] args) {
        if (args.length > 0) {
            return "Error: pwd takes no arguments.";
        }
        return "Current directory: " + currentDirectory.toString();
    }

    public String pwd() {
        return currentDirectory.toString();
    }

    // === cd ===
    public String cd(String[] args) {
        try {
            // More than one argument
            if (args.length > 1) {
                return "Error: cd takes at most one argument.";
            }

            // No arguments: go to the home directory
            if (args.length == 0) {
                currentDirectory = Paths.get(System.getProperty("user.home"));
                return "Changed directory to home: " + currentDirectory;
            }

            // argument
            String cmd = args[0];

            // Case .. then go to parent directory
            if (cmd.equals("..")) {
                Path parent = currentDirectory.getParent();
                if (parent != null) {
                    currentDirectory = parent;
                    return "Moved up to parent: " + currentDirectory;
                } else {
                    return "You are already at the root directory: " + currentDirectory;
                }
            }

            // Case <path> then go to given directory (absolute or relative)
            Path cmdPath = Paths.get(cmd);

            if (!cmdPath.isAbsolute()) {
                cmdPath = currentDirectory.resolve(cmdPath);
            }

            cmdPath = cmdPath.normalize();

            if (Files.exists(cmdPath) && Files.isDirectory(cmdPath)) {
                currentDirectory = cmdPath;
                return "Changed directory to: " + currentDirectory;
            } else {
                return "Error: Directory does not exist.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // === ls ===
    public String ls(String[] args) {
        try {
            if (args.length > 0) {
                return "Error: ls takes no argument.";
            }
            StringBuilder result = new StringBuilder();
            Files.list(currentDirectory)
                    .map(Path -> Path.getFileName().toString())
                    .sorted()
                    .forEach(name -> result.append(name).append("\n"));
            return result.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    // mkdir & rmdir & touch
// === mkdir ===
    public void mkdir(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: mkdir <dir1> [dir2] ...");
            return;
        }

        for (String dirName : args) {
            Path dirPath = Paths.get(dirName);
            if (!dirPath.isAbsolute()) {
                dirPath = currentDirectory.resolve(dirPath);
            }

            try {
                if (Files.exists(dirPath)) {
                    if (Files.isRegularFile(dirPath)) {
                        System.out.println("mkdir: cannot create directory '" + dirName + "': A file with the same name exists.");
                    } else {
                        System.out.println("mkdir: directory already exists: " + dirName);
                    }
                    continue;
                }

                Files.createDirectories(dirPath);
                System.out.println("Directory created: " + dirPath.toAbsolutePath());

            } catch (InvalidPathException e) {
                System.out.println("mkdir: invalid path — " + dirName);
            } catch (IOException e) {
                System.out.println("mkdir: failed to create directory '" + dirName + "': " + e.getMessage());
            }
        }
    }

    // === rmdir ===
    public void rmdir(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: rmdir <directory> or rmdir *");
            return;
        }

        if (args[0].equals("*")) {
            try (DirectoryStream<Path> dirs = Files.newDirectoryStream(currentDirectory)) {
                int removed = 0;
                boolean found = false;

                for (Path dir : dirs) {
                    if (Files.isDirectory(dir)) {
                        found = true;
                        try (DirectoryStream<Path> contents = Files.newDirectoryStream(dir)) {
                            if (!contents.iterator().hasNext()) {
                                Files.delete(dir);
                                System.out.println("Deleted: " + dir.getFileName());
                                removed++;
                            }
                        }
                    }
                }

                if (!found) {
                    System.out.println("No directories found in the current directory.");
                } else if (removed == 0) {
                    System.out.println("No empty directories to remove.");
                } else {
                    System.out.println("Removed " + removed + " empty directories.");
                }

            } catch (IOException e) {
                System.out.println("rmdir: error reading directories — " + e.getMessage());
            }
            return;
        }

        Path dirPath = Paths.get(args[0]);
        if (!dirPath.isAbsolute()) {
            dirPath = currentDirectory.resolve(dirPath);
        }

        try {
            if (!Files.exists(dirPath)) {
                System.out.println("rmdir: directory not found: " + dirPath.toAbsolutePath());
                return;
            }

            if (!Files.isDirectory(dirPath)) {
                System.out.println("rmdir: cannot remove '" + dirPath + "': Not a directory.");
                return;
            }

            try (DirectoryStream<Path> contents = Files.newDirectoryStream(dirPath)) {
                if (!contents.iterator().hasNext()) {
                    Files.delete(dirPath);
                    System.out.println("Directory removed: " + dirPath.toAbsolutePath());
                } else {
                    System.out.println("rmdir: failed to remove '" + dirPath + "': Directory not empty.");
                }
            }

        } catch (InvalidPathException e) {
            System.out.println("rmdir: invalid path — " + args[0]);
        } catch (IOException e) {
            System.out.println("rmdir: error deleting directory — " + e.getMessage());
        }
    }

    // === touch ===
    public void touch(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: touch <filename>");
            return;
        }

        Path filePath = Paths.get(args[0]);
        if (!filePath.isAbsolute()) {
            filePath = currentDirectory.resolve(filePath);
        }

        try {
            if (Files.exists(filePath)) {
                if (Files.isDirectory(filePath)) {
                    System.out.println("touch: cannot create file '" + filePath + "': Is a directory.");
                } else {
                    System.out.println("touch: file already exists: " + filePath.toAbsolutePath());
                }
                return;
            }

            // Ensure parent directory exists
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                System.out.println("touch: cannot create file '" + filePath + "': Directory does not exist.");
                return;
            }

            Files.createFile(filePath);
            System.out.println("File created: " + filePath.toAbsolutePath());

        } catch (InvalidPathException e) {
            System.out.println("touch: invalid path — " + args[0]);
        } catch (IOException e) {
            System.out.println("touch: error creating file — " + e.getMessage());
        }
    }

    // === cp & cp -r ===
    public void cp(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: Invalid number of arguments for cp command.");
            return;
        }

        boolean ifDir = false;
        int argStart = 0;

        // Check if cp -r is used
        if (args[0].equals("-r")) {
            ifDir = true;
            argStart = 1;
            if (args.length != 3) {
                System.out.println("Error: Invalid number of arguments for cp -r command.");
                return;
            }
        } else if (args.length != 2) {
            System.out.println("Error: Invalid number of arguments for cp command.");
            return;
        }

        Path source = Paths.get(args[argStart]);
        Path destination = Paths.get(args[argStart + 1]);

        if (!Files.exists(source)) {
            System.out.println("Error: Source does not exist.");
            return;
        }

        try {
            if (Files.isDirectory(source)) {
                if (!ifDir) {
                    System.out.println("Error: Source is a directory. Use cp -r to copy directories.");
                    return;
                }

                try {
                    Files.walk(source).forEach(sourcePath -> {
                        Path destinationPath = destination.resolve(source.relativize(sourcePath));
                        try {
                            if (Files.isDirectory(sourcePath)) {
                                if (!Files.exists(destinationPath)) {
                                    Files.createDirectories(destinationPath);
                                }
                            } else {
                                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    });
                    System.out.println("Directory copied successfully.");
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File copied successfully.");
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // === rm ===
    public void rm(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: rm command requires exactly one argument.");
            return;
        }
        Path removedFile = Paths.get(args[0]);
        if (!removedFile.isAbsolute()) {
            removedFile = Paths.get(pwd(), args[0]);
        }

        if (Files.isDirectory(Paths.get(args[0]))) {
            System.out.println("Error: rm command does not support directories use rmdir.");
            return;}
        if (!Files.exists(removedFile)) {
            System.out.println("Error: File or directory does not exist.");
            return;
        }
        try {

            Files.delete((removedFile));
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // cat
    public void cat(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: This command requires at least one argument.");

            return;
        }

        String file1, file2;

        file1 = ReadFile(args[0]);
        if (file1 == null) {
            return;
        }
        System.out.print(file1 + "\n");

        if (args.length > 1) {
            file2 = ReadFile(args[1]);
            if (file2 == null) {
                return;
            }
            System.out.print(file2 + "\n");
        }
    }

    // wc
    @SuppressWarnings("StringEquality")
    public void wc(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: This command requires only one argument.");
            return;
        }

        String file = ReadFile(args[0]);
        if (file == null) {
            return;
        }

        int lines = CountLines(file);
        int words = CountWords(file);
        int chars = CountChars(file);

        String output = String.valueOf(lines) + " " + String.valueOf(words) + " " + String.valueOf(chars) + " "
                + args[0];

        System.out.println(output);
    }

    // >>, >
    public void writeFile(String fileName, String content, boolean isAppend) {
        Path path = Paths.get(fileName);

        if (!path.isAbsolute()) {
            path = currentDirectory.resolve(fileName);
        }

        try (FileWriter writer = new FileWriter(path.toFile(), isAppend)) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    //  zip, unzip
    public void zip(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: zip <archive.zip> <file1> [file2 ...]");
            System.out.println("Or: zip -r <archive.zip> <directory>");
            return;
        }

        boolean recursive = false;
        int startIndex = 0;

        if (args[0].equals("-r")) {
            recursive = true;
            startIndex = 1;
        }

        Path zipPath = currentDirectory.resolve(args[startIndex]);
        if (!zipPath.toString().endsWith(".zip")) {
            System.out.println("Error: Output file must end with .zip");
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                ZipOutputStream zos = new ZipOutputStream(fos)) {

            if (recursive) {
                if (args.length != startIndex + 2) {
                    System.out.println("Usage: zip -r <archive.zip> <directory>");
                    return;
                }
                Path dirToZip = currentDirectory.resolve(args[startIndex + 1]);
                if (!Files.isDirectory(dirToZip)) {
                    System.out.println("Error: Not a directory.");
                    return;
                }
                zipDirectory(dirToZip, dirToZip.getFileName().toString(), zos);
            } else {
                for (int i = startIndex + 1; i < args.length; i++) {
                    Path fileToZip = currentDirectory.resolve(args[i]);
                    if (Files.exists(fileToZip) && Files.isRegularFile(fileToZip)) {
                        zipFile(fileToZip, fileToZip.getFileName().toString(), zos);
                    } else {
                        System.out.println("Skipping invalid file: " + fileToZip);
                    }
                }
            }

            System.out.println("Archive created: " + zipPath.toAbsolutePath());

        } catch (IOException e) {
            System.out.println("Error creating zip file: " + e.getMessage());
        }
    }

    private void zipFile(Path filePath, String zipEntryName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        }
    }

    private void zipDirectory(Path folderPath, String basePath, ZipOutputStream zos) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path path : stream) {
                String entryName = basePath + "/" + path.getFileName().toString();
                if (Files.isDirectory(path)) {
                    zipDirectory(path, entryName, zos);
                } else {
                    zipFile(path, entryName, zos);
                }
            }
        }
    }

    public void unzip(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: unzip <archive.zip> [-d destination]");
            return;
        }

        Path zipPath = currentDirectory.resolve(args[0]);
        if (!Files.exists(zipPath)) {
            System.out.println("Error: File not found: " + zipPath);
            return;
        }

        Path destination = currentDirectory;
        if (args.length >= 3 && args[1].equals("-d")) {
            destination = currentDirectory.resolve(args[2]);
            try {
                Files.createDirectories(destination);
            } catch (IOException e) {
                System.out.println("Error creating destination directory: " + e.getMessage());
                return;
            }
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFilePath = destination.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newFilePath);
                } else {
                    Files.createDirectories(newFilePath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(newFilePath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                zis.closeEntry();
            }
            System.out.println("Archive extracted to: " + destination.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error extracting zip file: " + e.getMessage());
        }
    }

    public void chooseCommandAction() {
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            parser.parse(input);
            String command = parser.getCommandName();
            String[] args = parser.getArgs();

            if (command.equals("exit")) {
                break;
            }
            switch (command) {
                case "pwd" ->
                    executeWithRedirection("pwd", args, this::pwd);
                case "cd" ->
                    System.out.println(cd(args));
                case "ls" ->
                    executeWithRedirection("ls", args, this::ls);
                case "mkdir" ->
                    mkdir(args);
                case "rmdir" ->
                    rmdir(args);
                case "touch" ->
                    touch(args);
                case "cp" ->
                    cp(args);
                case "rm" ->
                    rm(args);
                case "zip" ->
                    zip(args);
                case "unzip" ->
                    unzip(args);
                case "cat" ->
                    cat(args);
                case "wc" ->
                    wc(args);
                default ->
                    System.out.println("Error: Command not found or invalid parameters.");
            }
        }
    }

    public static void main(String[] args) {
        Terminal t1 = new Terminal();
        t1.chooseCommandAction();
    }
}
