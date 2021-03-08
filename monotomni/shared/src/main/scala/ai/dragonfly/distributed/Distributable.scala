package ai.dragonfly.distributed

import ai.dragonfly.monotomni.{MOI, Mono, Omni}

trait Distributable {
  val moi:MOI = Mono+Omni()
}
