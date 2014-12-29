package net.zwj.netty.filedistribute;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

public class FileClient {

	private void start(final String ipaddr, final String filedir,
			final String filename) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<Channel>() {

					@Override
					protected void initChannel(Channel ch) throws Exception {
						ch.pipeline().addLast(
								new LengthFieldPrepender(4),
								new LengthFieldBasedFrameDecoder(2048, 0, 4, 0,
										4), new FileTransCodec(),
								new FileClientHandler(filedir, filename));
					}
				});
		try {
			ChannelFuture f = bootstrap.connect(
					new InetSocketAddress(ipaddr, 9999)).sync();
			System.out.println("client connected!" + new Date());
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully().syncUninterruptibly();
		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException {

		if (args.length < 3) {
			System.out.println("params error!");
		} else {
			new FileClient().start(args[0], args[1], args[2]);

		}

	}
}
