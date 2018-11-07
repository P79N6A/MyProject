<script src="https://cs0.meituan.net/yuipagecore/1.0.11/footer.js" type="text/javascript"></script>
<script src="https://cs0.meituan.net/cosui/1.3.0/js/sidebar/sidebar-native.js" type="text/javascript"></script>
<script>
    M.addGroups({
        "ui": {
            base: "https://cs0.meituan.net/",
            root: "/cos.ui/",
            prefixs: "ui"
        }
    });
    M.use('ui-main', function(Y) {
        // cos.ui初始化
        Y.mt.ui.Main.init();
    });
</script>
