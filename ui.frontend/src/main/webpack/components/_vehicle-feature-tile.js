(function () {
    "use strict";

    var selectors = {
        self: '[data-cmp-is="vehicle-feature-tile"]'
    };

    function VehicleFeatureTile(config) {
        function init(config) {
            config.element.removeAttribute("data-cmp-is");

            const analyticsId = config.element.dataset.analyticsId;

            if (analyticsId && "IntersectionObserver" in window) {
                const observer = new IntersectionObserver(
                    (entries, obs) => {
                        entries.forEach((entry) => {
                            if (entry.isIntersecting) {
                                config.element.dispatchEvent(
                                    new CustomEvent("title:viewed", {
                                        bubbles: true,
                                        detail: { analyticsId },
                                    })
                                );
                                obs.disconnect();
                            }
                        });
                    },
                    { threshold: 0.5 }
                );
                observer.observe(config.element);
            }
        }

        if (config && config.element) {
            init(config);
        }
    }

    // Best practice:
    // Use a method like this mutation obeserver to also properly initialize the component
    // when an author drops it onto the page or modified it with the dialog.
    function onDocumentReady() {
        var elements = document.querySelectorAll(selectors.self);
        for (var i = 0; i < elements.length; i++) {
            new VehicleFeatureTile({ element: elements[i] });
        }

        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body = document.querySelector("body");
        var observer = new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                // needed for IE
                var nodesArray = [].slice.call(mutation.addedNodes);
                if (nodesArray.length > 0) {
                    nodesArray.forEach(function (addedNode) {
                        if (addedNode.querySelectorAll) {
                            var elementsArray = [].slice.call(addedNode.querySelectorAll(selectors.self));
                            elementsArray.forEach(function (element) {
                                new VehicleFeatureTile({ element: element });
                            });
                        }
                    });
                }
            });
        });

        observer.observe(body, {
            subtree: true,
            childList: true,
            characterData: true
        });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }

}());
