package mcp.tools

import mcp.tools.logic.{
  AndTool,
  OrTool,
  XorTool,
  LessThanTool,
  GreaterThanTool,
  EqualsTool
}

object LogicTools:
  private val tools: List[Tool] = List(
    AndTool,
    OrTool,
    XorTool,
    LessThanTool,
    GreaterThanTool,
    EqualsTool
  )

  def all: List[Tool] = tools

  def and: Tool = AndTool
  def or: Tool = OrTool
  def xor: Tool = XorTool
  def lessThan: Tool = LessThanTool
  def greaterThan: Tool = GreaterThanTool
  def equals: Tool = EqualsTool
