/*-
 * #%L
 * etext-manager
 * %%
 * Copyright (C) 2024 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
        let form;
        if (button.form) {
            form = button.form;
        } else {
            // the form id will be found in a data attribute
            const formId = button.dataset.formId;
            form = document.getElementById(formId);
        }

        var jqForm = $(form);

        var jqxhr = $.post(jqForm.attr('action'), jqForm.serialize());
        jqxhr.done(function(data) {
            // Close dialog and reload base page
            window.location.replace(data.location);
        });
        jqxhr.fail(function(result) {
            // Show error message
            $(".error-reason", "#" + jqForm.attr("id")).text(result.responseJSON.message);
            $(".error-div", "#" + jqForm.attr("id")).removeClass("rvt-display-none");

            // Re-activate buttons and stuff
            for(var i = 0; i < buttonsToDisable.length; i++) {
                buttonsToDisable[i].disabled = false;
            }
            button.classList.remove("rvt-button--loading");
            button.getElementsByTagName('div')[0].classList.add("rvt-display-none");
        });
    }
}
