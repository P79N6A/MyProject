<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="form-inline mb20">
        <a>配置类型：</a>

        <div id="dynamic_or_file" class="btn-group">
            <a value="1" type="button" id="supplier_type" class="btn btn-primary" href="javascript:void(0)">动态配置</a>
            <a value="2" type="button" id="supplier_type" class="btn btn-default" href="javascript:void(0)">文件配置</a>
        </div>

        <div id="dynamic_input" style="display: none; margin-top: 10px;">
           sg_agent的IP地址:
            <input id="search_ip" class="mb5 span2" type="text" placeholder="IP地址"></input>
            <button id="search_button" class="btn btn-primary" type="button" style="margin-bottom: 5px;">查询</button>
        </div>
        <div id="file_input" style="display: none;margin-top: 10px;">
            sg_agent的IP地址:
            <input id="search_file_ip" class="mb5 span2" type="text" placeholder="IP地址"></input>
            &nbsp 文件名:
            <input id="search_file_name" class="mb5 span2" type="text" placeholder="文件名"></input>
            <button id="search_file_button" class="btn btn-primary" type="button" style="margin-bottom: 5px;">查询
            </button>
        </div>

    </div>

    <div class="content-body">
        <div class="content-overlay">
            <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
        </div>
        <div id="dynamic_data" style="padding-left: 30px;">
            <textarea id="dynamic_data_value"  type="text" style="width: 100%;height: 400px; " readonly ></textarea>
        </div>

        <div id="file_data" style="padding-left: 30px;">
            <textarea id="file_data_value"  type="text" style="width: 100%;height: 400px; " readonly ></textarea>
        </div>

    </div>

</div>