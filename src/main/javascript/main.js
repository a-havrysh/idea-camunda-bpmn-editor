import {enablePanelPropertiesResizing} from './panelProperties/resizer';
import {setupScriptObserver} from './panelProperties/script/observers';
import './styles';
import {isDarkMode, isDraculaMode} from './utils/utils';
import {handlePaste} from "./modeler/paste";
import {decode, encode} from 'js-base64';

if (isDarkMode) {
    await import("../resources/ui/themes/dark/style.css");
} else if (isDraculaMode) {
    await import("../resources/ui/themes/dracula/style.css");
} else {
    document.body.classList.add('light');
}

window.initApp = async function () {
    const {initModeler, setLinting} = await import('./modeler/modeler');
    const useLintModule = window.serverBaseUrl && window.bpmnlintrc;
    const bpmnModeler = await initModeler(useLintModule);
    if (useLintModule) {
        await setLinting(bpmnModeler, window.serverBaseUrl, window.bpmnlintrc);
    }

    if (window.bpmnXml && window.bpmnXml.length > 0) {
        const {openDiagram} = await import('./modeler/diagram-utils');
        await openDiagram(decode(window.bpmnXml), bpmnModeler);
    } else {
        const {createNewDiagram} = await import('./modeler/diagram-utils');
        await createNewDiagram(bpmnModeler);
    }

    enablePanelPropertiesResizing();
    setupScriptObserver();

    bpmnModeler.on('commandStack.changed', async () => {
        try {
            const {xml} = await bpmnModeler.saveXML({format: true});
            window.updateBpmnXml(encode(xml));
        } catch (err) {
            console.error('Error while saving XML:', err);
        }
    });

    bpmnModeler.on('copyPaste.elementsCopied', event => {
        const {tree} = event;
        try {
            window.copyBpmnClipboard(JSON.stringify(tree));
        } catch (err) {
            console.error('Error while copyBpmnClipboard:', err);
        }
    });

    bpmnModeler.get('keyboard').addListener(3000, event => {
        handlePaste(event, bpmnModeler, window.serverBaseUrl);
    });

    window.bpmnModeler = bpmnModeler;
}

window.undoOperation = function () {
    if (window.bpmnModeler) {
        window.bpmnModeler.get('commandStack').undo();
    }
}

window.redoOperation = function () {
    if (window.bpmnModeler) {
        window.bpmnModeler.get('commandStack').redo();
    }
}

window.copySelectedContent = function () {
    if (window.bpmnModeler) {
        window.bpmnModeler.get('editorActions').trigger('copy');
        window.showNotification('Copied selected content to clipboard');
    }
}

window.pasteSelectedContent = function () {
    if (window.bpmnModeler) {
        window.bpmnModeler.get('editorActions').trigger('paste');
    }
}

window.exportAsSvg = async function () {
    if (window.bpmnModeler) {
        try {
            const {svg} = await window.bpmnModeler.saveSVG();
            window.saveSvg(encode(svg));
        } catch (err) {
            window.showErrorNotification(`<html lang="en">
                <p>Failed to export BPMN into an SVG file</p>
            </html>`);
        }
    }
}