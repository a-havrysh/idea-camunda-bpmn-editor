import $ from "jquery";
import '../styles';
import {engine, isDarkMode, isDraculaMode, isSketchyMode} from '../utils/utils';
import {Linter} from "bpmnlint";

const getCommonOptions = async () => {
    const config = {
        keyboard: {bindTo: document},
        container: $('#js-canvas'),
        propertiesPanel: {parent: $('#js-properties-panel')},
        alignToOrigin: {
            alignOnSave: false,
            offset: 150,
            tolerance: 50,
        }
    };

    if (isDarkMode) {
        config.bpmnRenderer = {
            defaultFillColor: 'rgba(43,45,48,255)',
            defaultStrokeColor: '#BCBEC4'
        }
    } else if (isDraculaMode) {
        config.bpmnRenderer = {
            defaultFillColor: '#262936',
            defaultStrokeColor: '#f8f8f2'
        }
    }

    if (isSketchyMode) {
        config.textRenderer = {
            defaultStyle: {
                fontFamily: '"Virgil"',
                fontWeight: 'normal',
                fontSize: 13,
                lineHeight: 0.9
            },
            externalStyle: {
                fontSize: 13,
                lineHeight: 0.9
            }
        }
    }

    return config;
};

const getCommonModules = async (useLintModule) => {
    const modules = [
        await import('bpmn-js-token-simulation').then(m => m.default),
        await import('@bpmn-io/element-template-chooser').then(m => m.default),
    ];

    if (isSketchyMode) {
        modules.push(await import('bpmn-js-sketchy').then(m => m.default));
    }

    if (useLintModule) {
        modules.push(await import('bpmn-js-bpmnlint').then(m => m.default));
    }

    return modules;
};

export async function initModeler(useLintModule) {
    const commonOptions = await getCommonOptions();
    return await initBaseModeler(useLintModule, commonOptions);
}

async function initBaseModeler(useLintModule, commonOptions) {
    const commonModules = await getCommonModules(useLintModule);

    switch (engine) {
        case 'c7': {
            const [BpmnModeler7, CreateAppendElementTemplatesModule] = await Promise.all([
                import("camunda-bpmn-js/lib/camunda-platform/Modeler").then(m => m.default),
                import("bpmn-js-create-append-anything").then(m => m.CreateAppendElementTemplatesModule)
            ]);
            return new BpmnModeler7({
                ...commonOptions,
                additionalModules: [
                    ...commonModules,
                    CreateAppendElementTemplatesModule
                ],
            });
        }
        case 'c8': {
            const {default: BpmnModeler8} = await import("camunda-bpmn-js/lib/camunda-cloud/Modeler");
            return new BpmnModeler8({...commonOptions, additionalModules: [...commonModules]});
        }
        case 'cb': {
            const [BpmnModeler, BpmnPropertiesPanelModule, BpmnPropertiesProviderModule] = await Promise.all([
                import('bpmn-js/lib/Modeler').then(m => m.default),
                import('bpmn-js-properties-panel').then(m => m.BpmnPropertiesPanelModule),
                import('bpmn-js-properties-panel').then(m => m.BpmnPropertiesProviderModule)
            ]);
            return new BpmnModeler({
                ...commonOptions,
                additionalModules: [
                    ...commonModules,
                    BpmnPropertiesPanelModule,
                    BpmnPropertiesProviderModule
                ],
            });
        }
        default:
            throw new UnsupportedEngineError(engine);
    }
}

export class UnsupportedEngineError extends Error {
    constructor(engine) {
        super(`Unsupported engine: ${engine}`);
    }
}

export async function setLinting(modeler, serverBaseUrl, bpmnlintrc) {
    const {default: HybridResolver} = await import('../lint/resolver/hybrid-resolver');
    await import('bpmn-js-bpmnlint/dist/assets/css/bpmn-js-bpmnlint.css');
    const linterConfig = {
        config: JSON.parse(bpmnlintrc),
        resolver: new HybridResolver(serverBaseUrl)
    };

    try {
        new Linter(linterConfig).lint(modeler.getDefinitions());
        const linting = modeler.get('linting');
        linting.setLinterConfig(linterConfig);
        linting.toggle(true);
    } catch (e) {
        console.error(e);
        window.showErrorNotification(`<html lang="en">
                <p>Please check your <code>.bpmnlintrc</code> or lint plugins configuration.</p>
                <p>For more details, visit the 
                <a href="https://github.com/a-havrysh/idea-camunda-bpmn-editor" title="Open the GitHub repository for the plugin">GitHub Repository</a>.
                </p>
            </html>`);
    }
}