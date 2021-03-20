package ai.dragonfly.monotomni.native

import ai.dragonfly.monotomni.{AMI, MOI, Mono, Omni, Clock}

trait RemoteClock extends Clock {
  def ready(callback: () => Unit):Unit
  def ami(moi:MOI = Mono+Omni()):AMI
}
