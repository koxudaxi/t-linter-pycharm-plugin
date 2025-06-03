# TLinter PyCharm Plugin

TLinter provides syntax highlighting for PEP 750 template strings in PyCharm.

## Features

- 🎨 Automatic language injection for template strings with type annotations
- 📝 Support for `Annotated[Template, "language"]` pattern
- 🔤 Type alias support (e.g., `type html = Annotated[Template, "html"]`)
- 🔍 Function parameter type inference
- 🌐 Support for HTML, SQL, JSON, YAML, JavaScript, TypeScript, CSS, and more

## Installation

1. Open PyCharm
2. Go to Settings/Preferences → Plugins
3. Search for "TLinter"
4. Click Install

## Usage

### Basic Example

```python
from typing import Annotated
from string.templatelib import Template

# Automatic HTML syntax highlighting
def render_page(content: Annotated[Template, "html"]) -> str:
    return content.render()

page = t"""
<!DOCTYPE html>
<html>
    <body>
        <h1>{title}</h1>
        <p>{content}</p>
    </body>
</html>
"""

# SQL highlighting
query: Annotated[Template, "sql"] = t"SELECT * FROM users WHERE id = {user_id}"
```

### Type Alias Support

```python
# Define reusable type aliases
type html = Annotated[Template, "html"]
type sql = Annotated[Template, "sql"]

# Use with automatic language detection
content: html = t"<div>{message}</div>"
db_query: sql = t"UPDATE users SET name = {name} WHERE id = {id}"
```

### Function Parameter Inference

```python
def execute_query(query: sql) -> list:
    return db.execute(query)

# Language inferred from function parameter type
execute_query(t"SELECT * FROM products WHERE price < {max_price}")
```

## Supported Languages

| Language | Annotation |
|----------|------------|
| HTML | `"html"` |
| XML | `"xml"` |
| SQL | `"sql"` |
| JSON | `"json"` |
| YAML | `"yaml"` |
| JavaScript | `"javascript"` or `"js"` |
| TypeScript | `"typescript"` or `"ts"` |
| CSS | `"css"` |
| Markdown | `"markdown"` or `"md"` |

## Requirements

- PyCharm 2025.2 or later
- Python 3.14.0b1 or later (for PEP 750 support)

## License

This plugin is licensed under the MIT License.