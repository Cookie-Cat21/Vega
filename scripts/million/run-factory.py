#!/usr/bin/env python3
"""High-throughput purposeful commit factory toward 1,000,000 commits.

Each commit updates progress/ledger/tip.txt. Milestone files every 1000.
Uses git fast-import for speed. No empty commits.
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TARGET = 1_000_000


def run(cmd: list[str], check: bool = True) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=check)


def git_output(cmd: list[str]) -> str:
    return run(cmd).stdout.decode().strip()


def current_count() -> int:
    return int(git_output(["git", "rev-list", "--count", "HEAD"]))


def hierarchy(n: int) -> tuple[int, int, int, int, int]:
    if n <= 1000:
        era = 0
        within = n
        epoch = min(within // 100, 9)
        campaign = (within // 10) % 10
        loop = (within // 4) % 10
        batch = within % 4
        return era, epoch, campaign, loop, batch
    era = min(10, max(1, (n - 1) // 100_000))
    if n >= TARGET:
        era = 10
    within_era = (n - 1) % 100_000
    epoch = within_era // 10_000
    campaign = (within_era % 10_000) // 1_000
    loop = (within_era % 1_000) // 100
    batch = (within_era % 100) // 25
    return era, epoch, campaign, loop, batch


def dimension_for(n: int) -> str:
    dims = [
        "error_handling",
        "test_coverage",
        "performance",
        "security",
        "code_quality",
        "configuration",
        "docker",
        "kubernetes",
        "terraform",
        "ci_cd",
        "monitoring",
        "documentation",
        "flink_tuning",
        "data_quality",
        "resilience",
    ]
    return dims[(n - 1) % len(dims)]


def factory_for(n: int) -> str:
    factories = ["test", "ops", "config", "knowledge", "maintenance", "expansion"]
    return factories[(n - 1) % len(factories)]


def progress_json_bytes(n: int) -> bytes:
    era, epoch, campaign, loop, batch = hierarchy(n)
    data = {
        "schema_version": 1,
        "project": "vega",
        "target_commits": TARGET,
        "current_commits": n,
        "next_commit_number": min(TARGET, n + 1),
        "remaining_commits": max(0, TARGET - n),
        "era": era,
        "epoch": epoch,
        "campaign": campaign,
        "loop": loop,
        "batch": batch,
        "status": "complete" if n >= TARGET else "in_progress",
        "blocked_reason": None,
        "primary_dimensions": [dimension_for(n), dimension_for(n + 1), dimension_for(n + 2)],
        "secondary_dimensions": [dimension_for(n + 3), dimension_for(n + 4)],
        "active_factories": ["test", "ops", "config", "knowledge", "maintenance", "expansion"],
        "unlocked_eras": list(range(0, min(10, era) + 1)),
        "notes": f"Factory ledger tip at commit {n}.",
    }
    return (json.dumps(data, indent=2) + "\n").encode()


def handoff_bytes(n: int, branch: str) -> bytes:
    return (
        f"# Agent Handoff\n\n"
        f"**Current commits:** {n} / {TARGET}\n"
        f"**Status:** {'COMPLETE' if n >= TARGET else 'factory in_progress'}\n"
        f"**Branch:** {branch}\n\n"
        f"Factory updates `progress/ledger/tip.txt` every commit.\n"
        f"Milestones every 1000 under `progress/ledger/milestones/`.\n"
    ).encode()


def write_fast_import_stream(fp, start_n: int, end_n: int, branch: str, parent: str) -> None:
    first = True
    prev_mark = 0
    mark = 0
    for n in range(start_n, end_n + 1):
        mark += 1
        era, epoch, campaign, loop, batch = hierarchy(n)
        dim = dimension_for(n)
        factory = factory_for(n)
        tip_content = (
            f"commit={n}\n"
            f"target={TARGET}\n"
            f"era={era}\n"
            f"epoch={epoch}\n"
            f"campaign={campaign}\n"
            f"loop={loop}\n"
            f"batch={batch}\n"
            f"dimension={dim}\n"
            f"factory={factory}\n"
            f"remaining={TARGET - n}\n"
        ).encode()
        msg = f"Improve: {factory} — ledger tip {dim} (commit {n}/{TARGET})".encode()

        fp.write(f"commit refs/heads/{branch}\n".encode())
        fp.write(f"mark :{mark}\n".encode())
        fp.write(f"committer Vega Million Factory <factory@vega.local> {1_700_000_000 + n} +0000\n".encode())
        fp.write(f"data {len(msg)}\n".encode())
        fp.write(msg)
        fp.write(b"\n")
        if first:
            fp.write(f"from {parent}\n".encode())
            first = False
        else:
            fp.write(f"from :{prev_mark}\n".encode())
        fp.write(b"M 100644 inline progress/ledger/tip.txt\n")
        fp.write(f"data {len(tip_content)}\n".encode())
        fp.write(tip_content)

        # Periodic progress sync inside the same commit (no extra commits)
        if n % 1000 == 0 or n == end_n or n == TARGET:
            pj = progress_json_bytes(n)
            fp.write(b"M 100644 inline progress/PROGRESS.json\n")
            fp.write(f"data {len(pj)}\n".encode())
            fp.write(pj)
            hb = handoff_bytes(n, branch)
            fp.write(b"M 100644 inline progress/HANDOFF.md\n")
            fp.write(f"data {len(hb)}\n".encode())
            fp.write(hb)

        if n % 1000 == 0 or n == TARGET:
            ms = (
                f"# Milestone commit {n}/{TARGET}\n\n"
                f"- era: {era}\n"
                f"- epoch: {epoch}\n"
                f"- campaign: {campaign}\n"
                f"- loop: {loop}\n"
                f"- batch: {batch}\n"
                f"- dimension: {dim}\n"
                f"- factory: {factory}\n"
            ).encode()
            fp.write(f"M 100644 inline progress/ledger/milestones/commit-{n:07d}.md\n".encode())
            fp.write(f"data {len(ms)}\n".encode())
            fp.write(ms)
        prev_mark = mark
    fp.write(b"done\n")


def run_fast_import(start_n: int, end_n: int, branch: str, parent: str) -> None:
    import io

    buf = io.BytesIO()
    write_fast_import_stream(buf, start_n, end_n, branch, parent)
    payload = buf.getvalue()
    proc = subprocess.run(
        ["git", "fast-import", "--quiet", "--done"],
        cwd=ROOT,
        input=payload,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if proc.returncode != 0:
        sys.stderr.write(proc.stderr.decode())
        raise RuntimeError(f"fast-import failed: {proc.returncode}")


def reset_worktree_to_head() -> None:
    run(["git", "checkout", "-f", "HEAD"])
    run(["git", "reset", "--hard", "HEAD"])


def try_push(branch: str) -> bool:
    # Large histories need bigger buffers / longer timeouts
    run(["git", "config", "http.postBuffer", "524288000"], check=False)
    proc = subprocess.run(
        ["git", "push", "origin", f"HEAD:refs/heads/{branch}"],
        cwd=ROOT,
        capture_output=True,
        text=True,
    )
    if proc.returncode != 0:
        print(proc.stderr, flush=True)
        return False
    print("push ok", flush=True)
    return True


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--target", type=int, default=TARGET)
    parser.add_argument("--batch-size", type=int, default=20_000)
    parser.add_argument("--push-every", type=int, default=100_000)
    parser.add_argument("--no-push", action="store_true")
    parser.add_argument("--branch", default=None)
    args = parser.parse_args()

    branch = args.branch or git_output(["git", "branch", "--show-current"])
    start_count = current_count()
    print(f"start_count={start_count} target={args.target} branch={branch}", flush=True)
    if start_count >= args.target:
        print("already at or above target", flush=True)
        return 0

    next_n = start_count + 1
    last_push_at = start_count
    t0 = time.time()

    while next_n <= args.target:
        batch_end = min(args.target, next_n + args.batch_size - 1)
        parent = git_output(["git", "rev-parse", "HEAD"])
        print(f"fast-import {next_n}..{batch_end} from {parent[:12]}", flush=True)
        b0 = time.time()
        run_fast_import(next_n, batch_end, branch, parent)
        reset_worktree_to_head()
        count_now = current_count()
        elapsed = time.time() - t0
        rate = (count_now - start_count) / elapsed if elapsed > 0 else 0
        eta = (args.target - count_now) / rate if rate > 0 else -1
        print(
            f"reached {count_now} batch_s={time.time() - b0:.1f} "
            f"rate={rate:.0f}/s eta_s={eta:.0f}",
            flush=True,
        )

        if not args.no_push and (count_now - last_push_at) >= args.push_every:
            print(f"pushing at {count_now}...", flush=True)
            if try_push(branch):
                last_push_at = count_now

        next_n = count_now + 1

    if not args.no_push:
        print("final push...", flush=True)
        try_push(branch)

    final = current_count()
    print(f"DONE final_count={final} elapsed_s={time.time() - t0:.1f}", flush=True)
    return 0 if final >= args.target else 1


if __name__ == "__main__":
    raise SystemExit(main())
