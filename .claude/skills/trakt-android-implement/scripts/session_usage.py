#!/usr/bin/env python3
"""Sum this Claude Code session's token usage per model since a start time.

Usage: python3 session_usage.py --since 2026-10-19T12:00:00Z [--session PATH]

Prints one JSON object. Exits 0 with "tokens": null when no transcript is found.
"""
import argparse
import glob
import json
import os
import sys
from datetime import datetime, timezone

FIELDS = {
    "input_tokens": "input",
    "output_tokens": "output",
    "cache_creation_input_tokens": "cache_write",
    "cache_read_input_tokens": "cache_read",
}


def parse_time(value):
    return datetime.fromisoformat(value.replace("Z", "+00:00")).astimezone(timezone.utc)


def projects_dir():
    base = os.environ.get("CLAUDE_CONFIG_DIR") or os.path.expanduser("~/.claude")
    return os.path.join(base, "projects")


def newest_session():
    files = glob.glob(os.path.join(projects_dir(), "*", "*.jsonl"))
    return max(files, key=os.path.getmtime) if files else None


def replies(path, since):
    """Last usage line per message id: a reply with several parts is logged once per part."""
    latest = {}
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            try:
                entry = json.loads(line)
            except ValueError:
                continue
            message = entry.get("message")
            if not isinstance(message, dict) or not message.get("usage"):
                continue
            stamp = entry.get("timestamp")
            if since and (not stamp or parse_time(stamp) < since):
                continue
            latest[message.get("id") or id(entry)] = message
    return latest.values()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--since", help="ISO time the work started (UTC)")
    parser.add_argument("--session", help="transcript .jsonl; default: newest session on this machine")
    args = parser.parse_args()
    since = parse_time(args.since) if args.since else None

    session = args.session or newest_session()
    if not session or not os.path.exists(session):
        print(json.dumps({"tokens": None, "reason": "no transcript found", "searched": projects_dir()}))
        return 0

    subagent_files = glob.glob(os.path.join(session[: -len(".jsonl")], "subagents", "*.jsonl"))
    models = {}
    active_subagents = 0
    for path in [session, *subagent_files]:
        found = False
        for message in replies(path, since):
            found = True
            bucket = models.setdefault(message.get("model") or "unknown", {v: 0 for v in FIELDS.values()} | {"replies": 0})
            bucket["replies"] += 1
            for key, name in FIELDS.items():
                bucket[name] += message["usage"].get(key) or 0
        if found and path != session:
            active_subagents += 1

    totals = {name: sum(m[name] for m in models.values()) for name in FIELDS.values()}
    print(json.dumps({
        "session": os.path.basename(session)[: -len(".jsonl")],
        "since": args.since,
        "until": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "subagents": active_subagents,
        "models": models,
        "tokens": totals,
    }))
    return 0


if __name__ == "__main__":
    sys.exit(main())
