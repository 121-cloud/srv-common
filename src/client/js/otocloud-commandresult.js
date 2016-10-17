!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    // CommonJS loader
    var Command = require('Command');
    if(!Command) {
      throw new Error('otocloud-commandresult.js requires Command, see http://dev.121cloud.com');
    }
    factory(Command);
  } 
  else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('otocloudcmdresult', ['otocloudcmd'], factory);
  } 
  else {
    // plain old include
    if (typeof this.Command === 'undefined') {
      throw new Error('otocloud-commandresult.js requires Command, see http://dev.121cloud.com');
    }
    CommandResult = factory(this.Command);
  }
}(function (Command) {

  /**
   * CommandResult
   *    <p>CommandResult是被调用方(Consumer)向调用方(Sender)返回的结果传输对象.</p> 
   *
   * @param {Command | CommandResult} command   
      <p>Command.createCommandResult()时传入Command对象</p>
      <p>Sender对象收到Consumer的回复消息时，传入收到的消息对象Message</p>
   * @constructor
   */
  var CommandResult = function(command) {
    self = this;

    if (command && (command instanceof Command)) {
      self.senderID = command.senderID;
      self.data = new Array();
      self.statusCode = 0;
      self.statusMessage = "";
      self.deliveryOptions = {
        sendTimeOut: 30000,
        codecName: "CommandResult"
      };
      self.replyOn = new Date();
      self.startOn = command.startOn;
      self.command = command; 
    }
    else if (command) {  //command is the Command Consumer Reply Result(message.body)
      for (var propertyName in command) {
        if (command.hasOwnProperty(propertyName)) {
          self[propertyName] = command[propertyName];
        }
      }
    }     
    else {
      throw new Error("command must be not null Command Object OR Message.body Object from remote server");
    }  
  };

  /**
   * CommandResult.fromMessage      从事件总线接收到Consumer回复的Message对象转换成CommandResult对象。
   *  这一般是Sender.callbak()的第一行代码，代码示例：
      <p>
        Command.execute(eventBus, function(message){
          CommandResult result = CommandResult.fromMessage(message);
          if (result.succeed()) {
            ......
          }
          else {
            ......
          }
          ......
        });
      </p>
   *
   * @param {Object} message  
   * @return {CommandResult}        
   */
  CommandResult.fromMessage = function(message) {
    result = message.body;
    if (result) {
      result = new CommandResult(result);
    }
    return result;
  }
    
  /////////////////////protetype.function define///////////////////////
  
  /**
   * addData      向CommandResult添加要返回的数据。
   *
   * @param {Object} data  json或者array，每次调用会把data push到CommandResult数据容器中。  
   * @return {CommandResult}        
   */  
  CommandResult.prototype.addData = function(data) {
    if (data) {
      if (data instanceof Array) {
        for (var i = 0; i < data.length; i++) {
          this.data.push(data[i]);    
        }
      }
      else {
        this.data.push(data);
      }
    }
    return this;
  };

  /**
   * succeed      向CommandResult添加要返回的数据，并置为成功状态。
   *
   * @param {Object} data  json或者array，每次调用会把data push到CommandResult数据容器中。  
   * @return {CommandResult}        
   */  
  CommandResult.prototype.succeed = function(data) {
    this.addData(data);
    this.statusCode = 0;
    return this;
  };

  /**
   * fail      向CommandResult添加要返回的错误代码和信息，并置为失败状态。
   *
   * @param {Integer} errcode  错误代码。 
   * @param {String}  errmessage    错误信息。  
   * @return {CommandResult}        
   */  
  CommandResult.prototype.fail = function (errcode, errmessage) {
    this.statusCode = errcode;
    this.statusMessage = errmessage;
    return this;
  };

  /**
   * succeeded      查询CommandResult是否是成功状态。
   *  
   * @return {boolean}        
   */ 
  CommandResult.prototype.succeeded = function() {
    return (this.statusCode === 0 || this.statusCode === 100);
  };

  /**
   * failed      查询CommandResult是否是失败状态。
   *  
   * @return {boolean}        
   */
  CommandResult.prototype.failed = function() {
    return !this.succeeded();
  };

  /**
   * reBaseProgress     重新调整进度的基准点。
                        这是要用于多次返回时接受的顺序与返回的顺序不一致时的处理。
                        使this.progress = reBasetolastProgress + this.data.
   *
   * @param {Integer} lastProgress  新的进度基点。 
   * @param {String}  errmessage    错误信息。  
   * @return {CommandResult}        
   */ 
  CommandResult.prototype.reBaseProgress = function(lastProgress) {
    var total = this.total;
    var delta = this.delta;
    this.progress = lastProgress + delta;
    if (this.progress === total) {
      this.statusCode = 0;
    }
    else {
      this.statusCode = 100;
    }
    return this;
  };

  /**
   * getProgressPercent      查询CommandResult 百分比进度状态 = progress * 100 / total。
   *  
   * @return {Integer}        
   */
  CommandResult.prototype.getProgressPercent = function() {
    var total = this.total;
    if (typeof total === 'undefined' || total === 0) {
      return 100;
    }
    var progress = this.progress;
    if (typeof progress === 'undefined') {
      return 0;
    }
    return (new Number((progress * 100) / total)).toFixed(0);
  };

  /**
   * isCompleted      查询可多次返回的命令是否结束。
   *  
   * @return {boolean}        
   */
  CommandResult.prototype.isCompleted = function() {
    return !(this.inProgress());
  };

  /**
   * isCompleted      查询可多次返回的命令是否还在继续。
   *  
   * @return {boolean}        
   */
  CommandResult.prototype.inProgress = function() {
    return this.statusCode === 100;
  };

  //---------------module exports define------------------
  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = CommandResult;
    } 
    else {
      exports.CommandResult = CommandResult;
    }
  } 
  else {
    return CommandResult;
  }
});