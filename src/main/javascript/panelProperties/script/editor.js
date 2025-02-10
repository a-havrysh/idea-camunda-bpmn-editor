import CodeMirror from 'codemirror';
import {isDarkMode, scriptFormat} from '../../utils/utils';
import '../../styles';
import 'codemirror/addon/display/autorefresh.js';
import {decode, encode} from 'js-base64';

export function processScriptEditor(textarea) {
    if (textarea && !textarea.codeMirror) {
        const editorContainer = document.createElement('div');
        editorContainer.className = 'code-editor-container';
        textarea.parentNode.insertBefore(editorContainer, textarea);
        editorContainer.appendChild(textarea);

        const editor = CodeMirror.fromTextArea(textarea, {
            lineNumbers: true,
            autoRefresh: true,
            matchBrackets: true,
            mode: 'groovy',
            theme: isDarkMode ? 'dracula' : 'default',
            viewportMargin: Infinity
        });

        editor.setSize(null, 'auto');
        textarea.codeMirror = editor;

        editor.on('change', () => {
            textarea.value = editor.getValue();
            textarea.dispatchEvent(new Event('input', {bubbles: true}));
        });

        editor.on('drop', () => {
            console.log("Dropped");
        });

        const buttonContainer = document.createElement('div');
        buttonContainer.className = 'edit-script-external-file-button-container';
        editorContainer.appendChild(buttonContainer);

        const editButton = document.createElement('button');
        editButton.textContent = 'Edit in new tab';
        editButton.className = 'edit-script-external-file-button';
        editButton.addEventListener('click', () => {
            if (editorContainer.getAttribute('virtual-file-id')) {
                window.setFocusVirtualFile(editorContainer.getAttribute('virtual-file-id'));
            } else {
                const format = getScriptFormatValue(editorContainer);
                window.openScriptExternalFile(encode(textarea.value) + '@' + format).then(virtualFileId => {
                    editorContainer.setAttribute('virtual-file-id', virtualFileId);
                });
            }

            editor.setOption("readOnly", true);
        });
        buttonContainer.appendChild(editButton);
    }
}

function getScriptFormatValue(editorContainer) {
    const scriptFormat = getScriptFormat(editorContainer.parentNode.parentNode.parentNode);
    return !scriptFormat || scriptFormat.value.length === 0 ? 'txt' : scriptFormat.value;
}

export function processScriptFormat(input) {
    if (input && input.value.trim() === '') {
        input.value = scriptFormat;
        input.dispatchEvent(new Event('input', {bubbles: true}));
    }
}

export function processAllScriptEditors(node) {
    node.querySelectorAll('textarea[id*="scriptValue"], textarea[id*="ScriptValue"]').forEach(processScriptEditor);
}

export function processAllScriptFormats(node) {
    if(scriptFormat && scriptFormat.length > 0) {
        node.querySelectorAll('input[id*="scriptFormat"], input[id*="ScriptLanguage"]').forEach(processScriptFormat);
    }
}

function getScriptFormat(node) {
    return node.querySelector('input[id*="scriptFormat"], input[id*="ScriptLanguage"]')
}

export function closeVirtualFile(node) {
    const virtualFileId = node.getAttribute('virtual-file-id');
    if (virtualFileId && virtualFileId.length > 0) {
        try {
            window.closeScriptExternalFile(virtualFileId);
        } catch (error) {
            console.error(error);
        }
    }
}

export function closeAllVirtualFiles(node) {
    node.querySelectorAll('div[class*="code-editor-container"]').forEach(closeVirtualFile);
}

function getCodeEditorContainer(virtualFileId) {
    return document.querySelector(`div[virtual-file-id="${virtualFileId}"].code-editor-container`);
}

window.updateScript = function (virtualFileId, scriptValue) {
    const editorContainer = getCodeEditorContainer(virtualFileId);
    if (editorContainer) {
        const codeMirror = editorContainer.querySelector('.CodeMirror').CodeMirror;
        codeMirror.setValue(decode(scriptValue));
    }
}

window.deleteVirtualFileId = function (virtualFileId) {
    const editorContainer = getCodeEditorContainer(virtualFileId);
    if (editorContainer) {
        editorContainer.removeAttribute('virtual-file-id');
        const codeMirror = editorContainer.querySelector('.CodeMirror').CodeMirror;
        codeMirror.setOption("readOnly", false);
    }
}