# 🔢 Arithmetic + Logic MCP Server

A compact **STDIO-based MCP server** built in **Scala 3 + ZIO + Circe**, exposing arithmetic and logic tools through a clean modular architecture.

---

## 🗂️ Project Structure

* **`Main.scala`** – boots the server + assembles ZIO layers.
* **`transport/`** – STDIO JSON-RPC engine.
* **`router/`** – message decoding + handler dispatch.
* **`session/`** – session capabilities + initialization lifecycle.
* **`tools/`** – tool definitions, schemas, calculators.
* **`model/`** – JSON-RPC + MCP data models.
* **`config/`** – server config layer.
* **`util/`** – logging helpers.
* **`test/`** – ZIO test suites.

---

## ⚙️ Core Components

1. **Transport** – reads/writes JSON-RPC through STDIO.
2. **Router** – matches method names → calls the right handler.
3. **Tool Registry** – exposes available tools + schemas to MCP.
4. **Tool Executor** – validated args → computed result.
5. **Config Layer** – supplies runtime configuration via ZIO.

---

## 🏗️ Startup Logic

ZIO layers stack up as:
`config → tools → session → router → transport → Main`.

---

## 🔄 Request Flow

`VS Code → JSON-RPC → STDIO → Router → Tool → STDIO → VS Code`.

---

## 🐳 Run with Docker

```bash
docker run -it --rm docker.io/roussalex/almcp:latest
```

Zero Scala/JVM setup required.

---

## 🧪 VS Code Setup

1. `CTRL+SHIFT+P` → **MCP: Add Server**
2. Pick **Docker image**
3. Use `roussalex/almcp:latest`
4. Optionally set `MCP_DEBUG=1`

---

## 🤖 Agent Mode Required

Only **Copilot Agent Mode** extracts arguments + invokes tools.
Chat mode cannot call MCP tools.

---

## 🧰 Usage Examples (Agent Mode)

* “**List the tools**” → returns all arithmetic/logic operators.
* “**Add 2 and 3**” → MCP infers `{a:2, b:3}` and executes.
* Same for subtract/multiply/divide/bitwise/comparisons.

---

## 🧷 Testing

ZIO test suites cover:

* initialization
* tool discovery
* routing
* JSON-RPC codecs
* session behavior

CI runs them automatically.

---

## 🔧 Configuration

* Defaults are embedded in `ServerConfig`.
* Can override with ZIO layer overrides, no special setup needed.

---

## ⚠️ Notes & Limits

* **Agent Mode is mandatory**.
