
function setupOnScreenKeyboard()
{
    let inputTextArea = document.getElementById('inputTextArea');
    let operationButtonsContainer = document.getElementById('onScreenKeyboard');

    for (let operation of ['¬', '&', '∨', '⊃', '≡', '□', '◇', '□⤴', '◇⤴', '□⤵', '◇⤵'])
    {
        let operationButton = document.createElement('button');
        operationButton.textContent = operation;
        operationButtonsContainer.appendChild(operationButton);
    }

    for (let button of operationButtonsContainer.children)
    {
        button.addEventListener('click', () =>
        {
            let position = inputTextArea.selectionStart;
            let before = inputTextArea.value.substring(0, position);
            let after = inputTextArea.value.substring(position, inputTextArea.value.length);
            inputTextArea.value = before + button.textContent + after;
            inputTextArea.selectionStart = inputTextArea.selectionEnd = position + button.textContent.length;
            inputTextArea.focus();
        });
    }
}

setupOnScreenKeyboard();
