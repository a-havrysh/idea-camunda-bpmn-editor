package dev.camunda.bpmn.editor.project;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static dev.camunda.bpmn.editor.util.Base64Utils.decodeBytes;
import static java.nio.file.Files.write;

import java.io.IOException;
import lombok.CustomLog;

/**
 * Service class for handling image-related operations in the BPMN editor.
 * <p>
 * This service provides functionality for saving BPMN diagrams as image files,
 * particularly in SVG format. It handles the file selection dialog, file writing,
 * and notification display within the IntelliJ IDEA environment.
 * <p>
 * The class is implemented as a record to maintain immutability and provide
 * a concise way to handle the dependency on ProjectService.
 *
 * @param projectService The ProjectService instance used for file operations and notifications
 * @author Oleksandr Havrysh
 */
@CustomLog
public record ImageService(ProjectService projectService) {

    /**
     * Success message template displayed after successfully exporting an SVG file.
     * The message is formatted as HTML to support rich text formatting in the IDE's
     * notification system.
     */
    private static final String SUCCESS_EXPORTED_SVG_MESSAGE = """
            <html lang="en">
                <p>Success exported a SVG file</p>
            </html>
            """;

    /**
     * Saves a base64-encoded SVG image to a file selected by the user.
     * <p>
     * This method performs the following operations:
     * <ul>
     *     <li>Opens a file chooser dialog for the user to select the save location</li>
     *     <li>Decodes the base64-encoded SVG content</li>
     *     <li>Writes the decoded content to the selected file</li>
     *     <li>Shows a success notification upon completion</li>
     * </ul>
     * <p>
     * The method ensures thread safety by executing the file operations in the
     * appropriate thread context using IntelliJ's application threading model.
     *
     * @param encodedSvg The SVG content encoded in base64 format
     */
    public void saveSvgImage(String encodedSvg) {
        getApplication().invokeLater(() -> projectService.getFileFromDialog("Save SVG File",
                        "Choose where to save the SVG file", "diagram.svg", "svg")
                .ifPresent(selectedFile -> getApplication().runWriteAction(() -> {
                    try {
                        write(selectedFile.toPath(), decodeBytes(encodedSvg));
                        projectService.showNotification(SUCCESS_EXPORTED_SVG_MESSAGE);
                    } catch (IOException e) {
                        log.error("Error while saving SVG file", e);
                    }
                })));
    }
}