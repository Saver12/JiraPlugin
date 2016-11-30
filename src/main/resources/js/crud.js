function addInput(divId) {
    var options = document.getElementById(divId);

    var inputFormDiv = document.getElementById('jiraform');
    var inputsCount = (inputFormDiv.getElementsByTagName('input').length - 2)/2;

    var keyInput = document.createElement("input");
    keyInput.type = "text";
    keyInput.name = "key[" + (inputsCount + 1) + "]";
    keyInput.id = "key[" + (inputsCount + 1) + "]";
    keyInput.className = "textfield";
    keyInput.autocomplete = "off";

    options.appendChild(keyInput);
    options.appendChild(document.createTextNode(" -> "));

    var valueInput = document.createElement("input");
    valueInput.type = "text";
    valueInput.name = "value[" + (inputsCount + 1) + "]";
    valueInput.id = "value[" + (inputsCount + 1) + "]";
    valueInput.className = "textfield";
    valueInput.autocomplete = "off";

    options.appendChild(valueInput);
    options.appendChild(document.createElement("br"));
    options.appendChild(document.createElement("br"));
}