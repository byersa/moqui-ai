# KI: Handling Sudo in Background Agent Processes

## Context
When an agent (like Antigravity) runs a bash script in a background sub-process, common commands like `sudo systemctl` will hang indefinitely.

## Problem
`sudo` requires a real interactive terminal (TTY) to prompt for a password. Because the agent's background shell isn't a TTY, the `[sudo] password:` prompt is hidden and the agent can't input text to it.

## Solution
1. **Remove `sudo`**: Remove the `sudo` prefix from scripts intended for agent execution.
2. **Polkit Integration**: Modern Linux systems use `Polkit` to handle service management. Running `systemctl stop/start` without `sudo` will trigger a system-level GUI authentication popup on the user's desktop.
3. **Session Check**: If the user has already authorized a session in their terminal, Polkit may allow the agent's script to run silently without any prompt.

## Prevention
Always prefer standard `systemctl` commands over `sudo systemctl` for any automated or agent-driven workflows in a desktop Linux environment.
