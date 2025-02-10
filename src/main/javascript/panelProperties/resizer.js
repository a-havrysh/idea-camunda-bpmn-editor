export function enablePanelPropertiesResizing() {
    const propertiesPanel = document.getElementById('js-properties-panel');
    const canvas = document.getElementById('js-canvas');
    const resizer = document.createElement('div');
    resizer.className = 'resizer';
    propertiesPanel.appendChild(resizer);

    let isResizing = false;
    let lastDownX = 0;

    resizer.addEventListener('mousedown', (e) => {
        isResizing = true;
        lastDownX = e.clientX;
        document.body.style.cursor = 'ew-resize';
    });

    document.addEventListener('mousemove', (e) => {
        if (!isResizing) return;

        const delta = lastDownX - e.clientX;
        const newWidth = propertiesPanel.offsetWidth + delta;

        if (newWidth > 200 && newWidth < window.innerWidth - 200) {
            propertiesPanel.style.width = `${newWidth}px`;
            canvas.style.width = `calc(100% - ${newWidth}px)`;
            lastDownX = e.clientX;
        }
    });

    document.addEventListener('mouseup', () => {
        isResizing = false;
        document.body.style.cursor = 'default';
    });
}