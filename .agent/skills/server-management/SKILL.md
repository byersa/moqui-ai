---
name: Server Management
description: Manages the Moqui server (Status, Start, Stop).
alias: server
---

# Server Management Skill

This skill allows the Agent to control the local Moqui development server.

## Usage

`/server [status|start|stop|restart]`

## Commands

### Status
Checks if the server is running.
1. Run `pgrep -f "moqui.war"` or `jps -l | grep moqui.war`.
2. Report the PID if found, or "Stopped" if not.

### Start
Starts the server in the background.
1. Check Status first. If running, abort.
2. Execute: `nohup ./start-server.sh > runtime/log/moqui.log 2>&1 &`
3. Wait 5 seconds.
4. Check Status again to confirm PID exists.
5. Report "Server started with PID [pid]. Logs at runtime/log/moqui.log".

### Stop
Stops the running server.
1. Execute: `./stop-server.sh`
2. Wait 2 seconds.
3. Check Status. If still running, force kill: `pkill -9 -f "moqui.war"`.
4. Report "Server stopped."

### Restart
1. Execute `Stop`.
2. Execute `Start`.
