package net.zwj.netty.filedistribute;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.CharsetUtil;

import java.util.List;

public class FileTransCodec extends ByteToMessageCodec<FileRequest> {

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, FileRequest msg,
			ByteBuf out) throws Exception {
		out.writeBytes(msg.getCmd().getBytes(CharsetUtil.UTF_8));
		out.writeInt(msg.getCount());
		out.writeInt(msg.getOffset());
		out.writeInt(msg.getChunckSize());
		if (msg.getEntity() != null) {
			out.writeBytes(msg.getEntity());
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {

		if (in.readableBytes() == 0)
			return;
		ByteBuf buf = Unpooled.buffer(4);
		in.readBytes(buf);
		String cmd = buf.toString(CharsetUtil.UTF_8);
		buf.release();

		int count = in.readInt();
		int offset = in.readInt();
		int chunckSize = in.readInt();
		byte[] entity = null;
		if (in.readableBytes() > 0) {
			entity = new byte[in.readableBytes()];
			in.readBytes(entity);
		}
		FileRequest res = new FileRequest();
		res.setCmd(cmd);
		res.setCount(count);
		res.setOffset(offset);
		res.setChunckSize(chunckSize);
		res.setEntity(entity);
		out.add(res);
	}

}
