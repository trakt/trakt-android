#!/usr/bin/env python3
"""Escapes bare % signs as %% in strings that Android formats with arguments.

Crowdin exports strings shared with web, where "%s% match" is valid. On Android,
String.format throws UnknownFormatConversionException on the trailing %.

A key counts as formatted when its values/strings.xml source has a placeholder.
Strings without placeholders are never passed through String.format, so a %% there
would render literally and they are left untouched.
"""

import re
import sys
from pathlib import Path

STRING = re.compile(r"(?P<open><string\b(?P<attrs>[^>]*)>)(?P<body>.*?)(?P<close></string>)", re.DOTALL)
NAME = re.compile(r'\bname="([^"]+)"')
NOT_FORMATTED = re.compile(r'\bformatted="false"')
SPECIFIER = r"%(?:\d+\$)?[-#+0,]*\d*(?:\.\d+)?[sSdoxXeEfgGcCbBhH]"
TOKEN = re.compile(rf"%%|%n|{SPECIFIER}|%")


def formatted_keys(source: Path) -> set:
    keys = set()
    for match in STRING.finditer(source.read_text(encoding="utf-8")):
        name = NAME.search(match["attrs"])
        if name is None or NOT_FORMATTED.search(match["attrs"]):
            continue
        if re.search(SPECIFIER, match["body"]):
            keys.add(name.group(1))
    return keys


def escape(body: str) -> str:
    return TOKEN.sub(lambda token: "%%" if token.group() == "%" else token.group(), body)


def process(file: Path, keys: set) -> list:
    fixed = []

    def fix(match):
        name = NAME.search(match["attrs"])
        if name is None or name.group(1) not in keys:
            return match.group()
        body = escape(match["body"])
        if body != match["body"]:
            fixed.append(name.group(1))
        return match["open"] + body + match["close"]

    text = file.read_text(encoding="utf-8")
    updated = STRING.sub(fix, text)
    if updated != text:
        file.write_text(updated, encoding="utf-8")
    return fixed


def main() -> None:
    res = Path(sys.argv[1])
    keys = formatted_keys(res / "values" / "strings.xml")
    for file in sorted(res.glob("values*/strings.xml")):
        for name in process(file, keys):
            print(f"{file.parent.name}: escaped % in '{name}'")


if __name__ == "__main__":
    main()
