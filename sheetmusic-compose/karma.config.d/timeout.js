// Rendering tests load the Skiko runtime and fetch the Bravura font, which exceeds Mocha's 2 s default.
config.set({
    client: {
        mocha: {
            timeout: 60000
        }
    }
});
