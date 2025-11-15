package mcp.tools

import mcp.tools.arithmetic.{AddTool, DivideTool, MultiplyTool, SubtractTool}

object ArithmeticTools:
  private val tools: List[Tool] = List(
    AddTool,
    SubtractTool,
    MultiplyTool,
    DivideTool
  )

  def all: List[Tool] = tools

  def add: Tool = AddTool
  def subtract: Tool = SubtractTool
  def multiply: Tool = MultiplyTool
  def divide: Tool = DivideTool
