!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    // CommonJS loader
    var EventBus = require('EventBus');
    if(!EventBus) {
      throw new Error('otocloud-command.js requires EventBus, see http://vertx.io');
    }
    factory(EventBus);
  } 
  else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('otocloudcmd', ['vertx3bus'], factory);
  } 
  else {
    // plain old include
    if (typeof this.EventBus === 'undefined') {
      throw new Error('otocloud-command.js requires EventBus, see http://vertx.io');
    }
    Command = factory(this.EventBus);
  }
}(function (EventBus) {

  /**
   * Command
   *    <p>Command是121 Cloud应用接口之间调用的Command模式封装.</p>
   *    <p>Command是调用方(Sender)与被调用方(Consumer)之间传递命令和信息的载体.</p> 
   *
   * @param {Integer} accountID   企业帐户ID
   * @param {String} address      目标地址
   * @param {String} action       行为 | 方法，如 GET/SET
   * @param {Object} params       命令参数
   * @param {Object | Array} content      命令要传输的内容
   * @param {Object} session      用户会话上下文对象
   * @constructor
   */
  var Command = function(accountID, address, action, params, content, session) {
    var self = this;

    self.accountID = accountID;
    self.address = address;
    self.action = action || "";
    self.queryParams = params || {};
    if (content instanceof Array) {
      self.content = content;  
    }
    else if(content) {
      self.content = new Array(content);
    }
    else {
      self.content = new Array();
    }
    self.session = session || {};
    self.senderID = makeUUID();    
  };

  /**
   * Command.consumer   在指定的地址上注册命令消费者（等待接受命令请求）
   *
   * @param {EventBus} eventBus   Vertx事件总线
   * @param {String} address      地址
   * @param {Function} callback   命令响应Handler，function({Command}){...}
   * @return {Object} consumer    
   *     <p>consumer.eventBus</p>
   *     <p>consumer.address</p>
   *     <p>consumer.handler</p>
   *     <p>consumer.unregistry() 从事件总线上注销该consumer</p>
   */
  Command.consumer = function(eventBus, address, callback) {
    assertEventBusStateOPEN(eventBus);

    eventBus.registerHandler(address, handler);
    var consumer = {eventBus: eventBus, address: address, handler: handler};
    consumer.unregistry = function() {
      this.eventBus.unregisterHandler(this.address, this.handler);
    };

    return consumer;

    function handler(err, message) {      
      callback(Command.fromMessage(message));
    }    
  };

  /**
   * Command.fromMessage      从事件总线接收到的命令请求的Message对象转换成Command对象。
   *  这一般是Consumer.handler()接受命令的第一行代码，代码示例：
      <p>
        Command.consumer(eventBus, someaddress, function(message){
          Command cmd = Command.fromMessage(message);
          ......
        });
      </p>
   *
   * @param {Object} message  
   * @return {Command}        
   */
  Command.fromMessage = function(message) {
    msgbody = message.body;
    var cmd = new Command(msgbody.accountID, msgbody.address, msgbody.action, 
                              msgbody.queryParams, msgbody.content, msgbody.session);
    cmd.senderID = msgbody.senderID;
    cmd.deliveryOptions = msgbody.deliveryOptions;
    cmd.multiTimeReply = (typeof msgbody.multiTimeReply === 'undefined') ? false : msgbody.multiTimeReply;
    if (cmd.multiTimeReply) {
      if (message.replyAddress) message.reply("received");      
    }
    else {
      cmd.replyAddress = message.replyAddress;
    }

    return cmd;
  };

  /**
   * createCommandResult      创建与该命令绑定的回复对象。
   *  这一般是Consumer.handler()接受命令的代码中使用，代码示例：
      <p>
        Command.consumer(eventBus, someaddress, function(message){
          Command cmd = Command.fromMessage(message);
          ......
          CommandResult result = cmd.createCommandResult();
          ......
          cmd.reply(eventBus, result);
        });
      </p>
   *  
   * @return {CommandResult}
   */
  Command.prototype.createCommandResult = function() {
    return new CommandResult(this);
  };

  /**
   * execute      向Command.address发送命令请求。
   *  
   * @param {EventBus}  eventBus Vertx.EventBus
   * @param {boolean}   multiTimeReply  允许多次返回结果，主要用于有进度更新的长时间任务
   * @param {Function}  callback  function(CommandResult){}  
   */
  Command.prototype.execute = function(eventBus, multiTimeReply, callback) {
    executeCommand(eventBus, this.address, this, multiTimeReply, callback);
  };

  /**
   * executeOnGateway      穿过Command.accountID的Gateway向Command.address发送命令请求。
   *  
   * @param {EventBus}  eventBus Vertx.EventBus
   * @param {boolean}   multiTimeReply  允许多次返回结果，主要用于有进度更新的长时间任务
   * @param {Function}  callback  function(CommandResult){}  
   */
  Command.prototype.executeOnGateway = function(eventBus, multiTimeReply, callback) {
    var gatewayAddress = Command.GATEWAY_ADDRESS_PREFIX + this.accountID.toString();
    executeCommand(eventBus, gatewayAddress, this, multiTimeReply, callback);
  };

  /**
   * succeed      向命令请求者(Sender)回复成功消息。
   *  
   * @param {EventBus}  eventBus Vertx.EventBus
   * @param {Object}    data  需要返回的数据，Command把data包装到CommandResult.data中，
                              并把CommandResult状态置为成功。  
   */
  Command.prototype.succeed = function(eventBus, data) {
    assertEventBusStateOPEN(eventBus);

    var result = this.createCommandResult();
    result.succeed(data);

    this.reply(eventBus, result); 
  };

  /**
   * fail      向命令请求者(Sender)回复失败消息。
   *  
   * @param {EventBus}  eventBus Vertx.EventBus
   * @param {Integer}   errCode  错误代码
   * @param {String}    errMessage  错误信息 
   */
  Command.prototype.fail = function(eventBus, errCode, errMessage) {
    assertEventBusStateOPEN(eventBus);

    var result = this.createCommandResult();
    result.fail(errCode, errMessage);

    this.reply(eventBus, result);
  };

  /**
   * reply      向命令请求者(Sender)回复消息。
   *  
   * @param {EventBus}  eventBus Vertx.EventBus
   * @param {CommandResult}   result  命令结果对象，包括数据和状态，一般用法为：
      <p>
        Command.consumer(eventBus, someaddress, function(message){
          Command cmd = Command.fromMessage(message);
          ......
          CommandResult result = cmd.createCommandResult();
          ......
          cmd.reply(eventBus, result);
        });
      </p>
   *  
   */
  Command.prototype.reply = function(eventBus, result) {
    assertEventBusStateOPEN(eventBus);
    if (this.replyAddress) {
      eventBus.send(this.replyAddress, result);
    }
    else {
      Console.log("the Command replyAddress is null, so system do nothing!");
    }
  };

  function executeCommand(eventBus, address, command, multiTimeReply, callback) {
    assertEventBusStateOPEN(eventBus);
    
    if (typeof multiTimeReply === 'function') {
      callback = multiTimeReply;
      multiTimeReply = false;
    }

    var that = command;
    if (multiTimeReply) {
      that.multiTimeReply = multiTimeReply;
    }
    that.startOn = new Date();

    if (multiTimeReply) {
      executeCommandWithMultiReply(eventBus, address, that, callback);
    }
    else {
      executeCommandWithSingleReply(eventBus, address, that, callback);
    }
  }

  function executeCommandWithSingleReply(eventBus, address, command, callback) {
    var that = command;
    if (callback) {
      eventBus.send(address, that, null, function(err, message) { 
        //socket communicate error
        if (err) {
          var result = that.createCommandResult();
          result.fail(err.failureCode, err.message);
          callback(result);
        }
        else {
          var result = CommandResult.fromMessage(message);
          //socket commnunicate right,but the application program environment error
          if (!result) {
            var result = that.createCommandResult();
            result.fail(message.failureCode, message.message);
          }
          callback(result);
        }
      });
    }
    else {
      eventBus.send(address, that);
    }
  }

  function executeCommandWithMultiReply(eventBus, address, command, callback) {
    var that = command;
    var receivedProgress = 0;
    var receiveBuffer = new Array();
    var currentReceiveSequenceID = 0;

    if (callback) {
      var replyAddress = makeUUID();
      that.replyAddress = replyAddress;

      eventBus.registerHandler(replyAddress, replyHandler);
    }

    eventBus.send(address, that, function(err, message) {
      var result = null;
      if (err) {
        result = that.createCommandResult();
        result.fail(err.failureCode, err.message);        
      }
      else if (!message.body) {
        //socket commnunicate right,but the application program environment error
        result = that.createCommandResult();
        result.fail(message.failureCode, message.message);        
      }
      if (result) {
        result.sequenceID = currentReceiveSequenceID;
        result.delta = 1;
        result.progress = receivedProgress + 1;
        result.total = result.progress;
        message.body = result;
        
        replyHandler(null, message);
      }
    });

    function replyHandler(err, message) {
      var result = CommandResult.fromMessage(message);
      receiveBuffer.push(result);
      
      receiveBuffer.sort(function (a, b){
        return a.sequenceID > b.sequenceID ? 1 : -1;
      });
      
      var lastSendedResult = null;
      while(receiveBuffer.length >0 && receiveBuffer[0].sequenceID === currentReceiveSequenceID) {
        lastSendedResult = receiveBuffer.shift();
        if (lastSendedResult.progress !== receivedProgress + lastSendedResult.delta) {
          lastSendedResult.reBaseProgress(receivedProgress);
        }
        receivedProgress = lastSendedResult.progress;

        callback(lastSendedResult);

        currentReceiveSequenceID += 1;        
      }
      if (lastSendedResult) {
        if (lastSendedResult.isCompleted()) {
          eventBus.unregisterHandler(address, replyHandler);
        }
      }
    }
  }

  function assertEventBusStateOPEN(eventBus) {
    if (!eventBus) {
      throw new Error('eventBus arg require not null');
    }
    if (eventBus.state != EventBus.OPEN) {
      throw new Error('eventBus must be succeeded connect to server and state is open');
    }
  }

  function makeUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (a, b) {
      return b = Math.random() * 16, (a == 'y' ? b & 3 | 8 : b | 0).toString(16);
    });
  }
  
  Command.GATEWAY_ADDRESS_PREFIX = "otocloud-gw-";

  //--------------module exports define----------------//
  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = Command;
    } 
    else {
      exports.Command = Command;
    }
  } 
  else {
    return Command;
  }
});