package ai.dragonfly.monotomni.native

import java.net.InetAddress

object HostName {
  lazy val hostName: String = InetAddress.getLocalHost.getHostName
}