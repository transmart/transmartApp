/**
 *
 * Dynamic Load is a utility class to load web resources dynamically
 *
 */

function DynamicLoad () {
    return this;
}

DynamicLoad.prototype = {

    headID : document.getElementsByTagName("head")[0],

    /**
     * this function will load css dynamically
     * @param uri
     */
    loadCSS : function (uri) {
        var cssNode = document.createElement('link');
        cssNode.type = 'text/css';
        cssNode.rel = 'stylesheet';
        cssNode.href = uri;
        cssNode.media = 'screen';
        this.headID.appendChild(cssNode);
    },

    /**
     * This funciton will load javascript dynamically
     * @param uri
     */
    loadJS : function (url, callback) {
        var script = document.createElement("script");
        script.type = "text/javascript";

        if (script.readyState){  //IE
            script.onreadystatechange = function(){
                if (script.readyState == "loaded" ||
                    script.readyState == "complete"){
                    script.onreadystatechange = null;
                    callback();
                }
            };
        } else {  //Others
            script.onload = function(){
                callback();
            };
        }

        script.src = url;
        document.getElementsByTagName("head")[0].appendChild(script);
    },


    loadJSCollection : function (coll) {

        console.log(coll);
        for (var i = 0; i < coll.length; i++) {
                this.loadJS(coll[i]);
        }
    },

    loadScriptsSequential : function (scriptsToLoad, callback) {

        function loadNextScript() {
            var done = false;
            var head = document.getElementsByTagName('head')[0];
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.onreadystatechange = function () {
                if (this.readyState == 'complete' || this.readyState == 'loaded') {
                    scriptLoaded();
                }
            };
            script.onload = scriptLoaded;
            script.src = scriptsToLoad.shift(); // grab next script off front of array
            head.appendChild(script);

            function scriptLoaded() {
                // check done variable to make sure we aren't getting notified more than once on the same script
                if (!done) {
                    script.onreadystatechange = script.onload = null;   // kill memory leak in IE
                    done = true;
                    if (scriptsToLoad.length != 0) {
                        loadNextScript();
                    } else {
                        callback();
                    }
                }
            }
        }

        loadNextScript();

    }
};

var dynamicLoad = new DynamicLoad();