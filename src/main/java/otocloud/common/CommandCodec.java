package otocloud.common;

import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class CommandCodec implements MessageCodec<Command, Command> {
	private static final Logger log = LoggerFactory.getLogger(CommandCodec.class.getName());
	
	@Override
	public void encodeToWire(Buffer buffer, Command command) {
		String strJson = command.encode();
	    byte[] encoded = strJson.getBytes(CharsetUtil.UTF_8);
	    buffer.appendInt(encoded.length);
	    Buffer buff = Buffer.buffer(encoded);
	    buffer.appendBuffer(buff);
	}

	@Override
	public Command decodeFromWire(int pos, Buffer buffer) {		
		int length = buffer.getInt(pos);
	    pos += 4;
	    byte[] encoded = buffer.getBytes(pos, pos + length);
	    String str = new String(encoded, CharsetUtil.UTF_8);
		return Command.fromJson(str);
	}

	@Override
	public Command transform(Command s) {
		return Command.fromJsonObject(s.copy());
	}

	@Override
	public String name() {		
		return "Command";
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

}
