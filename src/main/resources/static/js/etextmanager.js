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

(function() {
    var token = $('#_csrf').attr('content');
    var header = $('#_csrf_header').attr('content');
    $(document).ajaxSend(function(e,xhr,options) {
       xhr.setRequestHeader(header, token);
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

                // DataTables sorting defaults to third click removing sorting. This sets it to asc/desc only
                DataTable.defaults.column.orderSequence = ['asc', 'desc'];

                let table = $('#toolInfoTable').DataTable({
                    columnDefs: [{ targets: [3,4], orderable: false }],
                    layout: {
                       top1Start: {
                           buttons: {
                               name: 'newConfig',
                               buttons: [
                               {
                                   text: 'New Config', className: 'rvt-button',
                                   attr: {
                                       'data-rvt-dialog-trigger': 'edit-tool-properties-new'
                                   }
                               }
                               ]
                           },
                       },
                   },
                });
                // Add extra styling for the button group as there wasn't an obvious way to do it on the button group itself
                table.buttons('newConfig', null).containers().addClass('rvt-button-group rvt-items-center');
            });
        } else if (tabId === 'report-panel') {
           $('#reports-loader').toggleClass('rvt-display-none');
           $(innerDivId).empty();
           $(innerDivId).load(urlBase, function( response, status, xhr ) {
               $('#reports-loader').toggleClass('rvt-display-none');

               // Customize a few of the search input related wrapper classes
               DataTable.ext.classes.search.input = 'rvt-m-left-xs';
               DataTable.ext.classes.search.container = 'rvt-p-top-md search-wrapper';

               // DataTables sorting defaults to third click removing sorting. This sets it to asc/desc only
               DataTable.defaults.column.orderSequence = ['asc', 'desc'];

               // Track the column index before things get rendered/hidden so we can use it when customizing the data export
               let targetColForExportManipulation = $('th.colNotes').index();
               let table = $('#appTable').DataTable({
                lengthMenu: [
                           [10, 25, 50, 100, -1],
                           [10, 25, 50, 100, "All"]
                       ],
                   order: [[$('th.colResultId').index(), 'desc'],[$('th.colTool').index(), 'asc']],
                   language: {
                      // Setting the text for the search label, mostly to remove the colon that is there by default
                      search: 'Search',
                      select: {
                         aria: {
                             headerCheckbox: 'Select all records'
                         }
                      }
                  },
                 lmsAlly: {
                     checkLabelTargetSelector: 'span.chkLabelDesc'
                 },
                   columnDefs: [
                         {
                             targets: ['.colCheckbox'],
                             orderable: false,
                             // Get the column indexes containing the data that will be used for the checkbox value and name
                             render: DataTable.render.select('.' + $('th.colResultId').index(), '.' + $('th.colCheckboxName').index())
                         },
                         { targets: ['.colFilename', '.colToolId', '.colDeploymentId'], className: 'limited-column-width' },
                         { targets: ['.colNotes'], orderable: false },
                         {
                             // Enabling filters for these columns
                             targets: ['.colUploader', '.colDate', '.colFilename','.colTool', '.colSisCourseId'],
                             lmsFilters: true
                         },
                         {
                            targets: ['.colResultId', '.colBatch', '.colToolId', 'colCheckboxName'], visible: false
                         },
                         {
                            targets: ['.colArchived'], visible: false
                         },
                       ],
                   initComplete: function () {
                       $('#appTable').wrap("<div style='overflow:auto;width:100%;position:relative;'></div>");
                   },
                   select: {
                        selector: 'th:first-child',
                        style: 'multi',
                        info: false
                   },
                   layout: {
                       top2Start: {
                           buttons: {
                                name: 'downloadReport',
                                buttons: [
                                   { extend: 'spacer', text: 'View report', style: 'rvt-ts-23 rvt-text-bold' },
                                   { extend: 'csv', text: 'Download Report', className: 'rvt-button',
                                        exportOptions: {
                                            columns: ['.exportable'],
                                            format: {
                                                body: function ( data, row, column, node, type ) {
                                                    let modData = data;
                                                    // Remove any "extra" elements in this particular column that are sr-only,
                                                    // but would have the content linger when the tags were stripped
                                                    if (column == targetColForExportManipulation) {
                                                        $(node).find('.exportIgnore').each(function(igIndex, igValue) {
                                                            modData = modData.replace($(igValue).prop('outerHTML'), '').trim();
                                                        });
                                                    }
                                                    // Strip out any html (normally the default behavior)
                                                    return $.fn.DataTable.Buttons.stripData( modData, null );
                                                }
                                            }
                                        }
                                   },
                                   { extend: 'spacer', style: 'bar' },
                                   {
                                       name: 'bulkArchive',
                                       text: 'Bulk Archive',
                                       className: 'rvt-button rvt-button--secondary modalButton',
                                       attr: {
                                           id: 'asdf',
                                           'data-rvt-dialog-trigger': 'archive-confirmation',
                                           'aria-describedby': 'users-selected-lower',
                                           'aria-disabled': true,
                                           disabled: 'disabled'
                                       }
                                   },
                                   { extend: 'spacer', text: '0 selected', style: 'rows-selected-text' },
                                   { extend: 'spacer', style: 'bar' },
                                   { extend: 'spacer', text: 'Include archived records', style: 'rvt-ts-23' },
                                   {
                                      text: '<span class="rvt-switch__on">On</span><span class="rvt-switch__off">Off</span>',
                                      className: 'rvt-switch rvt-switch--small',
                                      attr: {
                                          id: 'archive-switch',
                                          'data-rvt-switch': 'archive-switch',
                                          'aria-label': 'Include archived records',
                                          'data-formid': 'show-archived-form',
                                          role: 'switch'
                                      }
                                   }
                               ],
                           },
                       },
                       top1Start: {
                           // Configuration for the filters
                           lmsFilters: {
                               containerClass: 'rvt-flex-md-up rvt-p-bottom-sm rvt-wrap',
                               includeClearFilters: true,
                               descSortOrderFilterNames: ['date']
                           }
                       },
                   },
                   headerCallback: function(thead, data, start, end, display) {
                        // Mark the autogenerated checkbox that ends up in the header with a special class, so it can be excluded from actual selected rows
                        $(thead).find('input[type=checkbox]').addClass('header-checkbox');
                   }
               });

                // Add extra styling for the button group as there wasn't an obvious way to do it on the button group itself
                table.buttons('downloadReport', null).containers().addClass('rvt-button-group rvt-items-center rvt-p-tb-xs');

                let showArchiveStatus = $('#show-archived-action').val();
                if ('show-all' === showArchiveStatus) {
                    table.buttons('downloadReport', '#archive-switch').nodes().attr('data-rvt-switch-on', 'true');
                }

                // Adding event listeners so that we can update controls based on "external" events
                table.on('select deselect user-select filter-update draw', function () {
                    // Update selected counts after row (de)selections and filters
                    // The draw event catches the regular search filtering
                    rowsSelectedCounter();
                });
           });
       }
    }, false);

    // Listener for turning the "show archived" switch on
    document.addEventListener('rvtSwitchToggledOn', event => {
        if (event.srcElement.getAttribute("id") == 'archive-switch') {
            $('#show-archived-action').attr('value', 'show-all');
            buttonLoading(event.srcElement);
        }
    }, false);

    // Listener for turning the "show archived" switch off
    document.addEventListener('rvtSwitchToggledOff', event => {
        if (event.srcElement.getAttribute("id") == 'archive-switch') {
            $('#show-archived-action').attr('value', 'show-unarchived');
            buttonLoading(event.srcElement);
        }
    }, false);

}());

function rowsSelectedCounter() {
    // Get all the selected checkboxes, except the "select-all" one up in the table header
    let newValue = document.querySelectorAll('input.dt-select-checkbox:checked:not(.header-checkbox)').length;
    $(".rows-selected-text").text(newValue + ' selected');

    // enable/disable buttons while we're in here
    if (newValue > 0) {
        $(".modalButton").removeAttr('disabled');
        $(".modalButton").attr('aria-disabled', 'false');
    } else {
        $(".modalButton").attr('disabled', '');
        $(".modalButton").attr('aria-disabled', 'true');
    }
}
