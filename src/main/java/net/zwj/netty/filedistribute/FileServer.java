package net.zwj.netty.filedistribute;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

public class FileServer {

	private void start(final String filedir) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		EventLoopGroup child = new NioEventLoopGroup();
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(group, child).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<Channel>() {

					@Override
					protected void initChannel(Channel ch) throws Exception {
						ch.pipeline().addLast(
								new LengthFieldPrepender(4),
								new LengthFieldBasedFrameDecoder(2048, 0, 4, 0,
										4), new FileTransCodec(),
								new FileServerHandler(filedir));
					}
				});
		try {
			ChannelFuture f = bootstrap.bind(new InetSocketAddress(9999))
					.sync();
			System.out.println("server started!" + new Date());
			f.channel().closeFuture().sync();
		} finally {
			child.shutdownGracefully().syncUninterruptibly();
			group.shutdownGracefully().syncUninterruptibly();
		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException {

		if (args.length < 1) {
			System.out.println("params error!");
		} else {
			new FileServer().start(args[0]);
		}

	}
}
