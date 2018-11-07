M.add('msgp-servicerep/dailyReport', function (Y) {
    Y.namespace('msgp.servicerep').dailyReport = dailyReport;
    var _day;
    var startInput = Y.one('#start_time');
    
    function dailyReport(day) {
        _day = day;
        document.title = '服务治理日报';
        initDatePicker();
    }

    function initDatePicker() {
        var dayInput = Y.one('#start_time');
        var dayTime = Date.parse(_day);
        var dayDate = new Date(dayTime);
        var sDate = new Y.mt.widget.Datepicker({
            node: dayInput,
            showSetTime: false
        });
        sDate.on('Datepicker.select', function () {
            refreshData()
        });
        dayInput.set('value', Y.mt.date.formatDateByString(dayDate, 'yyyy-MM-dd'));
    }

    function refreshData() {
        var day = startInput.get("value");
        window.location.href = "/repservice/daily/?day="+day;
    }
    
}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'mt-date',
        'w-date'
    ]
});
