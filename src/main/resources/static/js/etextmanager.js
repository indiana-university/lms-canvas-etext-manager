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
// Placeholder js file

(function() {
    var token = $('#_csrf').attr('content');
    var header = $('#_csrf_header').attr('content');
    $(document).ajaxSend(function(e,xhr,options) {
       xhr.setRequestHeader(header, token);
    });

    $(".loading-inline-btn").click(function(event) {
        $(".loading-inline").show().addClass("rvt-flex");

        // Set screenreader-only text to notify there is some loading action happening
        var srText = $(this).find(".rvt-loader").data("loader-text");
        $("#spinner-sr-text").text(srText).focus();
    });

    document.addEventListener('rvtTabActivated', event => {
        var tabId = event.detail.tab.id;
        var urlBase = $(event.detail.tab).data('urlbase');
        var innerDivId = $(event.detail.tab).data('inner-div-id');
        if (tabId === 'config-panel') {
            $('#config-loader').toggleClass('rvt-display-none');
            $(innerDivId).empty();
            $(innerDivId).load(urlBase, function( response, status, xhr ) {
                $('#config-loader').toggleClass('rvt-display-none');
                $('#toolInfoTable').DataTable({
                    columnDefs: [{ targets: [3,4], orderable: false }],
                    dom: '<"rvt-button-group rvt-m-bottom-md"B><lfrtip>',
                    buttons: [
                        {
                            text: 'New Config', className: 'rvt-button',
                            attr: {
                                'data-rvt-dialog-trigger': 'edit-tool-properties-new'
                            }
                        }
                    ]
                });
                var tooltable = $('#toolInfoTable').DataTable();
                tooltable.columns.adjust().draw();
            });
        } else if (tabId === 'report-panel') {
           $('#reports-loader').toggleClass('rvt-display-none');
           $(innerDivId).empty();
           $(innerDivId).load(urlBase, function( response, status, xhr ) {
               $('#reports-loader').toggleClass('rvt-display-none');
               $('#appTable').DataTable({
                   dom: '<"rvt-button-group rvt-m-bottom-md"<"button-heading rvt-ts-23 rvt-text-bold">B><lfrtip>',
                   buttons: [{ extend: 'csv', text: 'Download Report', className: 'rvt-button' }],
                   orderCellsTop: true,
                   order: [[0, 'desc'],[4, 'asc']],
                   columnDefs: [
                         { targets: [3, 7, 8], className: 'limited-column-width' },
                         { targets: [9], orderable: false }
                       ],
                   initComplete: function () {
                       this.api()
                       .columns('.selectFilter')
                       .every(function () {
                           var column = this;
                           var select = $('<select class="rvt-select"><option value="">All</option></select>')
                               .appendTo($("#appTable thead tr:eq(1) th").eq(column.index()).empty())
                               .on('change', function () {
                                   column.search($(this).val(), { exact: true }).draw();
                               })
                               .on( 'click' , function (evt) {
                                   evt.stopPropagation();
                               });
                           var colData = column.data().unique().sort();
                           var hasHeaderClass = $(column.header()).hasClass('selectFilterReverse')
                           if (hasHeaderClass) {
                               colData.reverse();
                           }
                           colData.each(function (d, j) {
                               select.append('<option value="' + d + '">' + d + '</option>');
                           });
                       });
                       this.api()
                       .columns('.inputFilter')
                       .every(function () {
                           let column = this;
                           var textinput = $('<input type="text" class="rvt-text-input" placeholder="Date">')
                               .appendTo($("#appTable thead tr:eq(1) th").eq(column.index()).empty())
                               .on('keyup', function () {
                                   column.search(this.value).draw();
                               });
                       })
                       $('#appTable').wrap("<div style='overflow:auto;width:100%;position:relative;'></div>");
                   }
               });
               var table = $('#appTable').DataTable();
               table.columns.adjust().draw();
           });
       }
    }, false);


}());
