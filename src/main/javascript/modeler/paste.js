import {isPaste} from "diagram-js/lib/features/keyboard/KeyboardUtil";
import request from 'sync-request';

export function handlePaste(event, bpmnModeler, serverBaseUrl) {
    const {keyEvent} = event;
    if (!isPaste(keyEvent)) {
        return;
    }

    const response = request('GET', `${serverBaseUrl}/clipboard/`);
    if (response.statusCode === 200) {
        const serializedCopy = response.getBody('utf8');
        if (!serializedCopy) {
            return;
        }

        const reviver = createReviver(bpmnModeler.get('moddle'));
        const parsedCopy = JSON.parse(serializedCopy, reviver);

        const clipboard = bpmnModeler.get('clipboard');
        clipboard.set(parsedCopy);
    }
}

export function createReviver(moddle) {
    return function (key, object) {
        if (typeof object === 'object' && typeof object.$type === 'string') {
            const type = object.$type;
            const attrs = Object.assign({}, object);

            delete attrs.$type;

            return moddle.create(type, attrs);
        }

        return object;
    };
}