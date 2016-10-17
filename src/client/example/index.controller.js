/* jshint -W106, -W040 */
(function() {
    'use strict';

    angular
        .module('app.eventbus')
        .controller('indexController', indexController);

    indexController.$inject = ['$rootScope', '$scope', '$state', 'logger'];
    /* @ngInject */
    function indexController($rootScope, $scope, $state, logger) {
		var vm = this;

        vm.eventbus = null;
		vm.messageCount = 12;
        vm.serverurl = "http://localhost:9000/eventbus";
        vm.result = (vm.eventbus && vm.eventbus.state) || "have not connect to server";
        vm.consumeraddress = "eventbusjs.consumer.test1";
        vm.consumer = null;
        vm.consumerreplycontent = JSON.stringify({result: "succeeded"});
        vm.consumerrequestcontent = null;
        vm.consumerrespondcontent = null;
        vm.senderrequestparams = JSON.stringify({param1: "hello"});
        vm.senderrequestcontent = JSON.stringify(new Array({}));
        vm.senderrequestsession = JSON.stringify({"sessionid":""});
        
        vm.accountid = 1001001;
        vm.action = "GET";
        vm.eventbusReady = false;
        vm.canmultitimereply = false;

        vm.connectToServer = function() {
            if (vm.eventbus === null || vm.eventbus.state === EventBus.CLOSED) {
                vm.eventbus = new EventBus(vm.serverurl);
                vm.eventbus.onopen = onEventBusConnected;
                vm.eventbus.onclose = onEventBusClosed;
            }            
        };    

        vm.disConnectToServer = function() {
            if (vm.eventbus) {
                if (vm.consumer) {
                    vm.consumerUnRegistry();
                }
                vm.eventbus.close();
            }      
        };    

        vm.getGatewayConfig = function() {
            var cmd = {
                accountID: 1001001,
                address: "otocloud-gwm-gatewayglobalconfig",
                action: "GET",
                senderID: "1001001webbrowser",
                queryParams: {},
                content: {},
                session: {}
            };

            vm.eventbus.send(cmd.address, cmd, {}, function(err, message) {
                if (err === null) {
                    vm.result = JSON.stringify(message);
                }
                else {
                    vm.result = JSON.stringify(err);
                }
                $scope.$digest();
            });
        };

        vm.commandFailedTest = function() {
            var cmd = new Command(1001001, 
                                    "otocloud-gwm-gatewayglobalconfi",
                                    "GET");
            
            cmd.execute(vm.eventbus, function(result) {                
                vm.result = JSON.stringify(result);
                $scope.$digest();
            });
        };

        vm.commandSucceededTest = function() {
            var cmd = new Command(1001001, 
                                    "otocloud-gwm-gatewayglobalconfig",
                                    "GET");
            
            cmd.execute(vm.eventbus, function(result) {                
                vm.result = JSON.stringify(result);
                
                $scope.$digest();
            });
        };

        vm.consumerUnRegistry = function() {
            if (vm.consumer) {
                vm.consumer.unregistry();
                vm.consumer = null;
                vm.consumerrequestcontent = "";
                
            }
        };

        vm.consumerRegistry = function() {
            if (!vm.consumeraddress) {
                throw new Error("consumer address required");
            }
            if (vm.consumer) {
                vm.consumerUnRegistry();
            }
            else {
                vm.consumer = Command.consumer(vm.eventbus, vm.consumeraddress, function (command) {
                    vm.consumerrequestcontent = JSON.stringify(command);
                    $scope.$digest();
                    command.succeed(vm.eventbus, JSON.parse(vm.consumerreplycontent));
                });
            }
        };

        vm.sendtoconsumer = function() {
            if (!vm.consumeraddress) {
                throw new Error("consumer address required");
            }
            if (!vm.accountid) {
                throw new Err("accountid required");
            }
            var params = {};
            try {
                params = JSON.parse(vm.senderrequestparams);
            }
            catch (e) {
                params = {};
            }
            var content = new Array();
            try {
                content = JSON.parse(vm.senderrequestcontent);
                if (!(content instanceof Array) && content) {
                    content = new content(content);
                } 
                else if (!content) {
                    content = new Array();
                }
            }
            catch (e) {
                content = new Array();
            }
            var session = {};
            try {
                session = JSON.parse(vm.senderrequestsession);
            }
            catch (e) {
                session = {};
            }
            vm.consumerrespondcontent = "";
            var command = new Command(parseInt(vm.accountid), vm.consumeraddress, vm.action, params, content, session);
            command.execute(vm.eventbus, vm.canmultitimereply, function (result) {
                if (vm.canmultitimereply && result.succeeded()) {
                    vm.consumerrespondcontent += "sequenceID=" + result.sequenceID.toString() + " delta=" + result.delta.toString() + " progress=" + result.progress.toString() + " total=" + result.total.toString() + "getProgressPercent=" + result.getProgressPercent().toString() + "%\n";
                    if(result.isCompleted()) {
                        vm.consumerrespondcontent += "completed\n";
                    }
                }   
                else {
                    vm.consumerrespondcontent = JSON.stringify(result);
                }

                $scope.$digest();
            });
        };

        vm.sendtogateway = function() {
            if (!vm.consumeraddress) {
                throw new Error("consumer address required");
            }
            if (!vm.accountid) {
                throw new Err("accountid required");
            }
            var params = {};
            try {
                params = JSON.parse(vm.senderrequestparams);
            }
            catch (e) {
                params = {};
            }
            var content = new Array();
            try {
                content = JSON.parse(vm.senderrequestcontent);
                if (!(content instanceof Array) && content) {
                    content = new content(content);
                } 
                else if (!content) {
                    content = new Array();
                }
            }
            catch (e) {
                content = new Array();
            }
            var session = {};
            try {
                session = JSON.parse(vm.senderrequestsession);
            }
            catch (e) {
                session = {};
            }
            var command = new Command(parseInt(vm.accountid), vm.consumeraddress, vm.action, params, content, session);
            vm.consumerrespondcontent = "";
            command.executeOnGateway(vm.eventbus, vm.canmultitimereply, function (result) {
                if (vm.canmultitimereply && result.succeeded()) {
                    vm.consumerrespondcontent += "sequenceID=" + result.sequenceID.toString() + " delta=" + result.delta.toString() + " progress=" + result.progress.toString() + " total=" + result.total.toString() + "getProgressPercent=" + result.getProgressPercent().toString() + "%\n";
                    if(result.isCompleted()) {
                        vm.consumerrespondcontent += "completed\n";
                    }
                }  
                else {              
                    vm.consumerrespondcontent += JSON.stringify(result) + "\n";
                }

                $scope.$digest();
            });
        };

        function onEventBusConnected() {
            if (vm.eventbus.state === EventBus.OPEN) {
                if (vm.result !== 'EventBus connect succeeded') {
                    vm.result = "EventBus connect succeeded";
                    vm.eventbusReady = true;
                    $scope.$digest();
                }                
            }
        }

        function onEventBusClosed() {
            vm.result = "EventBus disconnected";
            delete vm.eventbus;
            vm.eventbus = null;
            vm.eventbusReady = false;
            $scope.$digest();
        }        
    }
})();
