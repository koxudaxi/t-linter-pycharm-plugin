# t-linter PyCharm Plugin
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

<!-- Plugin description -->
T-Linter provides syntax highlighting for PEP 750 template strings in PyCharm.

## Features

- 🎨 Automatic language injection for template strings with type annotations
- 📝 Support for `Annotated[Template, "language"]` pattern
- 🔤 Type alias support (e.g., `type html = Annotated[Template, "html"]`)
- 🔍 Function parameter type inference
- 🌐 Support for HTML, SQL, JSON, YAML, JavaScript, TypeScript, CSS, and more

## Usage

```python
from typing import Annotated
from string.templatelib import Template

# Automatic HTML syntax highlighting
page: Annotated[Template, "html"] = t"""
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

# Type alias support
type html = Annotated[Template, "html"]
content: html = t"<div>{message}</div>"
```
<!-- Plugin description end -->