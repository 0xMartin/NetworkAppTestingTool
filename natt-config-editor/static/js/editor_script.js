var editor = ace.edit("editor");
editor.setTheme("ace/theme/monokai");
editor.session.setMode("ace/mode/yaml");
editor.setFontSize(16);
editor.setOptions({
    wrap: true,
    wrapBehavioursEnabled: true,
    wrapBehaviours: "end",
    enableBasicAutocompletion: true,
    enableLiveAutocompletion: true
});

function validateConfig() {
    const text = editor.getValue();
    fetch('/validate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: text })
    }).then(response => response.json())
        .then(data => {
            updateErrorPanel(data.errors);
        });
}

function updateErrorPanel(errors) {
    const errorPanel = document.getElementById('errorPanel');
    if (errors.length === 0) {
        errorPanel.innerHTML = 'No errors found ✔️';
    } else {
        errorPanel.innerHTML = errors.join('<br>');
    }
}

function undoEdit() {
    editor.undo();
}

function redoEdit() {
    editor.redo();
}

function changeFontSize(size) {
    editor.setFontSize(parseInt(size));
}

editor.commands.addCommand({
    name: "Endo",
    bindKey: { win: "Ctrl-Z", mac: "Command-Z" },
    exec: undoEdit
});

editor.commands.addCommand({
    name: "Redo",
    bindKey: { win: "Ctrl-Y", mac: "Command-Y" },
    exec: redoEdit
});

editor.getSession().on("change", function() {
    var content = editor.getValue(); 
    localStorage.setItem("editorContent", content);
});