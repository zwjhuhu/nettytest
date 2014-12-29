package net.zwj.netty.filedistribute;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

@Sharable
public class FileServerHandler extends SimpleChannelInboundHandler<FileRequest> {

	private String filedir;

	private ConcurrentHashMap<ChannelHandlerContext, byte[]> contMap = new ConcurrentHashMap<ChannelHandlerContext, byte[]>();

	public FileServerHandler(String filedir) {
		this.filedir = filedir;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	private void loadFile(File file, byte[] contents) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			in.read(contents);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FileRequest msg)
			throws Exception {
		if (msg.getCmd().equals("init")) {

			System.out.println("client init!");
			String filename = new String(msg.getEntity(), "utf-8");
			filename = filedir + File.separator + filename;
			File file = new File(filename);
			if (!file.isFile() || file.length() == 0) {
				msg.setCmd("error");
			} else {
				byte[] contents = new byte[(int) file.length()];
				contMap.put(ctx, contents);
				loadFile(file, contents);
				msg.setChunckSize(1024);
				msg.setCmd("init");
				msg.setCount(contents.length);
				msg.setOffset(0);
			}
			ctx.writeAndFlush(msg);
		} else if (msg.getCmd().equals("tran")) {
			byte[] contents = contMap.get(ctx);
			msg.setCmd("tran");
			int offset = msg.getOffset();
			int length = msg.getChunckSize();
			if (length + offset >= contents.length) {
				length = contents.length - offset;
				msg.setCmd("over");
			}
			byte[] buf = new byte[length];
			System.arraycopy(contents, offset, buf, 0, length);
			msg.setChunckSize(length);
			msg.setOffset(offset);
			msg.setEntity(buf);
			ctx.writeAndFlush(msg);
		} else if (msg.getCmd().equals("over")) {
			System.out.println("client over!");
			ctx.close();
		}

	}

}
