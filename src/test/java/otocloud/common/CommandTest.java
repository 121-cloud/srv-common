package otocloud.common;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class CommandTest {
	private static final Logger log = LoggerFactory.getLogger(CommandTest.class.getName());
	
	private Vertx vertx;
	
	@Before
	public void setup(){
		VertxOptions op = new VertxOptions();
    	op.setBlockedThreadCheckInterval(50000000);
    	vertx = Vertx.vertx(op);   
    	vertx.eventBus().registerDefaultCodec(Command.class, new CommandCodec());
    	vertx.eventBus().registerDefaultCodec(CommandResult.class, new CommandResultCodec());
	}
	
	@Test
    public void testCommandSucceed() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			assertTrue(cmd.getParams().getString("userid").equals("2"));
			assertTrue(cmd.getParams().getString("psw").equals("**"));
			assertTrue(cmd.getAbsURI().equals("http://localhost:8080/api/vendors/1?userid=2&psw=**"));
			assertTrue(cmd.getURI().equals("/api/vendors/1?userid=2&psw=**"));
			assertTrue(cmd.getPath().equals("/api/vendors/1"));
			assertTrue(cmd.getContent().getString("key").equals("1"));
			assertTrue(cmd.getContent(1).getString("key").equals("2"));
			assertTrue(cmd.getRestAPIDef().getString("uri").equals("api/vendors/:id"));
			assertTrue(cmd.getRestAPIDef().getString("method").equals("GET"));
			System.out.println("Received Command:" + cmd.toString());
			
			cmd.succeed(vertx, new JsonObject().put("result", "ok"));
			latch.countDown();					
		});
		
		//Command Sender
		Command command = new Command(1001, "otocloud-common-testcommand1");
		command.setRequestURI("http://localhost:8080/api/vendors/1?userid=2&psw=**");
		command.addParam("id", "1")
		       .addParam("userid", "2")
		       .addParam("psw","**");	
		command.addContent(new JsonObject().put("key", "1"));
		command.addContent(new JsonObject().put("key", "2"));		
		command.setRestAPIDef(new JsonObject().put("uri", "api/vendors/:id").put("method", "GET"));
			
		command.execute(vertx, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.succeeded());
			System.out.println("duration:" + String.valueOf(rep.duration()) + "ms");
			System.out.println("Returned CommandResult:" + rep.encode());
			
			assertTrue(command.getSenderID().equals(rep.getSenderID()));
			assertTrue(rep.getData().getString("result").equals("ok"));
			assertTrue(rep.getDatas().getJsonObject(0).getString("result").equals("ok"));
			assertTrue(rep.getData(0).getString("result").equals("ok"));
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCommandFail() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {
			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			
			cmd.fail(vertx, 500, "The object not be founded");
			
			latch.countDown();					
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
				
		command.execute(vertx, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.failed());
			assertTrue(rep.getStatusCode() == 500);
			assertTrue(rep.getStatusMessage().indexOf("not be found") > 0);
			if (log.isDebugEnabled()) {
				assertTrue(!rep.containsKey(CommandScheme.RESULT_STACK_TRACE));	
				log.error(rep.getStackTrace());
			}
						
			System.out.println(rep.toString());
			
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCommandFailThrowable() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {
			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			
			cmd.fail(vertx, new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 500, "The object not be founded"));
			
			latch.countDown();					
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
				
		command.execute(vertx, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.failed());
			assertTrue(rep.getStatusCode() == 500);
			assertTrue(rep.getStatusMessage().indexOf("not be found") > 0);
//			if (log.isDebugEnabled()) {
//				assertTrue(rep.containsKey(CommandScheme.RESULT_STACK_TRACE));	
//				log.error(rep.getStackTrace());
//			}			
			System.out.println(rep.toString());
			
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCommandProgress() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));			
			vertx.setTimer(1000, timer1 -> {	
				JsonObject databatch1 = new JsonObject();
				databatch1.put("orderid", "001");
				databatch1.put("ordernum", 100);				
				cmd.updateProgress(vertx, 4, 1, databatch1, "first batch data");				
				vertx.setTimer(1000, timer2 -> {
					JsonObject databatch2 = new JsonObject();
					databatch2.put("orderid", "002");
					databatch2.put("ordernum", 200);				
					cmd.updateProgress(vertx, 4, 1, databatch2, "second batch data");
				});
				vertx.setTimer(2000, timer3 ->{
					JsonArray databatch2 = new JsonArray();					
					JsonObject dataitem = new JsonObject();
					dataitem.put("orderid", "003");
					dataitem.put("ordernum", 300);
					databatch2.add(dataitem);
					dataitem = new JsonObject();
					dataitem.put("orderid", "004");
					dataitem.put("ordernum", 400);
					databatch2.add(dataitem);
					cmd.updateProgress(vertx, 4, 2, databatch2, "second batch data");
					latch.countDown();
				});				
			});			
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
		CommandDeliveryOptions options = new CommandDeliveryOptions();
		options.setSendTimeout(-1);
				
		command.execute(vertx, options, true, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.succeeded());
			System.out.println("progress = " + rep.getProgressPercent() + "%");
			System.out.println("progress message:" + rep.getStatusMessage());
			System.out.println("data=" + rep.getDatas().toString());
			if (rep.isComplete()) {				
				assertTrue(rep.getProgressPercent() == 100);
				assertTrue(rep.getData(0).getString("orderid") == "003");
				assertTrue(rep.getData(1).getString("orderid") == "004");
				latch.countDown();
			}
			else {
				if (rep.getInteger(CommandScheme.PROGRESS) == 50) {
					assertTrue(rep.getData().getString("orderid") == "002");
				}				
			}
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCommandProgressWithFail() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));			
			vertx.setTimer(1000, timer1 -> {	
				JsonObject databatch1 = new JsonObject();
				databatch1.put("orderid", "001");
				databatch1.put("ordernum", 100);				
				cmd.updateProgress(vertx, 4, 1, databatch1, "first batch data");				
				vertx.setTimer(1000, timer2 -> {
					JsonObject databatch2 = new JsonObject();
					databatch2.put("orderid", "002");
					databatch2.put("ordernum", 200);				
					cmd.fail(vertx, "terminate send data");
				});
				vertx.setTimer(2000, timer3 ->{
					JsonArray databatch2 = new JsonArray();					
					JsonObject dataitem = new JsonObject();
					dataitem.put("orderid", "003");
					dataitem.put("ordernum", 300);
					databatch2.add(dataitem);
					dataitem = new JsonObject();
					dataitem.put("orderid", "004");
					dataitem.put("ordernum", 400);
					databatch2.add(dataitem);
					cmd.updateProgress(vertx, 4, 2, databatch2, "second batch data");					
				});				
			});	
			latch.countDown();
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
		CommandDeliveryOptions options = new CommandDeliveryOptions();
		options.setSendTimeout(-1);
				
		command.execute(vertx, options, true, rep -> {
			assertTrue(rep instanceof CommandResult);
			
			if (rep.isComplete()) {	
				assertTrue(rep.failed());
				assertTrue(rep.getProgressPercent() == 100);
				assertTrue(rep.getStatusMessage().equals("terminate send data"));
				System.out.println(rep.getStatusMessage());
				
				latch.countDown();
			}
			else {
				assertTrue(rep.succeeded());
				System.out.println("progress = " + rep.getProgressPercent() + "%");
				System.out.println("progress message:" + rep.getStatusMessage());
				System.out.println("data=" + rep.getDatas().toString());								
			}
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCommandProgressWithSystemExceptionFail() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);		
    	
		//Command Receiver
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));			
			vertx.setTimer(1000, timer1 -> {	
				JsonObject databatch1 = new JsonObject();
				databatch1.put("orderid", "001");
				databatch1.put("ordernum", 100);				
				cmd.updateProgress(vertx, 4, 1, databatch1, "first batch data");				
				vertx.setTimer(1000, timer2 -> {
					JsonObject databatch2 = new JsonObject();
					databatch2.put("orderid", "002");
					databatch2.put("ordernum", 200);				
					cmd.fail(vertx, "terminate send data");
				});
				vertx.setTimer(2000, timer3 ->{
					JsonArray databatch2 = new JsonArray();					
					JsonObject dataitem = new JsonObject();
					dataitem.put("orderid", "003");
					dataitem.put("ordernum", 300);
					databatch2.add(dataitem);
					dataitem = new JsonObject();
					dataitem.put("orderid", "004");
					dataitem.put("ordernum", 400);
					databatch2.add(dataitem);
					cmd.updateProgress(vertx, 4, 2, databatch2, "second batch data");					
				});				
			});	
			latch.countDown();
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand");
		CommandDeliveryOptions options = new CommandDeliveryOptions();
		options.setSendTimeout(-1);
				
		command.execute(vertx, options, true, rep -> {
			assertTrue(rep instanceof CommandResult);
			
			if (rep.isComplete()) {	
				assertTrue(rep.failed());
				assertTrue(rep.getProgressPercent() == 100);
				//assertTrue(rep.getStatusMessage().equals("terminate send data"));
				System.out.println(rep.getStatusMessage());
				
				latch.countDown();
			}
			else {
				assertTrue(rep.succeeded());
				System.out.println("progress = " + rep.getProgressPercent() + "%");
				System.out.println("progress message:" + rep.getStatusMessage());
				System.out.println("data=" + rep.getDatas().toString());								
			}
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCommandProgressWithSingleReply() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));			
			
			JsonArray datas = new JsonArray();			
			JsonObject dataitem = new JsonObject();
			
			dataitem.put("orderid", "001");
			dataitem.put("ordernum", 100);			
			datas.add(dataitem);
			
			dataitem = new JsonObject();
			dataitem.put("orderid", "002");
			dataitem.put("ordernum", 200);
			datas.add(dataitem);
								
			dataitem = new JsonObject();
			dataitem.put("orderid", "003");
			dataitem.put("ordernum", 300);
			datas.add(dataitem);
			
			dataitem = new JsonObject();
			dataitem.put("orderid", "004");
			dataitem.put("ordernum", 400);
			datas.add(dataitem);			
			
			cmd.updateProgress(vertx, 0, 0, datas, "");
			
//			CommandResult result = cmd.createResultObject();
//			result.addData(datas);			
//			cmd.reply(vertx, result);
			
			latch.countDown();
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
		CommandDeliveryOptions options = new CommandDeliveryOptions();
		options.setSendTimeout(-1);
				
		command.execute(vertx, options, true, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.succeeded());
			assertTrue(rep.isComplete());
			System.out.println("progress = " + rep.getProgressPercent() + "%");
			System.out.println("progress message:" + rep.getStatusMessage());
			System.out.println("data=" + rep.getDatas().toString());
							
			assertTrue(rep.getProgressPercent() == 100);
			assertTrue(rep.getData(0).getString("orderid") == "001");
			assertTrue(rep.getData(2).getString("orderid") == "003");
			latch.countDown();
			
		});
		
    	assertTrue(latch.await(1000, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCommandProgressSequenceControl() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));			
			vertx.setTimer(1000, timer1 -> {	
				JsonObject databatch1 = new JsonObject();
				databatch1.put("orderid", "001");
				databatch1.put("ordernum", 100);				
				cmd.updateProgress(vertx, 4, 1, databatch1, "first batch data", 2);				
				vertx.setTimer(1000, timer2 -> {
					JsonObject databatch2 = new JsonObject();
					databatch2.put("orderid", "002");
					databatch2.put("ordernum", 200);				
					cmd.updateProgress(vertx, 4, 1, databatch2, "second batch data", 0);
				});
				vertx.setTimer(2000, timer3 ->{
					JsonArray databatch2 = new JsonArray();					
					JsonObject dataitem = new JsonObject();
					dataitem.put("orderid", "003");
					dataitem.put("ordernum", 300);
					databatch2.add(dataitem);
					dataitem = new JsonObject();
					dataitem.put("orderid", "004");
					dataitem.put("ordernum", 400);
					databatch2.add(dataitem);
					cmd.updateProgress(vertx, 4, 2, databatch2, "last batch data", 1);
					latch.countDown();
				});				
			});			
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
		CommandDeliveryOptions options = new CommandDeliveryOptions();
		options.setSendTimeout(-1);
				
		command.execute(vertx, options, true, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.succeeded());
			System.out.println("progress = " + rep.getProgressPercent() + "%");
			System.out.println("progress message:" + rep.getStatusMessage());
			System.out.println("data=" + rep.getDatas().toString());
			if (rep.isComplete()) {				
				assertTrue(rep.getProgressPercent() == 100);
				assertTrue(rep.getData(0).getString("orderid") == "001");
				latch.countDown();
			}
			else if (rep.getSequenceID() == 1){
				if (rep.getInteger(CommandScheme.PROGRESS) == 50) {
					assertTrue(rep.getData().getString("orderid") == "003");
				}				
			}
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCommandDeliveryOptions() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			vertx.setTimer(5000, timer -> {				
				cmd.succeed(vertx);
				latch.countDown();
			});			
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
		CommandDeliveryOptions options = new CommandDeliveryOptions();
		options.setSendTimeout(4000);
				
		command.execute(vertx, options, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.failed());
			assertTrue(rep.getStatusCode() == -1);
			assertTrue(rep.getStatusMessage().indexOf("out") > 0);
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}	
	
	@Test
    public void testCommandNoneTimeout() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			vertx.setTimer(5000, timer -> {				
				cmd.succeed(vertx);
				latch.countDown();
			});			
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
		CommandDeliveryOptions options = new CommandDeliveryOptions();
		options.setSendTimeout(-1);
				
		command.execute(vertx, options, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.succeeded());			
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCommandProgressWithTimeout() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));

			cmd.updateProgress(vertx, 3, 1, "first reply!");
			vertx.setTimer(3000, t -> {
				cmd.updateProgress(vertx, 3, 1, "second reply!");
			});
			
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
		CommandDeliveryOptions options = new CommandDeliveryOptions();
		options.setSendTimeout(2000);
				
		command.execute(vertx, options, true, rep -> {
			assertTrue(rep instanceof CommandResult);
			if(rep.succeeded()) {
				if (rep.getSequenceID() == 0) {				
					assertTrue(rep.getStatusCode() == 100);
					assertTrue(rep.getStatusMessage().equals("first reply!"));
				}
				else if(rep.getSequenceID() == 1) {
					assertTrue(rep.getStatusCode() == 100);
					assertTrue(rep.getStatusMessage().equals("second reply!"));
				}
			}
			else {
				assertTrue(rep.failed());
				assertTrue(rep.getSequenceID() == 1);
				System.out.println(rep.getStatusMessage());
				assertTrue(rep.getStatusMessage().equals("Timed out waiting for reply"));
			}
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCompatiableStyle1() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {
			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			
			cmd.succeed(vertx, new JsonObject().put("result", "ok"));
			
			latch.countDown();					
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
				
		vertx.eventBus().send(command.getCommandAddress(), command, rep -> {
			assertTrue(rep.result().body() instanceof CommandResult);
			CommandResult result = CommandResult.fromMessageBody(rep.result().body());
			assertTrue(result.succeeded());
			assertTrue(result.getStatusCode() == 0);
			assertTrue(result.getData().getString("result").equals("ok"));
			
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCompatiableStyle2() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		
		Command.consumer(vertx, "otocloud-common-testcommand1", cmd -> {
			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			
			cmd.succeed(vertx, new JsonObject().put("result", "ok"));
			
			latch.countDown();					
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
				
		vertx.eventBus().<CommandResult>send(command.getCommandAddress(), command, rep -> {
			assertTrue(rep.result().body() instanceof CommandResult);
			CommandResult result = rep.result().body();
			assertTrue(result.succeeded());
			assertTrue(result.getStatusCode() == 0);
			assertTrue(result.getData().getString("result").equals("ok"));
			
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCompatiableStyle3() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		vertx.eventBus().consumer("otocloud-common-testcommand1", msg -> {
			Command cmd = Command.fromMessageBody(msg.body());
			cmd.setMessageCarrier(msg);
			
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			
			cmd.succeed(vertx, new JsonObject().put("result", "ok"));
			
			latch.countDown();					
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
				
		command.execute(vertx, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.succeeded());
			assertTrue(rep.getStatusCode() == 0);
			assertTrue(rep.getData().getString("result").equals("ok"));
			
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCompatiableStyle4() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		vertx.eventBus().consumer("otocloud-common-testcommand1", msg -> {
			Command cmd = Command.fromMessageBody(msg.body());
						
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			
			CommandResult result = cmd.createResultObject();
			result.succeed(new JsonObject().put("result", "ok"));
			
			msg.reply(result);
			latch.countDown();					
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
				
		command.execute(vertx, rep -> {
			assertTrue(rep instanceof CommandResult);
			assertTrue(rep.succeeded());
			assertTrue(rep.getStatusCode() == 0);
			assertTrue(rep.getData().getString("result").equals("ok"));
			
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testCompatiableStyle5() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		vertx.eventBus().consumer("otocloud-common-testcommand1", msg -> {
			Command cmd = Command.fromMessageBody(msg.body());
						
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			
			CommandResult result = cmd.createResultObject();
			result.succeed(new JsonObject().put("result", "ok"));
			
			msg.reply(result);
			latch.countDown();					
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
				
		vertx.eventBus().send(command.getCommandAddress(), command, rep -> {
			assertTrue(rep.result().body() instanceof JsonObject);
			CommandResult result = CommandResult.fromMessageBody(rep.result().body());
			assertTrue(result.succeeded());
			assertTrue(result.getStatusCode() == 0);
			assertTrue(result.getData().getString("result").equals("ok"));
			
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@Test
    public void testMessageReply() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);		
    	
		//Command Receiver
		vertx.eventBus().consumer("otocloud-common-testcommand1", msg -> {
			Command cmd = Command.fromMessageBody(msg.body());
						
			assertTrue(cmd.isValid());
			assertTrue(cmd.getAccountID() == 1001);
			assertTrue(cmd.getGatewayAddress().equals("otocloud-gw-1001"));
			
			CommandResult result = cmd.createResultObject();
			result.succeed(new JsonObject().put("result", "ok"));
			
			//msg.reply(result);
			vertx.eventBus().send(msg.replyAddress(), result);
			latch.countDown();					
		});
		
		//Command Sender
		Command command = new Command(1001,"otocloud-common-testcommand1");
				
		vertx.eventBus().send(command.getCommandAddress(), command, rep -> {
			assertTrue(rep.result().body() instanceof JsonObject);
			CommandResult result = CommandResult.fromMessageBody(rep.result().body());
			System.out.println(result.toString());
			assertTrue(result.succeeded());
			assertTrue(result.getStatusCode() == 0);
			assertTrue(result.getData().getString("result").equals("ok"));
			
			try {
				latch.await(10, TimeUnit.SECONDS);
			}
			catch(Exception e) {
				
			}
			latch.countDown();
		});
		
    	assertTrue(latch.await(100, TimeUnit.SECONDS));
	}
	
	@After
	public void teardown() {
		if (vertx != null) {
			vertx.eventBus().unregisterDefaultCodec(Command.class);
			vertx.eventBus().unregisterDefaultCodec(CommandResult.class);
			vertx.close();
		}
	}
}
