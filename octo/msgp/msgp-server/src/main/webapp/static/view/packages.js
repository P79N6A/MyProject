M.add('msgp-view/packages', function(Y){
    var packages = {
        root: function(arr) {
            var map = {};
            var j = -1,
                lastGroup;
            console.log("The arr is: %o", arr);
            var filteredArr = arr;

            function find(name, data) {
                var node = map[name],
                    i;
                if (!node) {
                    node = map[name] = data || {
                        name: name,
                        children: []
                    };
                    if (name.length) {
                        i = name.lastIndexOf( "|" );
                        node.parent = find( name.substring(0, i = name.lastIndexOf("|")) );
                        node.parent.children.push( node );
                        node.key = name.substring( i + 1 );
                    }
                }
                return node;
            }
            //按照name做一个排序，保证同一个组的数据在索引上连在一起
            filteredArr.sort(function(a,b){
                return a.name.charCodeAt(0) - b.name.charCodeAt(0);
            });
            filteredArr.forEach(function(d) {
                var thisGroup = d.name.split('|')[0];
                if( lastGroup && lastGroup === thisGroup ){
                    d.color = j;
                }else{
                    lastGroup = thisGroup;
                    d.color = ++j;
                }
                find(d.name, d);
            });
            return map[""];
        },

        // Return a list of imports for the given array of nodes.
        imports: function(nodes) {
            var map = {},
                imports = [];

            // Compute a map from name to node.
            nodes.forEach(function(d) {
                map[d.name] = d;
            });

            // For each import, construct a link from the source to target node.
            nodes.forEach(function(d) {
                if (d.from) {
                    d.from.forEach(function(item) {
                        imports.push({
                            target: map[d.name],
                            source: map[item]
                        });
                    });
                }
                if (d.to) {
                    d.to.forEach(function(item) {
                        imports.push({
                            source: map[d.name],
                            target: map[item]
                        });
                    });
                }
            });
            return imports;
        }
    };
    Y.namespace('msgp.view').packages = packages;
}, '0.0.1', {
    requires : [
    ]
});
