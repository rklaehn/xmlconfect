package com.mthaler.xmlconfect

import org.scalatest.FunSuite

import ProductFormat._
import scala.reflect.classTag

object ProductFormatTest {
  case class TestClass(field1: String, field2: Int, field3: Double)
  case class Product1(field1: String)
  case class Product2(field1: String, field2: Int)
  case class Product1WithProduct1(product1: Product1)
  case class Product1WithOption(option: Option[Int])
  case class Person(name: String = "Albert Einstein", age: Int = 42)
}

class ProductFormatTest extends FunSuite {

  import ProductFormatTest._

  test("extractFieldNames") {
    val fieldNames = ProductFormat.extractFieldNames(classTag[TestClass])
    assert(Seq("field1", "field2", "field3") == fieldNames.toSeq)
  }

  test("xmlFormat1") {
    import BasicAttrFormats._
    implicit val f = xmlFormat1(Product1)
    val p = Product1("test")
    assertResult(<Product1 field1="test"/>) {
      p.toNode
    }
    assertResult(p) {
      <Product1 field1="test"/>.convertTo[Product1]
    }
    implicit val f2 = xmlFormat1(Product1WithProduct1)
    val p2 = Product1WithProduct1(Product1("test"))
    assertResult(<Product1WithProduct1><product1 field1="test"/></Product1WithProduct1>) {
      p2.toNode
    }
    assertResult(p2) {
      <Product1WithProduct1><product1 field1="test"/></Product1WithProduct1>.convertTo[Product1WithProduct1]
    }
  }

  test("xmlFormat2") {
    import com.mthaler.xmlconfect.BasicAttrFormats._
    implicit val f = xmlFormat2(Product2)
    val p = Product2("test", 42)
    assertResult(<Product2 field1="test" field2="42"/>) {
      p.toNode
    }
    assertResult(p) {
      <Product2 field1="test" field2="42"/>.convertTo[Product2]
    }
  }

  test("xmlFormat1WithOptions") {
    import com.mthaler.xmlconfect.BasicAttrFormats._
    import com.mthaler.xmlconfect.StandardFormats._
    implicit val f = xmlFormat1(Product1WithOption)
    val p = Product1WithOption(Some(42))
    assertResult(<Product1WithOption option="42"/>) {
      p.toNode
    }
    assertResult(p) {
      <Product1WithOption option="42"/>.convertTo[Product1WithOption]
    }
    val p2 = Product1WithOption(None)
    assertResult(<Product1WithOption/>) {
      p2.toNode
    }
    assertResult(p2) {
      <Product1WithOption/>.convertTo[Product1WithOption]
    }
  }

  test("xmlFormat2CustomFieldNames") {
    import com.mthaler.xmlconfect.BasicAttrFormats._
    implicit val f = xmlFormat(Product2.apply, "myfield1", "myfield2")
    val p = Product2("test", 42)
    assertResult(<Product2 myfield1="test" myfield2="42"/>) {
      p.toNode
    }
    assertResult(p) {
      <Product2 myfield1="test" myfield2="42"/>.convertTo[Product2]
    }
  }

  test("missingFields") {
    import com.mthaler.xmlconfect.BasicAttrFormats._
    implicit val f = xmlFormat2(Person)
    assertResult(<Person name="Richard Feynman" age="56"/>) {
      Person("Richard Feynman", 56).toNode
    }
    assertResult(Person("Richard Feynman", 56)) {
      <Person name="Richard Feynman" age="56"/>.convertTo[Person]
    }
    assertResult(Person("Richard Feynman", 42)) {
      <Person name="Richard Feynman"/>.convertTo[Person]
    }
  }
}
