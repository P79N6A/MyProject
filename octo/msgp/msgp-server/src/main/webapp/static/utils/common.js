/**
 * Created by wudechang on 16/7/25.
 * add common function
 */
M.add('msgp-utils/common', function (Y) {
    Y.namespace('msgp.utils').urlAddParameters = urlAddParameters;
    Y.namespace('msgp.utils').addTooltip = addTooltip;
    Y.namespace('msgp.utils').addTooltipWithContent = addTooltipWithContent;
    Y.namespace('msgp.utils').GuideUser = GuideUser;
    Y.namespace('msgp.utils').urlParameters = getParamaterObjByHref;
    function urlAddParameters(obj) {
        var parameters = "?";
        for (var key in obj) {
            if (parameters === "?") {
                parameters = parameters + key + "=" + obj[key];
            } else {
                parameters = parameters + "&" + key + "=" + obj[key];
            }
        }
        window.history.pushState({}, 0, window.location.pathname + parameters + window.location.hash);
    };
    function offSetTop(elements) {
        var top = elements.get('offsetTop');
        var parent = elements.offsetParent;
        while (parent != null) {
            top += parent.get('offsetTop');
            parent = parent.get('offsetParent');
        }
        ;
        return top;
    };
    function offSetLeft(elements) {
        var left = elements.get('offsetLeft');
        var parent = elements.offsetParent;
        while (parent != null) {
            left += parent.get('offsetLeft');
            parent = parent.get('offsetParent');
        }
        ;
        return left;
    };
    function GuideUser(arrElTarget) {
        addElementToBody();
        var elCover = Y.one('#cover'),
            index = 0;
        coverGuide(elCover, arrElTarget[index]);

        elCover.on('click', function () {
            index++;
            if (!arrElTarget[index]) {
                index = 0;
            }
            coverGuide(elCover, arrElTarget[index]);
        });
        Y.one('.close').on('click', function (e) {
            e.preventDefault;
            elCover.remove();
            this.remove();
        });
    };
    function coverGuide(cover, target) {
        var body = Y.one('body');
        if (cover && target) {
            // target size(width/height)
            var targetWidth = target.get('clientWidth'),
                targetHeight = target.get('clientHeight');

            // page size
            var pageHeight = body.get('scrollHeight'),
                pageWidth = body.get('scrollWidth');

            // offset of target
            var offsetTop = target.getXY().top;
            var offsetLeft = target.getXY().left;

            // set size and border-width
            cover.setStyle('width', targetWidth + 'px');
            cover.setStyle('height', targetHeight + 'px');
            cover.setStyle('borderWidth',
                offsetTop + 'px ' +
                (pageWidth - targetWidth - offsetLeft) + 'px ' +
                (pageHeight - targetHeight - offsetTop) + 'px ' +
                offsetLeft + 'px');

            cover.setStyle('display', 'block');

            // resize
            if (!cover.isResizeBind) {
                if (window.addEventListener) {
                    window.addEventListener('resize', function () {
                        coverGuide(cover, target);
                    });
                    cover.isResizeBind = true;
                } else if (window.attachEvent) {
                    window.attachEvent('onresize', function () {
                        coverGuide(cover, target);
                    });
                    cover.isResizeBind = true;

                    // IE7, IE8 box-shadow hack
                    cover.innerHTML = '<img src="guide-shadow.png">';
                }
            }
        }
    };
    function addElementToBody() {
        var parent = Y.one("body");
        var str = '<div id="cover"></div>' +
            '<a href="javascript:;" class="close" style="position: absolute; top: 0; right: 0; color: #fff; z-index: 999;">[关闭]</a>';
        parent.append(str);
    };

    function addTooltip(obj) {
        $.each(obj, function (name, value) {
            $(name).tooltip({
                html: true,
                title: "&nbsp;&nbsp;详细信息见 Wiki : <a href=" + value + " target='_blank'>戳我直达!&nbsp;&nbsp;</a>",
                delay: {
                    hide: 100
                },
                container: $(name)
            });
        })

    }

    function addTooltipWithContent(element, content) {
        $(element).tooltip({
            title: content,
            delay: {
                hide: 100
            },
            container: $(element)
        });
    }

    function getParamaterObjByHref() {
        var result = {};
        var url = location.href.split('#')[0];
        var reg = new RegExp('([\\?|&])(.+?)=([^&?]*)', 'ig');
        var arr = reg.exec(url);

        while (arr) {
            result[arr[2]] = arr[3];
            arr = reg.exec(url);
        }
        return result;
    }
}, '0.0.1', {});
