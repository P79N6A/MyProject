<link href="/static/user/assets/user.css" rel="stylesheet">

<div class="config-user J-config-user">
    <h1 class="config-user-title J-config-user-title"></h1>
    <div class="loading fa fa-spinner fa-spin fa-3x"></div>
    <div class="config-user-content J-config-user-content"></div>
</div>

<script>
YUI({
    'config-user': {
        render: true,
        container: '.J-config-user'
    }
}).use('config-user');
</script>
