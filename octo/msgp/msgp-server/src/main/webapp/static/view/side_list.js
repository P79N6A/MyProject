(function(){
	var graphId = 100;
	var sideData = [null, null, null];
	var activeList = 0;
	function newApps(data) {
		if(activeList != 0) return;
		if(sideData[0] === null) {
			setTimeout(function(){
				newApps();
			}, 100);
			return;
		}
		data = data || sideData[0];
		var th = ['<thead>',
			'<tr>',
				'<th>appkey</th>',
				'<th>负责人</th>',
				'<th>介绍</th>',
	            '<th>业务线</th>',
				'<th>创建时间</th>',
			'</tr>',
		'</thead>'].join();
		var tb = ['<tbody>',
			'<tr>',
				'<td>{appkey}</td>',
				'<td>{owners}</td>',
				'<td>{intro}</td>',
				'<td>{pdl}</td>',
				'<td>{createTime}</td>',
			'</tr>',
		'</tbody>'].join();
		var tempTb,resTb,value;
		for(var i=0; i<data.length; i++) {
			tempTb = tb;
			for(var key in data[i]) {
				value = data[i][key];
				if(key === 'owners') {
					value = value.map(function(item) {
						return item.name
					}).join(',')
				}
				if(key === 'createTime') {
					value = new Date(value*1000).toLocaleString();
				}
				if(key === 'pdl') {
					value =  data[i][owt] +"-" + value;
				}
				tempTb = tempTb.replace('{'+key+'}', value);
			}
			resTb += tempTb;
		}
		$('#side-table').html(th+resTb);
	}
	function newSpannames(data) {
		if(activeList != 1) return;
		if(sideData[1] === null) {
			setTimeout(function(){
				newSpannames();
			}, 100);
			return;
		}
		data = data || sideData[1];
		var th = ['<thead>',
			'<tr>',
				'<th>appkey</th>',
				'<th>api</th>',
				'<th>qps</th>',
				'<th>tp50</th>',
				'<th>tp90</th>',
				'<th>tp95</th>',
				'<th>tp99</th>',
			'</tr>',
		'</thead>'].join();
		var tb = ['<tbody>',
			'<tr>',
				'<td>{appkey}</td>',
				'<td>{spanname}</td>',
				'<td>{qps}</td>',
				'<td>{tp50}</td>',
				'<td>{tp90}</td>',
				'<td>{tp95}</td>',
				'<td>{tp99}</td>',
			'</tr>',
		'</tbody>'].join();
		var tempTb,resTb;
		for(var i=0; i<data.length; i++) {
			tempTb = tb;
			for(var key in data[i]) {
				tempTb = tempTb.replace('{'+key+'}', data[i][key]);
			}
			resTb += tempTb;
		}
		$('#side-table').html(th+resTb);
	}
	function apiTop20(data) {
		if(activeList != 2) return;
		if(sideData[2] === null) {
			setTimeout(function(){
				apiTop20();
			}, 100);
			return;
		}
		data = data || sideData[2];
		var th = ['<thead>',
			'<tr>',
				'<th>appkey</th>',
				'<th>api</th>',
				'<th>qps</th>',
				'<th>tp50</th>',
				'<th>tp90</th>',
				'<th>tp95</th>',
				'<th>tp99</th>',
			'</tr>',
		'</thead>'].join();
		var tb = ['<tbody>',
			'<tr>',
				'<td>{appkey}</td>',
				'<td>{spanname}</td>',
				'<td>{qps}</td>',
				'<td>{tp50}</td>',
				'<td>{tp90}</td>',
				'<td>{tp95}</td>',
				'<td>{tp99}</td>',
			'</tr>',
		'</tbody>'].join();
		var tempTb,resTb;
		for(var i=0; i<data.length; i++) {
			tempTb = tb;
			for(var key in data[i]) {
				tempTb = tempTb.replace('{'+key+'}', data[i][key]);
			}
			resTb += tempTb;
		}
		$('#side-table').html(th+resTb);
	}

	// 服务列表
	$('.side-nav').click(function(e) {
		$(this).children().removeAttr('class');
		var target = e.target.parentNode;
		var index = $(target).attr('data-index');
		target.className = 'active';
		$('#side-table').html('加载中...');
		activeList = index;
		switch(index){
			case '0':
				newApps(sideData[0]);
				break;
			case '1':
				newSpannames(sideData[1]);
				break;
			case '2':
				apiTop20(sideData[2]);
				break;
		}
	})
	// 新加入服务
	$('#side-table').html('加载中...');
	$.ajax({
		type: "GET",
	    url: "/graph/new/apps?id=" + graphId,
	    dataType: "JSON"
	}).done(function(res) {
		if(res.isSuccess === true && res.data){
	        var data = res.data;
	        sideData[0] = data;
	        newApps(sideData[0]);
	    }
	    else{
	        console.log('error')
	    }
	});
	// 新加入api
	$.ajax({
		type: "GET",
	    url: "/graph/new/spannames?id=" + graphId,
	    dataType: "JSON"
	}).done(function(res) {
		if(res.isSuccess === true && res.data){
	        var data = res.data;
	        sideData[1] = data;
	    }
	    else{
	        console.log('error')
	    }
	});
	// 性能最差api20
	$.ajax({
		type: "GET",
	    url: "/graph/perfWorst/spannames?id=" + graphId,
	    dataType: "JSON"
	}).done(function(res) {
		if(res.isSuccess === true && res.data){
	        var data = res.data;
	        sideData[2] = data;
	    }
	    else{
	        console.log('error')
	    }
	});
})();