'use strict';

// 1) Instantiate all dependency module instances.
//    This AngularJS module is defined in a local JavaScript file.
angular.module('github_light', ['ngResource']);

// 2) Create application instance; inject dependency module references.
var app = angular.module('DocumentationApp', ['ngRoute', 'ngSanitize', 'ngResource', 'mgcrea.ngStrap', 'github_light']);

// 3) Configure the application.
//    ("Constructor" call, invoked before scopes are defined/injected.)
app.config(['$routeProvider', function ($routeProvider) {

    // Define routes
    $routeProvider.when('/repo', {
        templateUrl: 'app/modules/github/repoView.html',
        controller: 'githubDocController'
    }).otherwise({
        redirectTo: '/repo'
    });
}
]);

// 4) Initialize the application's rootScope state.
//    ("Constructor" call, invoked *after* scopes are defined/injected.)
app.run(['$rootScope', function ($rootScope) {

    /**
     * Prototype function for creating a Theme structure.
     *
     * @param name The name of the returned Theme. Must not contain whitespace, and should be a
     * legal CSS class name. The name of the selected theme is added as a CSS class to the 'body' element.
     * @param structureID The id of the structure path used for including header and footer markup files.
     * Must not contain whitespace.
     */
    $rootScope.createTheme = function (name, structureID) {

        /**
         * The active theme's name.
         */
        this.name = name;

        /**
         * Retrieves a path to the theme's Footer file, which should be an include-able HTML file.
         * @returns {string} The path to the theme's Footer file, which should be an include-able HTML file.
         */
        this.footer = function () {
            return 'app/shared/theme/' + structureID + '/footer.html';
        };

        /**
         * Retrieves a path to the theme's top NavBar file, which should be an include-able HTML file.
         * @returns {string} The path to the theme's top NavBar file, which should be an include-able HTML file.
         */
        this.topNavBar = function () {
            return 'app/shared/theme/' + structureID + '/topNavbar.html';
        };
    };

    /**
     * Sets the theme with the supplied themeName as the active/selected theme.
     *
     * @param themeName The name of the theme to select.
     */
    $rootScope.setTheme = function (themeName) {

        // Find the theme with the supplied name
        for (var i = 0; i < $rootScope.themes.length; i++) {
            if ($rootScope.themes[i].name === themeName) {

                // Set the new (?) theme
                $rootScope.theme = $rootScope.themes[i];

                // All done.
                return;
            }
        }
    };

    // Shared state: All available themes.
    $rootScope.themes = [
        new $rootScope.createTheme('pale', 'standard'),
        new $rootScope.createTheme('mint', 'standard'),
        new $rootScope.createTheme('pink', 'standard')];

    // Shared state: The active theme.
    $rootScope.setTheme('mint');
}]);