package frankenpaxos.statemachine

import org.scalacheck.Gen
import org.scalatest.FlatSpec
import org.scalatest.Matchers

class ConflictIndexTest extends FlatSpec with Matchers {
  private def bytes(x: Int): Array[Byte] = {
    Array[Byte](x.toByte)
  }

  // Noop //////////////////////////////////////////////////////////////////////
  "Noop conflict index" should "put correctly" in {
    val noop = new Noop()
    val conflictIndex = noop.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.put(1, bytes(1))
    conflictIndex.put(2, bytes(2))
  }

  it should "getConflicts correctly" in {
    val noop = new Noop()
    val conflictIndex = noop.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.put(1, bytes(1))
    conflictIndex.put(2, bytes(2))
    conflictIndex.getConflicts(bytes(0)) shouldBe Set()
    conflictIndex.getConflicts(bytes(1)) shouldBe Set()
    conflictIndex.getConflicts(bytes(2)) shouldBe Set()
  }

  it should "getConflicts with snapshots correctly" in {
    val noop = new Noop()
    val conflictIndex = noop.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.putSnapshot(1)
    conflictIndex.put(2, bytes(2))
    conflictIndex.getConflicts(bytes(0)) shouldBe Set(1)
    conflictIndex.getConflicts(bytes(1)) shouldBe Set(1)
    conflictIndex.getConflicts(bytes(2)) shouldBe Set(1)
  }

  // Register //////////////////////////////////////////////////////////////////
  "Register conflict index" should "put and get correctly" in {
    val register = new Register()
    val conflictIndex = register.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.put(1, bytes(1))
    conflictIndex.put(2, bytes(2))
  }

  it should "remove correctly" in {
    val register = new Register()
    val conflictIndex = register.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.put(1, bytes(1))
    conflictIndex.remove(0)
    conflictIndex.getConflicts(bytes(2)) shouldBe Set(1)
  }

  it should "getConflicts correctly" in {
    val register = new Register()
    val conflictIndex = register.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.put(1, bytes(1))
    conflictIndex.put(2, bytes(2))
    conflictIndex.getConflicts(bytes(0)) shouldBe Set(0, 1, 2)
    conflictIndex.getConflicts(bytes(1)) shouldBe Set(0, 1, 2)
    conflictIndex.getConflicts(bytes(2)) shouldBe Set(0, 1, 2)
  }

  it should "getConflicts with snapshots correctly" in {
    val register = new Register()
    val conflictIndex = register.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.putSnapshot(1)
    conflictIndex.put(2, bytes(2))
    conflictIndex.getConflicts(bytes(0)) shouldBe Set(0, 1, 2)
    conflictIndex.getConflicts(bytes(1)) shouldBe Set(0, 1, 2)
    conflictIndex.getConflicts(bytes(2)) shouldBe Set(0, 1, 2)
  }

  // AppendLog /////////////////////////////////////////////////////////////////
  "AppendLog conflict index" should "put and get correctly" in {
    val log = new AppendLog()
    val conflictIndex = log.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.put(1, bytes(1))
    conflictIndex.put(2, bytes(2))
  }

  it should "remove correctly" in {
    val log = new AppendLog()
    val conflictIndex = log.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.put(1, bytes(1))
    conflictIndex.remove(0)
    conflictIndex.getConflicts(bytes(2)) shouldBe Set(1)
  }

  it should "getConflicts correctly" in {
    val log = new AppendLog()
    val conflictIndex = log.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.put(1, bytes(1))
    conflictIndex.put(2, bytes(2))
    conflictIndex.getConflicts(bytes(0)) shouldBe Set(0, 1, 2)
    conflictIndex.getConflicts(bytes(1)) shouldBe Set(0, 1, 2)
    conflictIndex.getConflicts(bytes(2)) shouldBe Set(0, 1, 2)
  }

  it should "getConflicts with snapshots correctly" in {
    val log = new AppendLog()
    val conflictIndex = log.conflictIndex[Int]()
    conflictIndex.put(0, bytes(0))
    conflictIndex.putSnapshot(1)
    conflictIndex.put(2, bytes(2))
    conflictIndex.getConflicts(bytes(0)) shouldBe Set(0, 1, 2)
    conflictIndex.getConflicts(bytes(1)) shouldBe Set(0, 1, 2)
    conflictIndex.getConflicts(bytes(2)) shouldBe Set(0, 1, 2)
  }

  // KeyValueStore /////////////////////////////////////////////////////////////
  private def get(keys: String*): KeyValueStoreInput =
    KeyValueStoreInput().withGetRequest(GetRequest(keys))

  private def set(keys: String*): KeyValueStoreInput =
    KeyValueStoreInput().withSetRequest(
      SetRequest(keys.map(key => SetKeyValuePair(key = key, value = "")))
    )

  "Key-value store conflict index" should "put and get correctly" in {
    val kvs = new KeyValueStore()
    val conflictIndex = kvs.typedConflictIndex[Int]()
    conflictIndex.put(0, get("x", "y"))
    conflictIndex.put(1, get("x", "y"))
    conflictIndex.put(2, get("x", "y"))
  }

  it should "remove correctly" in {
    val kvs = new KeyValueStore()
    val conflictIndex = kvs.typedConflictIndex[Int]()
    conflictIndex.put(0, get("x", "y"))
    conflictIndex.put(1, get("x", "y"))
    conflictIndex.remove(0)
    conflictIndex.getConflicts(set("x", "y")) shouldBe Set(1)
  }

  it should "getConflicts correctly" in {
    val kvs = new KeyValueStore()
    val conflictIndex = kvs.typedConflictIndex[Int]()
    conflictIndex.put(0, get("a", "b"))
    conflictIndex.put(0, get("x", "y"))
    conflictIndex.put(1, get("y", "z"))
    conflictIndex.put(2, set("y", "z"))
    conflictIndex.put(3, set("z"))

    conflictIndex.getConflicts(get("y")) shouldBe Set(2)
    conflictIndex.getConflicts(set("y")) shouldBe Set(0, 1, 2)
    conflictIndex.getConflicts(set("z")) shouldBe Set(1, 2, 3)
    conflictIndex.getConflicts(set("a")) shouldBe Set()
  }

  it should "getConflicts with snapshots correctly" in {
    val kvs = new KeyValueStore()
    val conflictIndex = kvs.typedConflictIndex[Int]()
    conflictIndex.put(0, get("a", "b"))
    conflictIndex.put(1, get("y", "z"))
    conflictIndex.putSnapshot(2)
    conflictIndex.put(3, set("z"))

    conflictIndex.getConflicts(get("a")) shouldBe Set(2)
    conflictIndex.getConflicts(get("b")) shouldBe Set(2)
    conflictIndex.getConflicts(get("x")) shouldBe Set(2)
    conflictIndex.getConflicts(get("y")) shouldBe Set(2)
    conflictIndex.getConflicts(get("z")) shouldBe Set(2, 3)
    conflictIndex.getConflicts(set("a")) shouldBe Set(0, 2)
    conflictIndex.getConflicts(set("b")) shouldBe Set(0, 2)
    conflictIndex.getConflicts(set("x")) shouldBe Set(2)
    conflictIndex.getConflicts(set("y")) shouldBe Set(1, 2)
    conflictIndex.getConflicts(set("z")) shouldBe Set(1, 2, 3)
  }
}
