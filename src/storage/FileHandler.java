package storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes one CSV record per line using standard Java file APIs. */
public class FileHandler {
    private final Path directory;

    /** Uses the local data folder. */
    public FileHandler() {
        this(Path.of("data"));
    }

    /** Uses the given folder, which can also be a temporary test folder. */
    public FileHandler(Path directory) {
        this.directory = directory;
    }

    /** Returns whether the CSV already exists. */
    public boolean exists(String file) {
        return Files.exists(directory.resolve(file));
    }

    /** Reads all rows. */
    public ArrayList<String[]> read(String file) throws IOException {
        ArrayList<String[]> rows = new ArrayList<>();
        for (String line : Files.readAllLines(directory.resolve(file))) {
            if (!line.isBlank()) {
                rows.add(parse(line));
            }
        }
        return rows;
    }

    /** Replaces the CSV only after the new contents have been written. */
    public void write(String file, List<String[]> rows) throws IOException {
        Files.createDirectories(directory);
        ArrayList<String> lines = new ArrayList<>();
        for (String[] row : rows) {
            StringBuilder line = new StringBuilder();
            for (String value : row) {
                if (value.contains("\n") || value.contains("\r")) {
                    throw new IllegalArgumentException("Entries must fit on one line.");
                }
                if (!line.isEmpty()) {
                    line.append(',');
                }
                line.append('"').append(value.replace("\"", "\"\"")).append('"');
            }
            lines.add(line.toString());
        }
        Path temporary = Files.createTempFile(directory, "inventory-", ".tmp");
        try {
            Files.write(temporary, lines);
            Files.move(temporary, directory.resolve(file), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /** Splits a row while keeping commas and escaped quotes inside fields. */
    private String[] parse(String line) throws IOException {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                fields.add(value.toString());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        if (quoted) {
            throw new IOException("A CSV row has an unclosed quote.");
        }
        fields.add(value.toString());
        return fields.toArray(new String[0]);
    }
}
