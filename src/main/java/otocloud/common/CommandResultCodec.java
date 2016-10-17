package otocloud.common;

import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class CommandResultCodec implements MessageCodec<CommandResult, CommandResult> {
	private static final Logger log = LoggerFactory.getLogger(CommandResultCodec.class.getName());
	
	@Override
	public void encodeToWire(Buffer buffer, CommandResult result) {
		String strJson = result.encode();
	    byte[] encoded = strJson.getBytes(CharsetUtil.UTF_8);
	    buffer.appendInt(encoded.length);
	    Buffer buff = Buffer.buffer(encoded);
	    buffer.appendBuffer(buff);
	}

	@Override
	public CommandResult decodeFromWire(int pos, Buffer buffer) {		
		int length = buffer.getInt(pos);
	    pos += 4;
	    byte[] encoded = buffer.getBytes(pos, pos + length);
	    String str = new String(encoded, CharsetUtil.UTF_8);
		return CommandResult.fromJson(str);
	}

	@Override
	public CommandResult transform(CommandResult s) {
		return CommandResult.fromJsonObject(s.copy());
	}

	@Override
	public String name() {		
		return "CommandResult";
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}
}
