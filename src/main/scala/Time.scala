import org.jboss.netty.channel._
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import org.jboss.netty.channel.socket.nio.{NioServerSocketChannelFactory, NioClientSocketChannelFactory}
import org.jboss.netty.handler.codec.replay.{ReplayingDecoder, VoidEnum}
import org.jboss.netty.bootstrap.{ServerBootstrap, ClientBootstrap}
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.Date

class TimeServerHandler extends SimpleChannelHandler {
  override def channelConnected(context: ChannelHandlerContext, e: ChannelStateEvent) {
    val ch = e.getChannel
    val time = ChannelBuffers.buffer(4)
    time.writeInt((System.currentTimeMillis / 1000).toInt)
    val f = ch.write(time)
    f.addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) {
        future.getChannel.close()
      }
    })
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    e.getCause.printStackTrace()
    e.getChannel.close()
  }
}

object TimeServer {
  def main(args: Array[String]) {
    val factory = new NioServerSocketChannelFactory(
      Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool())

    val bootstrap = new ServerBootstrap(factory)

    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      def getPipeline =
        Channels.pipeline(new TimeServerHandler)
    })

    bootstrap.setOption("child.tcpNoDelay", true)
    bootstrap.setOption("child.keepAlive", true)
    bootstrap.bind(new InetSocketAddress(8080))
  }
}

class TimeDecoder extends ReplayingDecoder[VoidEnum] {
  def decode(context: ChannelHandlerContext, channel: Channel, buffer: ChannelBuffer, state: VoidEnum) =
    buffer.readBytes(4)
}

class TimeClientHandler extends SimpleChannelHandler {
  private val buf = ChannelBuffers.dynamicBuffer()

  override def messageReceived(context: ChannelHandlerContext, e: MessageEvent) {
    val m = e.getMessage.asInstanceOf[ChannelBuffer]
    buf.writeBytes(m)

    if (buf.readableBytes >= 4) {
      val currentTime = buf.readInt * 1000L
      System.out.println(new Date(currentTime))
      e.getChannel.close()
    }
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    e.getCause.printStackTrace()
    e.getChannel.close()
  }
}

object TimeClient {
  def main(args: Array[String]) {
    val factory = new NioClientSocketChannelFactory(
      Executors.newCachedThreadPool,
      Executors.newCachedThreadPool)

    val bootstrap = new ClientBootstrap(factory)

    bootstrap.setPipelineFactory(new ChannelPipelineFactory {
      def getPipeline =
        Channels.pipeline(
          new TimeDecoder,
          new TimeClientHandler)
    })

    bootstrap.setOption("child.tcpNoDelay", true)
    bootstrap.setOption("child.keepAlive", true)
    bootstrap.connect(new InetSocketAddress("localhost", 8080))
  }
}
