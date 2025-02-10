import $ from "jquery";
import '../styles';

const container = $('#js-drop-zone');

export async function createNewDiagram(bpmnModeler) {
    try {
        await bpmnModeler.createDiagram();
        container.removeClass('with-error').addClass('with-diagram');
    } catch (err) {
        container.removeClass('with-diagram').addClass('with-error');
        container.find('.error pre').text(err.message);
        console.error(err);
    }
}


export async function openDiagram(xml, bpmnModeler) {
    try {
        await bpmnModeler.importXML(xml);
        container.removeClass('with-error').addClass('with-diagram');
    } catch (err) {
        container.removeClass('with-diagram').addClass('with-error');
        container.find('.error pre').text(err.message);
        console.error(err);
    }
}