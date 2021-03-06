package scalapb.validate.cats

import cats.data.{NonEmptyList, NonEmptyMap, NonEmptySet}
import e2e.cats.types.{
  NonEmptyTypes,
  NonEmptyTypesTesting,
  NonEmptyTypesWithSubRules
}
import scalapb.validate.Success
import scalapb.validate.Validator
import scalapb.validate.ValidationHelpers
import scalapb.validate.ValidationException
import scalapb.json4s.JsonFormat
import e2e.cats.alltypes.instances
import scalapb.GeneratedMessage
import scalapb.GeneratedMessageCompanion

class CatsTypesSpec extends munit.FunSuite with ValidationHelpers {
  val nonEmptyTypes = NonEmptyTypes(
    nonEmptySet = NonEmptySet.of("foo", "bar"),
    nonEmptyList = NonEmptyList.of("bar", "baz"),
    nonEmptyMap = NonEmptyMap.of(3 -> 4)
  )

  val nonEmptyTypesTesting = NonEmptyTypesTesting(
    nonEmptySet = Seq("foo", "bar"),
    nonEmptyList = Seq("baz"),
    nonEmptyMap = Map(3 -> 4)
  )

  test("NonEmptyTypes serialize and parse successfully") {
    assertEquals(
      NonEmptyTypes.parseFrom(nonEmptyTypes.toByteArray),
      nonEmptyTypes
    )
    assertEquals(
      NonEmptyTypes.fromAscii(nonEmptyTypes.toProtoString),
      nonEmptyTypes
    )
    assertEquals(
      Validator[NonEmptyTypes].validate(nonEmptyTypes).isSuccess,
      true
    )
  }

  test("NonEmptyTypes fails to construct if invalid") {
    intercept[ValidationException] {
      nonEmptyTypes.copy(foo = "verylongstring")
    }
  }

  test("NonEmptyTypes fail when input is empty") {
    interceptMessage[ValidationException](
      "Could not build an empty NonEmptySet"
    )(NonEmptyTypes.parseFrom(Array[Byte]()))
  }

  val subrules = NonEmptyTypesWithSubRules(
    nonEmptySet = NonEmptySet.of("fooba", "gooma"),
    nonEmptyList = NonEmptyList.of("fooba", "gooma"),
    nonEmptyMap = NonEmptyMap.of(4 -> 5)
  )

  test("NonEmptyTypesWithSubRules should validate correctly") {
    assertEquals(
      Validator[NonEmptyTypesWithSubRules].validate(subrules),
      Success
    )
  }

  test("NonEmptyTypesWithSubRules should fail member validation") {
    val invalid1 =
      subrules.update(
        _.nonEmptySet := NonEmptySet.of("foo")
      ) // str length is not 5
    val invalid2 =
      subrules.update(
        _.nonEmptyList := NonEmptyList.of("foo")
      ) // str length is not 5
    val invalid3 =
      subrules.update(_.nonEmptyMap := NonEmptyMap.of(1 -> 12)) // key not gte 4
    val invalid4 =
      subrules.update(
        _.nonEmptyMap := NonEmptyMap.of(4 -> 4)
      ) // value not gte 5

    assertFailure(
      Validator[NonEmptyTypesWithSubRules].validate(invalid1),
      List(
        ("NonEmptyTypesWithSubRules.non_empty_set", "\"foo\"")
      )
    )
    assertFailure(
      Validator[NonEmptyTypesWithSubRules].validate(invalid2),
      List(
        ("NonEmptyTypesWithSubRules.non_empty_list", "\"foo\"")
      )
    )
    assertFailure(
      Validator[NonEmptyTypesWithSubRules].validate(invalid3),
      List(
        ("NonEmptyTypesWithSubRules.NonEmptyMapEntry.key", Int.box(1))
      )
    )
    assertFailure(
      Validator[NonEmptyTypesWithSubRules].validate(invalid4),
      List(
        ("NonEmptyTypesWithSubRules.NonEmptyMapEntry.value", Int.box(4))
      )
    )
  }

  test("cat types are json-serializable") {
    assertEquals(
      JsonFormat.fromJsonString[NonEmptyTypes](
        JsonFormat.toJsonString(nonEmptyTypes)
      ),
      nonEmptyTypes
    )
  }

  test("Throws exception for empty list in json parsing") {
    val j = """
    {"nonEmptySet":["bar","foo"],"nonEmptyList":[],"nonEmptyMap":{"3":4}}
    """
    interceptMessage[ValidationException](
      "Could not build an empty NonEmptyList"
    ) {
      JsonFormat.fromJsonString[NonEmptyTypes](j)
    }
  }

  test("Throws exception when non-empty set has duplicate elements") {
    val n = nonEmptyTypesTesting.withNonEmptySet(Seq("foo", "foo"))
    interceptMessage[ValidationException](
      "Got duplicate elements for NonEmptySet"
    )(NonEmptyTypes.parseFrom(n.toByteArray))
  }

  test("Throws exception when set has duplicate elements") {
    val n = nonEmptyTypesTesting.withSet(Seq("foo", "foo"))
    interceptMessage[ValidationException](
      "Got duplicate elements for Set"
    )(NonEmptyTypes.parseFrom(n.toByteArray))
  }

  test("all instances serialize and parse to binary") {
    instances.all.foreach { p =>
      assertEquals(
        p.companion.parseFrom(p.toByteArray).asInstanceOf[GeneratedMessage],
        p
      )
    }
  }

  test("all instances serialize and parse to json") {
    instances.all.foreach { p =>
      assertEquals(
        JsonFormat
          .fromJsonString[GeneratedMessage](JsonFormat.toJsonString(p))(
            p.companion
              .asInstanceOf[GeneratedMessageCompanion[GeneratedMessage]]
          )
          .asInstanceOf[GeneratedMessage],
        p
      )
    }
  }
}
