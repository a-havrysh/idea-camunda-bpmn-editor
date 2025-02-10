package dev.camunda.bpmn.editor.vfs;

import static com.intellij.openapi.vfs.VirtualFileUtil.readText;
import static com.intellij.openapi.vfs.VirtualFileUtil.writeBytes;
import static dev.camunda.bpmn.editor.util.Base64Utils.decodeBytes;
import static dev.camunda.bpmn.editor.util.Base64Utils.encode;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;

/**
 * Service class for handling operations on VirtualFile objects within the BPMN Editor.
 * This class provides methods for reading, encoding, decoding, and saving content of a VirtualFile.
 * It acts as a wrapper around VirtualFile, offering convenient methods for file operations
 * specific to the BPMN Editor's needs, including Base64 encoding and decoding of file content.
 */
@Getter
public class BpmnFile {

    private final String content;
    private final VirtualFile file;

    /**
     * Constructs a new VirtualFileService for the given VirtualFile.
     * Reads the content of the file upon initialization and stores it internally.
     *
     * @param file The VirtualFile to be managed by this service.
     */
    public BpmnFile(VirtualFile file) {
        this.file = file;
        this.content = readText(file);
    }

    /**
     * Returns the content of the file encoded in Base64 format.
     * This method is useful when the file content needs to be transmitted
     * or stored in a format that requires text representation of binary data.
     *
     * @return A Base64 encoded string representation of the file's content.
     */
    public String getEncodedContent() {
        return encode(content);
    }

    /**
     * Saves the provided Base64 encoded content to the file.
     * This method decodes the Base64 string and writes the resulting bytes to the file.
     *
     * @param encodedContent The Base64 encoded content to be saved to the file.
     */
    public void saveEncodedContent(String encodedContent) {
        WriteAction.run(() -> writeBytes(file, decodeBytes(encodedContent)));
    }

    /**
     * Returns the path of the file.
     * This method provides a string representation of the file's location in the file system.
     *
     * @return A string representing the path of the file.
     */
    public String getPath() {
        return file.getPath();
    }

    /**
     * Checks if the underlying VirtualFile is valid.
     * A file is considered valid if it still exists and can be accessed.
     *
     * @return true if the file is valid and can be accessed, false otherwise.
     */
    public boolean isValid() {
        return file.isValid();
    }
}