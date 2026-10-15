// Serve Compose resources (the bundled Bravura font) at the URL the web resource reader requests.
const path = require("path");
config.files.push({
    pattern: path.resolve(config.basePath, "kotlin/composeResources/**/*"),
    included: false,
    served: true,
    watched: false
});
config.proxies["/composeResources/"] = "/base/kotlin/composeResources/";
