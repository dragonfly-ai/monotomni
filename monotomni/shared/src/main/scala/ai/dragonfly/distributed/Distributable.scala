package ai.dragonfly.distributed

import ai.dragonfly.monotomni.MOI

/**
 * Guarantees that all implementing classes wil have a Monotonically increasing Omnipresent Identifier.
 */
trait Distributable {
  val moi:MOI
}
