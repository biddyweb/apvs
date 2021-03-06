package ch.cern.atlas.apvs.ptu.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RemoveDelimiterDecoder extends ByteToMessageDecoder {

	public RemoveDelimiterDecoder() {
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {

		ByteBuf o = Unpooled.buffer();
		int len = in.readableBytes();
		for (int i = 0; i < len; i++) {
			byte b = in.readByte();
			if ((b != 0x10) && (b != 0x13) && (b != 0x00)) {
				o.writeByte(b);
			}
		}

		out.add(o);
	}
}
