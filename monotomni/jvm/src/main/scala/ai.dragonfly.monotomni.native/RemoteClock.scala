package ai.dragonfly.monotomni.native

import ai.dragonfly.monotomni
import monotomni.{AMI, MOI, Mono, Omni, Clock}

/**
 * See [[monotomni.RemoteClock]] for relevant JVM implementations.
 */

trait RemoteClock extends Clock {
  def ready(callback: () => Unit):Unit
  def ami(moi:MOI = Mono+Omni()):AMI
}
