import org.jboss.netty.channel._
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import org.jboss.netty.channel.socket.nio.{NioServerSocketChannelFactory, NioClientSocketChannelFactory}
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

class TimeClientHandler extends SimpleChannelHandler {
  override def messageReceived(context: ChannelHandlerContext, e: MessageEvent) {
    val buf = e.getMessage.asInstanceOf[ChannelBuffer]
    val currentTime = buf.readInt * 1000L
    System.out.println(new Date(currentTime))
    e.getChannel.close()
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    e.getCause.printStackTrace()
    e.getChannel.close()
  }
}

object TimeClient {
  def main(args: Array[String]) {
    val host = args(0)
    val port = args(1).toInt

    val factory = new NioClientSocketChannelFactory(
      Executors.newCachedThreadPool,
      Executors.newCachedThreadPool)

    val bootstrap = new ClientBootstrap(factory)

    bootstrap.setPipelineFactory(new ChannelPipelineFactory {
      def getPipeline =
        Channels.pipeline(new TimeClientHandler)
    })

    bootstrap.setOption("child.tcpNoDelay", true)
    bootstrap.setOption("child.keepAlive", true)
    bootstrap.connect(new InetSocketAddress(host, port))
  }
}
