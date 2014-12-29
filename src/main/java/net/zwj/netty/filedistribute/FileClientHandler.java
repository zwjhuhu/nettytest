package net.zwj.netty.filedistribute;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class FileClientHandler extends SimpleChannelInboundHandler<FileRequest> {

	private byte[] contents;

	private String filename;

	private String filedir;

	public FileClientHandler(String filedir, String filename) {
		this.filedir = filedir;
		this.filename = filename;
	}

	private FileRequest arrangeTransMsg(FileRequest msg) {
		msg.setCmd("tran");

		int offset = msg.getOffset();
		int length = msg.getChunckSize();

		byte[] buf = msg.getEntity();
		System.arraycopy(buf, 0, contents, offset, length);
		int count = msg.getCount() + 1;
		msg.setCount(count);
		msg.setOffset(offset + length);
		msg.setEntity(null);
		return msg;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		FileRequest msg = new FileRequest();
		msg.setCmd("init");
		msg.setCount(0);
		msg.setOffset(0);
		msg.setChunckSize(0);
		msg.setEntity(filename.getBytes("utf-8"));
		ctx.writeAndFlush(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FileRequest msg)
			throws Exception {
		if (msg.getCmd().equals("init")) {

			filename = new String(msg.getEntity(), "utf-8");
			System.out.println("server init filesize: " + msg.getCount()
					+ ", chunckSize: " + msg.getChunckSize() + ", filename: "
					+ filename);
			contents = new byte[msg.getCount()];

			msg.setCmd("tran");
			msg.setCount(1);
			msg.setOffset(0);
			msg.setEntity(null);
			ctx.writeAndFlush(msg);

		} else if (msg.getCmd().equals("tran")) {
			msg = arrangeTransMsg(msg);
			ctx.writeAndFlush(msg);
		} else if (msg.getCmd().equals("over")) {
			byte[] buf = msg.getEntity();
			System.arraycopy(buf, 0, contents, msg.getOffset(),
					msg.getChunckSize());
			ctx.channel().eventLoop().execute(new Runnable() {

				@Override
				public void run() {
					OutputStream out = null;
					try {
						out = new FileOutputStream(new File(filedir
								+ File.separator + UUID.randomUUID().toString()
								+ filename));
						out.write(contents);
						System.out.println("save file success！");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (out != null) {
							try {
								out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
			ctx.close();
		} else {
			System.out.println("error cmd! " + msg.getCmd());
		}

	}

}
