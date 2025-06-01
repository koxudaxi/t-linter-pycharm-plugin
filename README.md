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

# Define variables for the templates
title = "Welcome"
content = "Hello, World!"
user_id = 123
message = "Important notification"

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

## License Requirements

While this plugin itself is free and open source, certain language injection features depend on your PyCharm edition:

### Community Edition (Free)
The following languages are supported in PyCharm Community Edition:
- **HTML/XML** - Full syntax highlighting and code assistance
- **JSON** - Full syntax highlighting and validation
- **YAML** - Full syntax highlighting and validation

### Professional Edition (Paid License Required)
Advanced language support requires PyCharm Professional Edition:
- **SQL** - Database tools and SQL dialect support are Professional-only features
- **JavaScript/TypeScript** - JS/TS support requires the Professional edition
- **CSS** - Advanced CSS features are part of the Web development toolset

For detailed information about PyCharm editions and their features, visit [JetBrains PyCharm Comparison](https://www.jetbrains.com/pycharm/features/editions_comparison_matrix.html).

**Note:** The plugin will still inject these languages in Community Edition, but you won't get syntax highlighting, code completion, or error detection without the corresponding language support from PyCharm Professional.
<!-- Plugin description end -->