package utils;

import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private final Map<String, String> fileStorage = new HashMap<>();  // filename -> content

    public void addFile(String fileName, String content) {
        fileStorage.put(fileName, content);
    }

    public String getFileContent(String fileName) {
        return fileStorage.get(fileName);
    }

    public Map<String, String> getAllFiles() {
        return fileStorage;
    }
}
