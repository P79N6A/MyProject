 // init editor
var codeContainer = document.querySelector('.editor-code');
var treeContainer = document.querySelector('.editor-tree');
var codeOptions = {
    mode: 'code',
    modes: ['code', 'text']
};
var treeOptions = {
    mode: 'tree'
};
var codeEditor = new JSONEditor(codeContainer, codeOptions);
var treeEditor = new JSONEditor(treeContainer, treeOptions);
var jsonEditorStore = window.top.jsonEditorStore;
var btnToLeft = document.querySelector('.editor-to-left');
var btnToRight = document.querySelector('.editor-to-right');

// set json
window.setJson = function(value) {
    var mode = codeEditor.getMode();
    clear(mode);
    try {
        value = JSON.parse(value);
        mode !== 'code' && codeEditor.setMode('code');            
        codeEditor.set(value);
        treeEditor.set(codeEditor.get())
        treeEditor.expandAll();
    } catch (e) {
        mode !== 'text' && codeEditor.setMode('text');
        codeEditor.setText(value);
        treeEditor.set({});
    };
}

// clear current content
function clear(mode) {
    if (mode === 'text') {
        codeEditor.setText('');
    } else if (mode === 'code') {
        codeEditor.set('');
    }
}

// save content
var saveBtn = document.querySelector('.editor-save');
saveBtn.addEventListener('click', function() {
    var mode = codeEditor.getMode();
    var value = '';
    if (mode === 'text') {
        value = codeEditor.getText();
    } else if (mode === 'code') {
        value = codeEditor.get();
        try {
            value = JSON.stringify(value);
        } catch (e) {};
    }
    jsonEditorStore.saveJson(value);
    jsonEditorStore.hideModal();
});

// cancle edit
var cancleBtn = document.querySelector('.editor-cancle');
cancleBtn.addEventListener('click', function() {
    jsonEditorStore.hideModal();
});

// to right
btnToLeft.addEventListener('click', function() {
    codeEditor.set(treeEditor.get());
});
btnToRight.addEventListener('click', function() {
    treeEditor.set(codeEditor.get());
    treeEditor.expandAll();
});
