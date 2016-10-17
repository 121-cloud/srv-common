(function() {
    'use strict';

    angular
        .module('app.eventbus')
        .run(appRun);

    appRun.$inject = ['routerHelper'];
    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'eventbus',
                config: {
                    url: '/eventbus',
                    templateUrl: 'app/eventbus/index.html',
                    controller: 'indexController',
                    controllerAs: 'vm',
                    title: 'EventBus',
                    settings: {
                        nav: 1,
                        content: '<i class="fa fa-dashboard"></i> EventBus'
                    }
                }
            }
        ];
    }
})();
