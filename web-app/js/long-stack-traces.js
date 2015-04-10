/*
 * Copyright (c) 2011, Thomas Robinson tom@tlrobinson.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * This file has been modified to remove node specific functionality, among
 * other small changes.
 *
 * Original: https://raw.github.com/tlrobinson/long-stack-traces/cba99ad/lib/long-stack-traces.js
 *
 */
(function() {

    if (!Error.captureStackTrace) {
        console.warn("Skipping long-stack-traces debug helper -- for V8 only");
        return;
    }

    Error.stackTraceLimit = 100;

    var currentTraceError = null;

    var filename = 'long-stack-traces.js';
    function filterInternalFrames(frames) {
        return frames.split("\n").filter(function(frame) { return frame.indexOf(filename) < 0; }).join("\n");
    }

    var originalPrepareStackTrace = Error.prepareStackTrace;
    var newPrepareStackTrace = function(error, frames) {
        var result;
        result = filterInternalFrames(FormatStackTrace(error, frames));
        if (!has.call(error, "__previous")) {
            var previous = currentTraceError;
            while (previous) {
                var previousTrace = previous.stack;
                result += "\nCallback registered at:\n" +
                    "    at " + previous.__location + "\n" +
                    previousTrace.substring(previousTrace.indexOf("\n") + 1);
                previous = previous.__previous;
            }
        }
        return result;
    };

    Error.prepareStackTrace = newPrepareStackTrace;

    /* Note that the stack trace is cached. Once originalStack or stack is
     * called, both will give the same result in subsequent calls! */
    Object.defineProperty(Error.prototype, 'originalStack', {
        enumerable: true,
        get: function Error_originalStack_prop_get() {
            Error.prepareStackTrace = originalPrepareStackTrace;
            var ret = this.stack;
            Error.prepareStackTrace = newPrepareStackTrace;
            return ret;
        }
    });

    var has = Object.prototype.hasOwnProperty;

    // Takes an object, a property name for the callback function to wrap, and an argument position
    // and overwrites the function with a wrapper that captures the stack at the time of callback registration
    function wrapRegistrationFunction(object, property, callbackArg) {
        if (typeof object[property] !== "function") {
            console.error("(long-stack-traces) Object", object, "does not contain function", property);
            return;
        }
        if (!has.call(object, property)) {
            console.warn("(long-stack-traces) Object", object, "does not directly contain function", property);
        }

        // TODO: better source position detection
        var sourcePosition = (object.constructor.name || Object.prototype.toString.call(object)) + "." + property;

        // capture the original registration function
        var fn = object[property];
        if (fn.name == 'wrapped_callback_registering_method') {
            console.warn("(long-stack-traces) Duplicate registration for ",
                property, "on", object);
        }

        // overwrite it with a wrapped registration function that modifies the supplied callback argument
        object[property] = function wrapped_callback_registering_method() {
            // replace the callback argument with a wrapped version that captured the current stack trace
            if (arguments[callbackArg] === null) {
                console.warn("(long-stack-traces) null callback argument " +
                    "provided to (intercepted) callback taking function; " +
                    "will not attempt wrapping the callback");
            } else {
                arguments[callbackArg] = makeWrappedCallback(arguments[callbackArg], sourcePosition);
            }
            // call the original registration function with the modified arguments
            return fn.apply(this, arguments);
        };

        // check that the registration function was indeed overwritten
        if (object[property] === fn)
            console.warn("(long-stack-traces) Couldn't replace ", property, "on", object);
    }

    // Takes a callback function and name, and captures a stack trace, returning a new callback that restores the stack frame
    // This function adds a single function call overhead during callback registration vs. inlining it in wrapRegistationFunction
    function makeWrappedCallback(callback, frameLocation) {
        // add a fake stack frame. we can't get a real one since we aren't inside the original function
        var traceError = new Error();
        traceError.__location = frameLocation;
        traceError.__previous = currentTraceError;

        if (!callback) {
            return;
        }
        return function wrapped_user_callback() {
            if (currentTraceError) {
                 console.warn("(long-stack-traces) A wrapped callback is "
                    + "present is more than one stack frame; probably a " +
                     "wrapped callback was called directly. The stack " +
                     "at the point this wrapped callback for ", frameLocation,
                     " was created will not be reported");
            } else {
                // restore the trace
                currentTraceError = traceError;
            }

            try {
                return callback.apply(this, arguments);
            } catch (e) {
                console.error("Uncaught " + e.stack);
                throw e;
            } finally {
                // clear the trace so we can check that none is set above.
                currentTraceError = null;
            }
        };
    }

    if (typeof window !== "undefined") {
        wrapRegistrationFunction(window.constructor.prototype, "setTimeout", 0);
        wrapRegistrationFunction(window.constructor.prototype, "setInterval", 0);

        /* TODO: support removeEventListener. Probably the easiest way would be
         *       to override replace removeEventListener on a per-object basis
         *       every time addEventListener is called
         *       We can also replace removeEventListener on the prototype,
         *       as long as we save the original function in a property of
         *       the wrapped function. Then we can loop through the events
         *       (obtained with the global getEventListeners()) and call the
         *       real removeEventListener().
         */
        [
            window.Node.prototype,
            window.MessagePort.prototype,
            window.SVGElementInstance.prototype.__proto__,
            window.WebSocket.prototype,
            window.XMLHttpRequest.prototype,
            window.EventSource.prototype,
            window.XMLHttpRequestUpload.prototype,
            window.SharedWorker.prototype.__proto__,
            window.constructor.prototype,
            window.applicationCache.constructor.prototype
        ].forEach(function(object) {
            wrapRegistrationFunction(object, "addEventListener", 1);
        });

        var onreadystatechangeDescriptor = {
            get: function XMLHttpRequest_wrapping_onreadystatechange_getter() {
                return this._onreadystatechange;
            },
            set: function XMLHttpRequest_wrapping_onreadystatechange_setter(newCallback) {
                if (this._onreadystatechange) {
                    this.removeEventListener("readystatechange", this._onreadystatechange);
                }

                if (newCallback === null) {
                    console.warn("(long-stack-traces) setting onreadystatechange " +
                        "on XMLHttpRequest object to null; will not attempt " +
                        "wrapping the callback");
                    this._onreadystatechange = null;
                } else {
                    /* addEventListener already wraps the callback */
                    this.addEventListener("readystatechange", newCallback);
                }
            }
        };
        var _onreadystatechangeDescriptor = {
            value: null,
            writable: true,
            enumerable: false
        };

        /* for some reason setting the property on the prototype is not enough */
        var _XMLHttpRequest = XMLHttpRequest;
        XMLHttpRequest = function (arg) {
            var actualThis = new _XMLHttpRequest(arg);

            Object.defineProperty(actualThis, 'onreadystatechange', onreadystatechangeDescriptor);
            Object.defineProperty(actualThis, '_onreadystatechange', _onreadystatechangeDescriptor);

            return actualThis;
        };
        XMLHttpRequest.prototype = _XMLHttpRequest.prototype;
        Object.getOwnPropertyNames(_XMLHttpRequest).forEach(function(it) {
            if (it == 'prototype' || it == 'name' || it == 'length')
                return;

            Object.defineProperty(XMLHttpRequest, it,
                Object.getOwnPropertyDescriptor(_XMLHttpRequest, it));
        });



//        this actually captures the stack when "send" is called, which isn't ideal,
//        but it's the best we can do without hooking onreadystatechange assignments
//        use this only if hooking onreadystatechange fails
//        var _send = XMLHttpRequest.prototype.send;
//        XMLHttpRequest.prototype.send = function() {
//            if (!Object.getOwnPropertyDescriptor(this, '_onreadystatechange')) {
//                var wrappedCallback = makeWrappedCallback(this.onreadystatechange, "onreadystatechange");
//                Object.defineProperty(this, "_onreadystatechange", backingDescriptor);
//                Object.defineProperty(this, "onreadystatechange", onreadystatechangeDescriptor);
//                this._onreadystatechange = wrappedCallback
//            }
//            return _send.apply(this, arguments);
//        }
    }

    function FormatStackTrace(error, frames) {
        var lines = [];
        try {
            lines.push(error.toString());
        } catch (e) {
            try {
                lines.push("<error: " + e + ">");
            } catch (ee) {
                lines.push("<error>");
            }
        }
        for (var i = 0; i < frames.length; i++) {
            var frame = frames[i];
            var line;
            try {
                line = FormatSourcePosition(frame);
            } catch (e) {
                try {
                    line = "<error (no position): " + e + ">";
                } catch (ee) {
                    // Any code that reaches this point is seriously nasty!
                    line = "<error>";
                }
            }
            lines.push("    at " + line);
        }
        return lines.join("\n");
    }

    function FormatSourcePosition(frame) {
        var fileLocation = "";
        if (frame.isNative()) {
            fileLocation = "native";
        } else if (frame.isEval()) {
            fileLocation = "eval at " + frame.getEvalOrigin();
        } else {
            var fileName = frame.getFileName();
            if (fileName) {
                fileLocation += fileName;
                var lineNumber = frame.getLineNumber();
                if (lineNumber != null) {
                    fileLocation += ":" + lineNumber;
                    var columnNumber = frame.getColumnNumber();
                    if (columnNumber) {
                        fileLocation += ":" + columnNumber;
                    }
                }
            }
        }
        if (!fileLocation) {
            fileLocation = "unknown source";
        }
        var line = "";
        var functionName = frame.getFunctionName();
        var isConstructor = frame.isConstructor();
        var isMethodCall = !(frame.isToplevel() || isConstructor);
        if (isMethodCall) {
            var methodName = frame.getMethodName();
            line += frame.getTypeName() + ".";
            if (functionName) {
                line += functionName;
                if (methodName && (methodName != functionName)) {
                    line += " [as " + methodName + "]";
                }
            } else {
                line += methodName || "<anonymous>";
            }
        } else if (isConstructor) {
            line += "new " + (functionName || "<anonymous>");
        } else if (functionName) {
            line += functionName;
        } else {
            line += "<anonymous>";
        }

        line += " (" + fileLocation + ")";

        return line;
    }
})();
