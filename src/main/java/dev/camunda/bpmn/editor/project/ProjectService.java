package dev.camunda.bpmn.editor.project;

import static com.intellij.notification.NotificationType.ERROR;
import static com.intellij.notification.NotificationType.INFORMATION;
import static com.intellij.openapi.vfs.VirtualFileUtil.findFileOrDirectory;
import static java.util.Arrays.copyOfRange;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileUtil;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import java.io.File;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for handling project-related operations within an IntelliJ IDEA project.
 * This class provides utility methods for working with files and editors, including
 * file searching, content reading, and editor management.
 * <p>
 * The service is designed to work with the IntelliJ Platform's virtual file system
 * and project structure, offering methods to find files by name or path, read file
 * contents, and interact with the FileEditorManager.
 *
 * @author Oleksandr Havrysh
 */
public record ProjectService(Project project) {

    private static final String NOTIFICATION_ERROR_TITLE = "Camunda BPMN Editor";
    private static final String NOTIFICATION_GROUP_ID = "dev.camunda.bpmn.editor.notification";

    /**
     * Displays an error notification in the IntelliJ IDEA environment.
     *
     * @param message The error message to be displayed in the notification.
     */
    public void showErrorNotification(String message) {
        createNotification(message, ERROR);
    }

    /**
     * Displays an information notification in the IntelliJ IDEA environment.
     * <p>
     * This method shows a notification with the INFORMATION type, which is typically
     * used for success messages or general information updates.
     *
     * @param message The information message to be displayed in the notification
     */
    public void showNotification(String message) {
        createNotification(message, INFORMATION);
    }

    /**
     * Creates and displays a notification in the IntelliJ IDEA environment.
     * <p>
     * This private helper method is used by both showErrorNotification and showNotification
     * to create notifications with different types (ERROR or INFORMATION).
     *
     * @param message The message to be displayed in the notification
     * @param type    The type of notification (ERROR or INFORMATION)
     */
    private void createNotification(String message, NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(NOTIFICATION_ERROR_TITLE, message, type)
                .notify(project);
    }

    /**
     * Retrieves the FileEditorManager instance for the current project.
     *
     * @return The FileEditorManager instance associated with the project.
     */
    public FileEditorManager getFileEditorManager() {
        return FileEditorManager.getInstance(project);
    }

    /**
     * Finds the content of a file by its path.
     * This method requires a read lock on the project model.
     *
     * @param path An array of strings representing the path to the file.
     * @return An Optional containing the file content as a String if found, or empty if not found.
     */
    @RequiresReadLock
    public Optional<String> findContentByPath(String[] path) {
        return findFilesByPath(path).stream().findFirst().map(VirtualFileUtil::readText);
    }

    /**
     * Finds the content of a file by its name.
     * This method requires a read lock on the project model.
     *
     * @param fileName The name of the file to find.
     * @return An Optional containing the file content as a String if found, or empty if not found.
     */
    @RequiresReadLock
    public Optional<String> findContentByFileName(String fileName) {
        return findFilesByFileName(fileName, true).stream().findFirst().map(VirtualFileUtil::readText);
    }

    /**
     * Finds a VirtualFile by its path.
     *
     * @param path An array of strings representing the path to the file.
     * @return An Optional containing the VirtualFile if found, or empty if not found.
     */
    private Set<VirtualFile> findFilesByPath(String[] path) {
        if (isNull(path) || path.length == 0) {
            return Set.of();
        }

        return findFilesByFileName(path[0], false).stream()
                .map(startDir -> findFileOrDirectory(startDir,
                        String.join("/", copyOfRange(path, 1, path.length))))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    /**
     * Finds all files with the given name in the project directory and its subdirectories.
     *
     * @param fileName      The name of the file to find.
     * @param findFirstOnly Whether to only return the first file matching the name.
     * @return A Set of VirtualFiles matching the given name.
     */
    private Set<VirtualFile> findFilesByFileName(String fileName, boolean findFirstOnly) {
        if (isNull(project.getBasePath()) || project.getBasePath().isEmpty()) {
            return Set.of();
        }

        var baseDirectory = LocalFileSystem.getInstance().findFileByPath(project.getBasePath());
        if (isNull(baseDirectory) || !baseDirectory.isDirectory()) {
            return Set.of();
        }

        var foundFiles = new HashSet<VirtualFile>(5);
        var directoryStack = new ArrayDeque<VirtualFile>();
        directoryStack.push(baseDirectory);

        while (!directoryStack.isEmpty()) {
            var currentDirectory = directoryStack.pop();
            for (var childFile : currentDirectory.getChildren()) {
                if (fileName.equals(childFile.getName())) {
                    foundFiles.add(childFile);
                    if (findFirstOnly) {
                        return foundFiles;
                    }
                }
                if (childFile.isDirectory()) {
                    directoryStack.push(childFile);
                }
            }
        }

        return foundFiles;
    }

    /**
     * Opens a file chooser dialog and returns the selected file.
     * <p>
     * This method creates and displays a file save dialog with the specified parameters,
     * allowing the user to choose a location and name for saving a file. The dialog
     * can be customized with a title, description, default filename, and allowed file
     * extension.
     *
     * @param title       The title of the file chooser dialog
     * @param description The description text shown in the file chooser dialog
     * @param fileName    The default file name to be shown in the dialog
     * @param extension  The allowed file extension (e.g., "svg", "png")
     * @return An Optional containing the selected File if the user completes the dialog, or empty if the dialog is canceled
     */
    public Optional<File> getFileFromDialog(String title, String description, String fileName, String extension) {
        var descriptor = new FileSaverDescriptor(title, description, extension);
        var fileWrapper = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project).save(fileName);
        return isNull(fileWrapper) ? Optional.empty() : Optional.of(fileWrapper.getFile());
    }
}