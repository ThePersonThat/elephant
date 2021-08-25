var editor;

function runQuery(query, clearResults) {
    if (!query)
        return;
    $.ajax({
      url: URL,
      data: { query: query },
      async: false,
      cache: false,
      method:"post"
    })
      .done(function( html ) {
         var content = $( "#DB150_content" );
         if (clearResults) content.empty();
         $("<div class='result'>"+html+"</div>").hide().appendTo(content).fadeIn(1000);
      });
}

function runCurrentQuery() {
    var editorText = editor.getValue();

    var position = editor.getCursorPosition();
    var start, end, currRow = 0, currPos = 0;

    while (currRow < position.row) {
        if (editorText[currPos] == '\n') {
            currRow++;
        }
        currPos++;
    }
    currPos += position.column;

    if (editorText[currPos] == ';')
        currPos--;

    for (start = currPos; start > 0 && editorText[start] != ';'; start-- );
    for (end = currPos; end < editorText.length && editorText[end] != ';'; end++ );

    if (editorText[start] == ';') start++;

    //substring excluding end index
    let query = editorText.substring(start, end).trim();
    if (!query)
        return; // no current query

    runQuery(query, true);
}

function runAll() {
    showProgress();
    var queries = editor.getValue().split(';');
    $( "#DB150_content").empty();
    $.each(queries, function(index, value) {
        console.log(value);
        runQuery(value, false);
    });
    hideProgress();
}

window.onload = function() {
    editor = ace.edit("editor");
    document.getElementById('editor').style.fontSize='12px';
    document.getElementById('editor').style.font= 'monospace';
    editor.setTheme("ace/theme/crimson_editor");
    editor.getSession().setMode("ace/mode/sql");
    editor.setValue(
        'select * from table1'
    );
    editor.setShowPrintMargin(false); // такая полоска-граница (40, 80 или свободно число символов, считая слева)
    editor.setReadOnly(false); // нельзя редактировать, false - можно

    editor.commands.addCommand({
        name: 'executeAll',
        bindKey: {win: 'Ctrl-Enter',  mac: 'Command-Enter'},
        exec: runAll,
        readOnly: false // false if this command should not apply in readOnly mode
    });
    editor.commands.addCommand( {
        name: 'executeCurrent',
        bindKey: {win: 'F8',  mac: 'F8'},
        exec: runCurrentQuery,
        readOnly: false // false if this command should not apply in readOnly mode
    });
    editor.commands.addCommands([{
        name: "unfind",
        bindKey: {
            win: "Ctrl-F",
            mac: "Command-F"
        },
        exec: function(editor, line) {
            return false;
        },
        readOnly: true
    }]);

    //todo add rules for oracle words highlighting
    editor.focus();
};

var spinnerVisible = false;
function showProgress() {
    if (!spinnerVisible) {
        $("div#DB150_spinner").fadeIn("fast");
        spinnerVisible = true;
    }
};
function hideProgress() {
    if (spinnerVisible) {
        var spinner = $("div#DB150_spinner");
        spinner.stop();
        spinner.fadeOut("fast");
        spinnerVisible = false;
    }
};