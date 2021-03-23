package ai.dragonfly.monotomni.native

import java.net.InetAddress

import ai.dragonfly.monotomni

/**
 * This variable determines the least significant 8 bits of every [[monotomni.MOI]] and [[monotomni.AMI]] id generated from this host.
 */
object HostName {
  lazy val hostName: String = InetAddress.getLocalHost.getHostName
}