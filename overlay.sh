#!/bin/bash
# overlay.sh — Dynamic, Additive, and Antigravity-First Overlay for Moqui Agent OS
# Place this at the root of your component (e.g., runtime/component/moqui-ai/)

# 1. Configuration
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
AGENT_OS="$(cd "$SCRIPT_DIR/../moqui-agent-os" && pwd)"

# Identify this component from its folder name
COMP_RAW=$(basename "$SCRIPT_DIR")
# Clean the name for the prefix (remove dashes: "moqui-ai" -> "moquiai")
COMP_CLEAN=$(echo "$COMP_RAW" | tr -d '-')

# Final prefix for all symlinks in the Agent OS tree
PREFIX="overlay-${COMP_CLEAN}-"

if [ ! -d "$AGENT_OS" ]; then
    echo "Error: Target directory not found at $AGENT_OS"
    exit 1
fi

echo "Source Component: $COMP_RAW (Prefix: $PREFIX)"
echo "Target OS:         $AGENT_OS"

# 2. Phase 1: Clean ONLY this component's existing symlinks
echo "Cleaning old symlinks for $COMP_RAW..."
find "$AGENT_OS" -name "$PREFIX*" -type l -delete

# Special handling for settings.local.json (Antigravity/Claude specific)
if [ -L "$AGENT_OS/.antigravity/settings.local.json" ]; then
    tgt=$(readlink "$AGENT_OS/.antigravity/settings.local.json")
    if [[ "$tgt" == *"$COMP_RAW"* ]]; then
        rm "$AGENT_OS/.antigravity/settings.local.json"
    fi
fi

# 3. Phase 2: Create fresh symlinks from this component's overlay folder
if [ ! -d "$SCRIPT_DIR/overlay" ]; then
    echo "No 'overlay/' directory found in $SCRIPT_DIR. Skipping link phase."
    exit 0
fi

count=0
# Recursively find all files in the local overlay directory
# This handles .antigravity/, .claude/, guidelines/, references/, etc.
for src in $(find "$SCRIPT_DIR/overlay" -type f); do
    # Calculate the relative path from the overlay/ folder
    rel_path="${src#$SCRIPT_DIR/overlay/}"
    
    # Target directory in moqui-agent-os
    target_dir="$(dirname "$AGENT_OS/$rel_path")"
    filename="$(basename "$rel_path")"
    
    # Determine the target filename with prefix (except for config files)
    if [[ "$filename" == "settings.local.json" || "$filename" == "config.yml" ]]; then
        target_file="$target_dir/$filename"
    else
        target_file="$target_dir/${PREFIX}${filename}"
    fi

    # Create target directory and link the file using absolute path for stability
    mkdir -p "$target_dir"
    ln -sf "$src" "$target_file"
    
    count=$((count + 1))
done

echo "Done. $count $COMP_RAW records meshed into Antigravity Agent OS."
