
function buttonLoading(button, formSubmit = true) {
    button.setAttribute("aria-busy", true);
    var buttonsToDisable = document.getElementsByTagName('button');
    for(var i = 0; i < buttonsToDisable.length; i++) {
        buttonsToDisable[i].disabled = true;
    }
    button.classList.add("rvt-button--loading");
    button.getElementsByTagName('div')[0].classList.remove("rvt-display-none");

    if (formSubmit) {
        // FF doesn't need this, but Chrome and Edge do
        // Also, Rivet 2 moves the dialog out of the form ¯\_(ツ)_/¯ so we have to manually get the form by id
        if (button.form) {
            button.form.submit();
        } else {
            // the form id will be found in a data attribute
            const formId = button.dataset.formId;
            document.getElementById(formId).submit();
        }
    }
}
