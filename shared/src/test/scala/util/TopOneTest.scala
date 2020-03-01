package frankenpaxos.util

import org.scalacheck.Gen
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import scala.collection.mutable

class TopOneTest extends FlatSpec with Matchers {
  val like = new VertexIdLike[(Int, Int)] {
    override def leaderIndex(t: (Int, Int)): Int = t._1
    override def id(t: (Int, Int)): Int = t._2
    override def make(x: Int, y: Int): (Int, Int) = (x, y)
  }

  "A TopOne" should "return zeros after no puts" in {
    val topOne = new TopOne(3, like)
    topOne.get() shouldBe mutable.Buffer(0, 0, 0)
  }

  it should "return one thing after putting it" in {
    val topOne = new TopOne(3, like)
    topOne.put((0, 0))
    topOne.get() shouldBe mutable.Buffer(1, 0, 0)
  }

  it should "return one thing per leader after putting them" in {
    val topOne = new TopOne(3, like)
    topOne.put((0, 0))
    topOne.put((1, 1))
    topOne.put((2, 2))
    topOne.get() shouldBe mutable.Buffer(1, 2, 3)
  }

  it should "return top thing per leader after lots of puts" in {
    val topOne = new TopOne(5, like)
    topOne.put((0, 0))
    topOne.put((0, 1))
    topOne.put((0, 2))
    topOne.put((1, 1))
    topOne.put((1, 10))
    topOne.put((2, 2))
    topOne.put((4, 3))
    topOne.put((4, 1))
    topOne.put((4, 7))
    topOne.put((4, 1))
    topOne.get() shouldBe mutable.Buffer(3, 11, 3, 0, 8)
  }

  it should "merge two empty indexes correctly" in {
    val lhs = new TopOne(3, like)
    val rhs = new TopOne(3, like)
    lhs.mergeEquals(rhs)
    lhs.get() shouldBe mutable.Buffer(0, 0, 0)
  }

  it should "merge lhs empty correctly" in {
    val lhs = new TopOne(3, like)
    val rhs = new TopOne(3, like)
    rhs.put((0, 0))
    rhs.put((1, 1))
    rhs.put((2, 2))
    lhs.mergeEquals(rhs)
    lhs.get() shouldBe mutable.Buffer(1, 2, 3)
  }

  it should "merge rhs empty correctly" in {
    val lhs = new TopOne(3, like)
    val rhs = new TopOne(3, like)
    lhs.put((0, 0))
    lhs.put((1, 1))
    lhs.put((2, 2))
    lhs.mergeEquals(rhs)
    lhs.get() shouldBe mutable.Buffer(1, 2, 3)
  }

  it should "merge neither empty correctly" in {
    val lhs = new TopOne(3, like)
    val rhs = new TopOne(3, like)
    lhs.put((0, 0))
    lhs.put((1, 1))
    lhs.put((2, 2))
    rhs.put((0, 0))
    rhs.put((1, 10))
    lhs.mergeEquals(rhs)
    lhs.get() shouldBe mutable.Buffer(1, 11, 3)
  }
}
