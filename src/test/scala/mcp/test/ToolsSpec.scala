package mcp.test

import io.circe.{Json, JsonObject}
import mcp.tools._
import zio._
import zio.test.Assertion._
import zio.test._

object ToolsSpec extends ZIOSpecDefault:

  private def args(a: Int, b: Int): JsonObject =
    JsonObject("a" -> Json.fromInt(a), "b" -> Json.fromInt(b))

  private def withRegistry[A](effect: ZIO[ToolRegistry, Any, A]): ZIO[Any, Any, A] =
    effect.provideLayer(ToolRegistry.layer)

  def spec: Spec[TestEnvironment, Any] =
    suite("ToolsSpec")(
      suite("Arithmetic tools")(
        test("add returns correct sum") {
          val tool = ArithmeticTools.add
          assertZIO(tool.execute(args(2, 3)))(equalTo(Json.fromInt(5)))
        },
        test("subtract returns correct difference") {
          val tool = ArithmeticTools.subtract
          assertZIO(tool.execute(args(7, 4)))(equalTo(Json.fromInt(3)))
        },
        test("multiply returns correct product") {
          val tool = ArithmeticTools.multiply
          assertZIO(tool.execute(args(5, 6)))(equalTo(Json.fromInt(30)))
        },
        test("divide returns integer quotient") {
          val tool = ArithmeticTools.divide
          assertZIO(tool.execute(args(9, 3)))(equalTo(Json.fromInt(3)))
        },
        test("invalid arguments surface ToolError.InvalidArguments") {
          val tool = ArithmeticTools.add
          val invalidArgs = JsonObject("a" -> Json.fromInt(1))
          assertZIO(tool.execute(invalidArgs).either)(
            isLeft(isSubtype[ToolError.InvalidArguments](anything))
          )
        }
      ),
      suite("Logic tools")(
        test("and returns bitwise conjunction") {
          val tool = LogicTools.and
          for
            both <- tool.execute(args(1, 1))
            mixed <- tool.execute(args(1, 0))
          yield assertTrue(both == Json.fromInt(1), mixed == Json.fromInt(0))
        },
        test("or returns bitwise disjunction") {
          val tool = LogicTools.or
          for result <- tool.execute(args(1, 2))
          yield assertTrue(result == Json.fromInt(3))
        },
        test("xor returns bitwise exclusive or") {
          val tool = LogicTools.xor
          for
            res1 <- tool.execute(args(1, 2))
            res2 <- tool.execute(args(3, 3))
          yield assertTrue(res1 == Json.fromInt(3), res2 == Json.fromInt(0))
        },
        test("comparison tools yield boolean results") {
          val less = LogicTools.lessThan
          val greater = LogicTools.greaterThan
          val equal = LogicTools.equals
          for
            lt <- less.execute(args(1, 2))
            gt <- greater.execute(args(3, 2))
            eq <- equal.execute(args(5, 5))
          yield assertTrue(
            lt == Json.fromBoolean(true),
            gt == Json.fromBoolean(true),
            eq == Json.fromBoolean(true)
          )
        }
      ),
      suite("ToolRegistry")(
        test("listTools contains arithmetic and logic definitions") {
          withRegistry {
            for
              registry <- ZIO.service[ToolRegistry]
              tools    <- registry.listTools()
              names     = tools.map(_.name).toSet
            yield assertTrue(
              names.contains("add"),
              names.contains("subtract"),
              names.contains("multiply"),
              names.contains("divide"),
              names.contains("and"),
              names.contains("equals")
            )
          }
        },
        test("getTool returns a tool when present") {
          withRegistry {
            for
              registry <- ZIO.service[ToolRegistry]
              tool     <- registry.getTool("add")
            yield assertTrue(tool.definition.name == "add")
          }
        },
        test("getTool fails with ToolNotFound for unknown tool") {
          withRegistry {
            for
              registry <- ZIO.service[ToolRegistry]
              result   <- registry.getTool("nonexistent").either
            yield assert(result)(
              isLeft(equalTo(ToolError.ToolNotFound("nonexistent")))
            )
          }
        }
      )
    )
