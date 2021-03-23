package ai.dragonfly.monotomni.native

import ai.dragonfly.monotomni._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.language.implicitConversions

/**
 * Native trait for JavaScript implementation of [[RemoteClock]].
 */
trait RemoteClock extends Clock {
  /**
   * Exposes the ready method for scala callbacks.
   * @param callback a callback function to invoke once the RemoteClock has estimated ServerTime
   */
  def ready(callback: () => Unit):Unit

  /**
   * Generates an Approximate Monotonically increasing Omnipresent Identifier based on this [[RemoteClock]]'s current
   * best estimate of ServerTime.
   * @param moi optional locally generated [[MOI]].  If provided, the resulting [[AMI]] value will encode the [[AMI]] close to what
   * the server would have generated if it had been called at the time that the local [[MOI]] was generated.  If not provided
   * the [[AMI]] timestamp encodes now().
   * @return an AMI value.
   * @throws RemoteClockNotReady Exception.
   */
  def ami(moi:MOI = Mono+Omni()):AMI

  /**
   * Helper Method to expose the [[RemoteClock]].ami(moi:[[MOI]] = Mono+Omni()):[[AMI]] method to native JavaScript.
   * @param moiJS a js.BigInt value from native JS.
   * @return a js.BigInt value representing a valid [[AMI]] value.
   */
  @JSExport("ami") def amiJS(moiJS:js.BigInt):js.BigInt = orDefault[js.BigInt](moiJS, ami())

  /**
   * Helper method to expose RemoteClock.ready to native JavaScript callback functions.
   * @param callback a callback function to invoke once the RemoteClock has estimated ServerTime
   */
  @JSExport("ready") def readyJS(callback: js.Function0[Unit]):Unit = ready(callback)

  /**
   * A helper method to expose RemoteClock.now() to native JavaScript.
   * @return a best approximation of ServerTime at the time of invocation.
   */
  @JSExport("now") def nowJS():js.BigInt = now()
}
