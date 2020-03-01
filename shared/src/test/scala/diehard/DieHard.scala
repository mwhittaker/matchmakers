package frankenpaxos.diehard

import frankenpaxos.simulator.SimulatedSystem
import org.scalacheck
import org.scalacheck.Gen
import org.scalacheck.rng.Seed

// DieHard models the scene from "Die Hard With a Vengeance" in which the two
// main protagonists are given a 3 gallon and 5 gallon jug and need to fill a
// jug with exactly 4 gallons. The idea for modelling this system was taken
// from Lamport's video course on TLA+ [1].
//
// [1]: https://lamport.azurewebsites.net/video/videos.html
class DieHard {
  var small: Int = 0
  var big: Int = 0

  def fillSmall(): Unit = {
    small = 3
  }

  def fillBig(): Unit = {
    big = 5
  }

  def emptySmall(): Unit = {
    small = 0
  }

  def emptyBig(): Unit = {
    big = 0
  }

  def smallToBig(): Unit = {
    if (big + small <= 5) {
      big = big + small
      small = 0
    } else {
      small = small - (5 - big)
      big = 5
    }
  }

  def bigToSmall(): Unit = {
    if (small + big <= 3) {
      small = small + big
      big = 0
    } else {
      big = big - (3 - small)
      small = 3
    }
  }
}

sealed trait DieHardCommand
case object FillSmall extends DieHardCommand
case object FillBig extends DieHardCommand
case object EmptySmall extends DieHardCommand
case object EmptyBig extends DieHardCommand
case object SmallToBig extends DieHardCommand
case object BigToSmall extends DieHardCommand

class SimulatedDieHard extends SimulatedSystem {
  override type System = DieHard
  override type State = (Int, Int)
  override type Command = DieHardCommand

  override def newSystem(seed: Long): System = {
    new DieHard()
  }

  override def getState(
      system: System
  ): State = {
    (system.small, system.big)
  }

  override def generateCommand(
      system: System
  ): Option[Command] = {
    val gen: Gen[Command] =
      Gen.oneOf(
        Gen.const(FillSmall),
        Gen.const(FillBig),
        Gen.const(EmptySmall),
        Gen.const(EmptyBig),
        Gen.const(SmallToBig),
        Gen.const(BigToSmall)
      )
    gen.apply(Gen.Parameters.default, Seed.random())
  }

  override def runCommand(
      system: System,
      command: Command
  ): System = {
    command match {
      case FillSmall  => system.fillSmall()
      case FillBig    => system.fillBig()
      case EmptySmall => system.emptySmall()
      case EmptyBig   => system.emptyBig()
      case SmallToBig => system.smallToBig()
      case BigToSmall => system.bigToSmall()
    }
    system
  }

  override def stateInvariantHolds(
      state: State
  ): SimulatedSystem.InvariantResult = {
    val (small, big) = state
    if (0 <= small && small <= 3 && 0 <= big && big <= 5) {
      SimulatedSystem.InvariantHolds
    } else {
      SimulatedSystem.InvariantViolated(
        s"Type invariant broken: small=${small}, big=${big}."
      )
    }

    // Uncomment this code to find an execution of the system that finds a jug
    // with four gallons.
    //
    //   if (small != 4 && big != 4) {
    //     SimulatedSystem.InvariantHolds
    //   } else {
    //     SimulatedSystem.InvariantViolated("4 gallon found!")
    //   }
  }

}
