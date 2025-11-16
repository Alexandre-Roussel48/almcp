# Arithmetic + Logic MCP Server

## Overview
- STDIO-based Model Context Protocol (MCP) server implemented in Scala 3 using ZIO and Circe.
- Ships arithmetic and logic tools (add, subtract, multiply, divide, bitwise operators, comparisons) behind a clean modular architecture.
- Designed around layered functional composition so each concern—transport, routing, tool orchestration, configuration—remains isolated and testable.

## High-Level Architecture
### Project structure
- `src/main/scala/mcp/Main.scala` – boots the server, assembles ZIO layers, and wires the STDIO bridge.
- `src/main/scala/mcp/transport` – STDIO JSON-RPC transport adapters (interfaces plus the concrete stream implementation).
- `src/main/scala/mcp/router` – JSON-RPC router plus method handlers (initialize, tools/list, tool dispatch) and shared error mapping.
- `src/main/scala/mcp/session` – session state service and abstractions for maintaining capabilities across requests.
- `src/main/scala/mcp/tools` – tool definitions, schemas, registries, and arithmetic/logic implementations grouped by concern.
- `src/main/scala/mcp/model` – data models for JSON-RPC and MCP payloads (initialize payloads, tool protocol, IDs, errors, capabilities).
- `src/main/scala/mcp/config` – configuration loading and environment wiring.
- `src/main/scala/mcp/util` – shared logging helpers.
- `src/test/scala/mcp/test` – ZIO test suites for initialization, routing, tool discovery, and session behavior.

### Core components
1. **Transport layer (STDIO JSON-RPC)** – `transport.StdioTransport` reads and writes JSON-RPC envelopes over standard input/output, abstracted behind a `Transport` trait for testing.
2. **Router** – `router.RequestRouter` decodes JSON-RPC messages, matches MCP method names, and invokes the right handler (`InitializeHandler`, `ToolsHandler`, arithmetic dispatch, etc.).
3. **Tool registry** – `tools.ToolRegistry` aggregates arithmetic and logic tool metadata, schemas, and callable references for the MCP tools/list response.
4. **Tool executor** – each tool implements `Tool`, receives validated arguments (e.g., via `tools.common.NumericPairSchema`), and returns structured MCP results.
5. **Configuration layer** – `config.ServerConfig` produces configuration values (timeouts, logging verbosity, etc.) exposed as a ZIO layer.

### ZIO layer composition at startup
`Main` composes layers roughly in this order: configuration → tool registry / tool logic → session services → routing handlers → STDIO transport. When the ZIO runtime boots, the composed layer graph injects dependencies into the server effect, ensuring each component only sees the interfaces it needs.

### Execution flow
`VS Code sends JSON-RPC → STDIO transport reads it → router dispatches to the correct handler → handler executes the tool or method logic via the registry/executor → response propagates back over STDIO to VS Code`.

## Run with Docker
```bash
docker run -it --rm ghcr.io/alexandre-roussel48/almcp:latest
```
- The published image bundles the complete MCP server; no local Scala toolchain or JVM setup is required for basic usage.
- The container exposes the STDIO MCP contract automatically, making it easy to integrate with editors or clients.

## Add the server to VS Code
1. Press `CTRL+SHIFT+P`.
2. Run **MCP: Add Server**.
3. Choose the **Docker image** option (VS Code will wrap the STDIO invocation automatically).
4. Enter `ghcr.io/alexandre-roussel48/almcp:latest` as the image and keep the default STDIO settings unless you need extra flags (Use MCP_DEBUG=1 to debug requests and responses).
5. Save the entry; the VS Code MCP client will now launch this containerized server directly whenever Copilot connects.

## Copilot Agent Mode is mandatory
- Copilot **Agent Mode** is the only mode that performs argument extraction and tool invocation.
- Regular chat mode does not populate tool parameters, so requests like `Add 2 and 3` will never reach the MCP server there.
- Always switch Copilot to Agent Mode before trying to interact with these tools.

## Usage examples (Agent Mode only)
- **List the tools** – Ask Copilot: “List the available tools from the arithmetic MCP server.” The agent issues `tools/list`, returning every arithmetic and logic operator.
- **Call the `add` tool** – Simply say: “Add 2 and 3.” Copilot infers `{ "a": 2, "b": 3 }`, invokes the `add` tool, and streams the sum back to the conversation.
- Any supported operator (subtract, multiply, divide, AND/OR/XOR, comparisons) works the same way—natural language → agent infers parameters → MCP tool executes.

## Testing
- ZIO test suites cover initialization (`SessionServiceSpec`), `tools/list` discovery (`ToolsSpec`), routing (`RequestRouterSpec`), JSON-RPC codecs, and session layers.
- Continuous integration runs these suites (see GitHub Actions for recent runs) to guarantee protocol compliance across changes.

## Configuration
- Defaults are embedded in `ServerConfig`; there are no required environment variables or custom build steps for standard Docker/VS Code usage.
- Advanced setups can override configuration through typical ZIO layer overrides, but nothing special is needed out of the box.

## Limitations & notes
- Copilot Agent Mode is **100% required** for argument extraction and tool execution; normal chat sessions cannot call tools.
- The server currently targets STDIO transport only; other transports would require additional wiring.

## Goal of this README
Equip new contributors and VS Code users with a concise understanding of:
- what the server provides (arithmetic + logic tools),
- how its ZIO-based layers and router fit together,
- how to run it quickly via Docker,
- and how to connect through VS Code with Copilot Agent Mode for productive tool calls.
