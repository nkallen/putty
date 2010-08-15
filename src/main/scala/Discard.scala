import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.bootstrap.ServerBootstrap
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class DiscardServerHandler extends SimpleChannelHandler {
  override def messageReceived(context: ChannelHandlerContext, e: MessageEvent) {}
  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    e.getCause.printStackTrace()
    e.getChannel.close()
  }
}

object DiscardServer {
  def main(args: Array[String]) {
    val factory = new NioServerSocketChannelFactory(
      Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool())

    val bootstrap = new ServerBootstrap(factory)

    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      def getPipeline =
        Channels.pipeline(new DiscardServerHandler)
    })

    bootstrap.setOption("child.tcpNoDelay", true)
    bootstrap.setOption("child.keepAlive", true)
    bootstrap.bind(new InetSocketAddress(8080))
  }
}
