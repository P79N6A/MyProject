M.add('msgp-view/relation', function(Y){
    var relation = {
        init : function(){
            this.chartInit();
        },
        chartInit : function() {
            var colors = ['fd78bd', 'a186be', '662d91', '5ea95c', 'ffdd00', '6dcff6', 'd74d94', '46142e', 'f26d7d', '5dbab9', '80bb42', 'cacec2', 'f1b867', '003663', 'f5989d', 'cd6f3c', '00a99d', '2e5a59', 'fff799', 'fbaf5d', '003663', '052a24', 'fff799', 'fbaf5d', '007236', 'aa71aa', 'bbbb42', '9ac2b9', '1d3b56', 'f26c4f', 'ee3224', 'fed42a', '82ca9c', 'aaa6ce', '455870', '0b6e5f', '00aeef', '448ccb', '7b0046', 'c4d9ec'];
            var w = 1100,
                h = 1000,
                rx = w / 2 - 100,
                ry = h / 2 - 100;

            var cluster = d3.layout.cluster()
                .size([360, ry - 120])
                //.sort(function(a, b) { return d3.ascending(a.name, b.name); })

            var bundle = d3.layout.bundle();

            var line = d3.svg.line.radial()
                .interpolate("bundle")
                .tension(.85)
                .radius(function(d) {
                    return d.y;
                })
                .angle(function(d) {
                    return d.x / 180 * Math.PI;
                });

            Y.one('#d3_wrapper').setHTML('');
            var div = d3.select("#d3_wrapper");

            var svg = div.append("svg:svg")
                .attr("width", w)
                .attr("height", w)
                .append("svg:g")
                .attr("transform", "translate(" + (rx + 100) + "," + (ry + 100) + ")");

            svg.append("svg:path")
                .attr("class", "arc")
                .attr("d", d3.svg.arc().outerRadius(ry - 120).innerRadius(0).startAngle(0).endAngle(2 * Math.PI));
                //.on("mousedown", mousedown);

            var url = '/graph/api/circle';
            /*
            d3.json(url, function(error, ret) {
                if( ret.isSuccess ){
                    var arr = ret.data;
                    drawCharts( arr );
                }
            });
            */
            Y.io(url, {
                method : 'get',
                on : {
                    success : function(id ,o){
                        var ret = Y.JSON.parse( o.responseText );
                        if( ret.isSuccess ){
                            var arr = ret.data;
                            drawCharts( arr );
                        }else{
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        }
                    },
                    failure : function(){
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                }
            });
            function drawCharts( arr ){
                var fclasses = Y.msgp.view.packages.root( arr );
                var nodes = cluster.nodes(fclasses);
                var links = Y.msgp.view.packages.imports(nodes);
                var splines = bundle(links);

                var path = svg.selectAll("path.link")
                    .data(links)
                    .enter().append("svg:path")
                    .attr("class", function(d) {
                        return "link source-" + opStr(d.source.key) + " target-" + opStr(d.target.key);
                    })
                    .attr("d", function(d, i) {
                        return line(splines[i]);
                    });

                var label = svg.selectAll("g.node")
                    .data(nodes.filter(function(n) {
                        return !n.children;
                    }))
                    .enter().append("svg:g")
                    .attr("class", "node")
                    .attr("id", function(d) {
                        return "node-" + opStr(d.key);
                    })
                    .attr("transform", function(d) {
                        return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")";
                    });


                label.append("circle")
                    .attr("cx", 0)
                    .attr("cy", 0)
                    .attr("fill", function(d, i) {
                        return '#' + colors[d.color];
                    })
                    .attr("opacity", 1.0)
                    .attr("r", function(d, i) {
                        return 6;
                    });

                label.append("svg:text")
                    .attr("dx", function(d) {
                        return d.x < 180 ? 20 : -20;
                    })
                    .attr("dy", "0.31em")
                    .attr("font-size", "1.2em")
                    .attr("fill", function(d, i) {
                        return '#' + colors[d.color];
                    })
                    .attr("text-anchor", function(d) {
                        return d.x < 180 ? "start" : "end";
                    })
                    .attr("transform", function(d) {
                        return d.x < 180 ? null : "rotate(180)";
                    })
                    .text(function(d) {
                        var showName = d.key;
                        if (showName.length > 20) {
                            showName = d.key.slice(0, 19);
                            showName = showName + '...';
                        }
                        return showName;
                    })
                    .on('click', function(d, i) {
                        clickNode(d, i);
                    })
                    .on("mouseover", function(d, i) {
                        mouseover(d, i);
                    })
                    .on("mouseout", mouseout)
                    .append("svg:title")
                    .text(function(d) {
                        return d.name.replace('|', ' ');
                    });
            }

            function clickNode(d) {
                logSourceTarget(d);
                // remove all target source class
                svg.selectAll("path.link.target-click").classed("target-click", false);
                svg.selectAll("path.link.source-click").classed("source-click", false);
                svg.selectAll("g.target-click").classed("target-click", false);
                svg.selectAll("g.source-click").classed("source-click", false);

                svg.select("#node-" + opStr(d.key)).classed("source-click", true);

                svg.selectAll("path.link.target-" + opStr(d.key))
                    .classed("target-click", true)
                    .each(updateNodesClick("source", true));

                svg.selectAll("path.link.source-" + opStr(d.key))
                    .classed("source-click", true)
                    .each(updateNodesClick("target", true));
            }

            function logSourceTarget(d) {
                console.dir( d );
                console.info("from: " + d.from.join(' && '));
                console.info("to: " + d.to.join(' && '));
            }

            function mouseover(d) {
                svg.selectAll("path.link.target-" + opStr(d.key))
                    .classed("target", true)
                    .each(updateNodes("source", true));

                svg.selectAll("path.link.source-" + opStr(d.key))
                    .classed("source", true)
                    .each(updateNodes("target", true));

            }

            function mouseout(d) {
                svg.selectAll("path.link.source-" + opStr(d.key))
                    .classed("source", false)
                    .each(updateNodes("target", false));

                svg.selectAll("path.link.target-" + opStr(d.key))
                    .classed("target", false)
                    .each(updateNodes("source", false));
            }

            function updateNodesClick(name, value) {
                var nmap = {
                    target: 'target-click',
                    source: 'source-click'
                }
                return function(d) {
                    svg.select("#node-" + opStr(d[name].key)).classed(nmap[name], value);
                };
            }

            function updateNodes(name, value) {
                return function(d) {
                    svg.select("#node-" + opStr(d[name].key)).classed(name, value);
                };
            }

            function cross(a, b) {
                return a[0] * b[1] - a[1] * b[0];
            }

            function dot(a, b) {
                return a[0] * b[0] + a[1] * b[1];
            }
            //替换掉字符串中的.
            //TODO:替换掉所有css选择器相关字符，if needed
            function opStr(str){
                return str.replace(/\./g, '-');
            }
        }
    };
    Y.namespace('msgp.view').relation = relation;
}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'msgp-view/packages',
        'msgp-utils/msgpHeaderTip'
    ]
});
