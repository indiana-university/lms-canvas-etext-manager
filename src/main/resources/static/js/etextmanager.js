// Placeholder js file

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
                $('#toolInfoTable').DataTable({
                    columnDefs: [{ targets: 3, orderable: false }]
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
                       .columns( [0, 1, 3, 4, 5] )
                       .every(function () {
                           var column = this;
                           var select = $('<select class="rvt-select"><option value="">All</option></select>')
                               .appendTo($("#appTable thead tr:eq(1) th").eq(column.index()).empty())
                               .on('change', function () {
                                   var val = $.fn.dataTable.util.escapeRegex($(this).val());
                                   column.search(val ? '^' + val + '$' : '', true, false).draw();
                               })
                               .on( 'click' , function (evt) {
                                   evt.stopPropagation();
                               });
                           column
                               .data()
                               .unique()
                               .sort()
                               .each(function (d, j) {
                                   select.append('<option value="' + d + '">' + d + '</option>');
                               });
                       });
                       this.api()
                       .columns( [2] )
                       .every(function () {
                           let column = this;
                           var textinput = $('<input type="text" class="rvt-text-input" placeholder="Date">')
                               .appendTo($("#appTable thead tr:eq(1) th").eq(column.index()).empty())
                               .on('keyup', function () {
                                   var val = $.fn.dataTable.util.escapeRegex($(this).val());
                                   column.search(val).draw();
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