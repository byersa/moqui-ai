#!/bin/bash

# Cross-Reference Validation Script for Agent OS Documentation
#
# This script validates cross-reference consistency between:
# - framework-guide.md section headers
# - Skill SKILL.md "Deep Reference" sections
# - Skill references/*.md "Framework Guide Reference" sections
# - Agent skill_integration sections
#
# Usage: ./validate-cross-references.sh [--fix] [--verbose]

set -e

# Configuration
AGENT_OS_DIR="${AGENT_OS_DIR:-$(dirname "$0")/..}"
FRAMEWORK_GUIDE="$AGENT_OS_DIR/framework-guide.md"
SKILLS_DIR="$AGENT_OS_DIR/.claude/skills"
AGENTS_DIR="$AGENT_OS_DIR/.claude/agents"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
ERRORS=0
WARNINGS=0
VALID=0

# Options
VERBOSE=false
FIX_MODE=false

# Parse arguments
for arg in "$@"; do
    case $arg in
        --verbose|-v)
            VERBOSE=true
            shift
            ;;
        --fix)
            FIX_MODE=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [--fix] [--verbose]"
            echo ""
            echo "Options:"
            echo "  --verbose, -v  Show detailed output including valid references"
            echo "  --fix          Attempt to suggest fixes for broken references"
            echo "  --help, -h     Show this help message"
            exit 0
            ;;
    esac
done

echo -e "${BLUE}=== Agent OS Cross-Reference Validation ===${NC}"
echo ""

# Check if required files exist
if [ ! -f "$FRAMEWORK_GUIDE" ]; then
    echo -e "${RED}ERROR: framework-guide.md not found at $FRAMEWORK_GUIDE${NC}"
    exit 1
fi

if [ ! -d "$SKILLS_DIR" ]; then
    echo -e "${YELLOW}WARNING: Skills directory not found at $SKILLS_DIR${NC}"
    WARNINGS=$((WARNINGS + 1))
fi

if [ ! -d "$AGENTS_DIR" ]; then
    echo -e "${YELLOW}WARNING: Agents directory not found at $AGENTS_DIR${NC}"
    WARNINGS=$((WARNINGS + 1))
fi

# Step 1: Extract section headers from framework-guide.md
echo -e "${BLUE}1. Extracting section headers from framework-guide.md...${NC}"

HEADERS_FILE=$(mktemp)
grep -n "^## \|^### \|^#### " "$FRAMEWORK_GUIDE" | sed 's/^[0-9]*://' > "$HEADERS_FILE"
HEADER_COUNT=$(wc -l < "$HEADERS_FILE" | tr -d ' ')

echo "   Found $HEADER_COUNT section headers"
if $VERBOSE; then
    echo "   Sample headers:"
    head -5 "$HEADERS_FILE" | while read -r line; do
        echo "     - $line"
    done
fi
echo ""

# Step 2: Find all framework-guide.md references in skills
echo -e "${BLUE}2. Validating skill references to framework-guide.md...${NC}"

if [ -d "$SKILLS_DIR" ]; then
    # Find all references to framework-guide.md sections
    SKILL_REFS=$(mktemp)

    # Search for patterns like "framework-guide.md" followed by section references
    grep -rn "framework-guide.md\|§\|\"####\|\"###\|\"##" "$SKILLS_DIR" 2>/dev/null | \
        grep -v "Binary file" > "$SKILL_REFS" || true

    REF_COUNT=$(wc -l < "$SKILL_REFS" | tr -d ' ')
    echo "   Found $REF_COUNT potential section references in skills"

    # Validate each reference
    while IFS= read -r ref_line; do
        FILE=$(echo "$ref_line" | cut -d':' -f1)
        LINE_NUM=$(echo "$ref_line" | cut -d':' -f2)
        CONTENT=$(echo "$ref_line" | cut -d':' -f3-)

        # Extract section header references (look for quoted headers or § notation)
        # Pattern: "### Header Name" or "#### Header Name" or § Header Name
        SECTION_REFS=$(echo "$CONTENT" | grep -oE '"(#{2,4}[^"]+)"|\u00A7 [^,\n]+|§ [^,\n]+' || true)

        if [ -n "$SECTION_REFS" ]; then
            while IFS= read -r section_ref; do
                # Clean up the reference
                CLEAN_REF=$(echo "$section_ref" | sed 's/^"//; s/"$//; s/^§ //; s/^# *//')

                # Check if this header exists in framework-guide.md
                if grep -qF "$CLEAN_REF" "$HEADERS_FILE" 2>/dev/null; then
                    VALID=$((VALID + 1))
                    if $VERBOSE; then
                        echo -e "   ${GREEN}✓${NC} $FILE:$LINE_NUM - Found: $CLEAN_REF"
                    fi
                else
                    ERRORS=$((ERRORS + 1))
                    echo -e "   ${RED}✗${NC} $FILE:$LINE_NUM"
                    echo -e "     Reference: $CLEAN_REF"
                    echo -e "     ${RED}Not found in framework-guide.md${NC}"

                    if $FIX_MODE; then
                        # Suggest similar headers
                        SIMILAR=$(grep -i "$(echo "$CLEAN_REF" | head -c 20)" "$HEADERS_FILE" | head -3)
                        if [ -n "$SIMILAR" ]; then
                            echo -e "     ${YELLOW}Suggestions:${NC}"
                            echo "$SIMILAR" | while read -r suggestion; do
                                echo "       - $suggestion"
                            done
                        fi
                    fi
                fi
            done <<< "$SECTION_REFS"
        fi
    done < "$SKILL_REFS"

    rm -f "$SKILL_REFS"
else
    echo "   Skipped - skills directory not found"
fi
echo ""

# Step 3: Validate file path references in agents
echo -e "${BLUE}3. Validating agent file path references...${NC}"

if [ -d "$AGENTS_DIR" ]; then
    AGENT_REFS=$(mktemp)

    # Find references to skill files and other agent-os files
    grep -rn "skills/\|references/\|standards/\|guidelines/" "$AGENTS_DIR" 2>/dev/null | \
        grep -v "Binary file" > "$AGENT_REFS" || true

    REF_COUNT=$(wc -l < "$AGENT_REFS" | tr -d ' ')
    echo "   Found $REF_COUNT file path references in agents"

    while IFS= read -r ref_line; do
        FILE=$(echo "$ref_line" | cut -d':' -f1)
        LINE_NUM=$(echo "$ref_line" | cut -d':' -f2)
        CONTENT=$(echo "$ref_line" | cut -d':' -f3-)

        # Extract file paths
        PATHS=$(echo "$CONTENT" | grep -oE '(skills|references|standards|guidelines)/[a-zA-Z0-9_/-]+\.md' || true)

        for path in $PATHS; do
            # Construct full path
            if [[ "$path" == skills/* ]]; then
                FULL_PATH="$AGENT_OS_DIR/.claude/$path"
            else
                FULL_PATH="$AGENT_OS_DIR/$path"
            fi

            if [ -f "$FULL_PATH" ]; then
                VALID=$((VALID + 1))
                if $VERBOSE; then
                    echo -e "   ${GREEN}✓${NC} $FILE:$LINE_NUM - $path"
                fi
            else
                ERRORS=$((ERRORS + 1))
                echo -e "   ${RED}✗${NC} $FILE:$LINE_NUM"
                echo -e "     Path: $path"
                echo -e "     ${RED}File not found${NC}"

                if $FIX_MODE; then
                    # Try to find similar files
                    DIR=$(dirname "$FULL_PATH")
                    BASENAME=$(basename "$path" .md)
                    if [ -d "$DIR" ]; then
                        SIMILAR=$(ls "$DIR" 2>/dev/null | grep -i "${BASENAME:0:10}" | head -3)
                        if [ -n "$SIMILAR" ]; then
                            echo -e "     ${YELLOW}Similar files in directory:${NC}"
                            echo "$SIMILAR" | while read -r suggestion; do
                                echo "       - $suggestion"
                            done
                        fi
                    fi
                fi
            fi
        done
    done < "$AGENT_REFS"

    rm -f "$AGENT_REFS"
else
    echo "   Skipped - agents directory not found"
fi
echo ""

# Step 4: Check skill SKILL.md files have Deep Reference sections
echo -e "${BLUE}4. Checking skill structure compliance...${NC}"

if [ -d "$SKILLS_DIR" ]; then
    for skill_dir in "$SKILLS_DIR"/*/; do
        if [ -d "$skill_dir" ]; then
            SKILL_NAME=$(basename "$skill_dir")
            SKILL_FILE="$skill_dir/SKILL.md"
            REFS_DIR="$skill_dir/references"

            if [ ! -f "$SKILL_FILE" ]; then
                WARNINGS=$((WARNINGS + 1))
                echo -e "   ${YELLOW}!${NC} Skill '$SKILL_NAME' missing SKILL.md"
            else
                # Check for Deep Reference section
                if ! grep -q "Deep Reference\|Framework Guide Reference" "$SKILL_FILE" 2>/dev/null; then
                    WARNINGS=$((WARNINGS + 1))
                    echo -e "   ${YELLOW}!${NC} Skill '$SKILL_NAME' SKILL.md missing Deep Reference section"
                else
                    if $VERBOSE; then
                        echo -e "   ${GREEN}✓${NC} Skill '$SKILL_NAME' has proper structure"
                    fi
                fi
            fi

            # Check references directory
            if [ ! -d "$REFS_DIR" ]; then
                if $VERBOSE; then
                    echo -e "   ${YELLOW}!${NC} Skill '$SKILL_NAME' has no references directory"
                fi
            fi
        fi
    done
else
    echo "   Skipped - skills directory not found"
fi
echo ""

# Step 5: Validate ignore list in documentation-discovery.md
echo -e "${BLUE}5. Checking documentation-discovery.md ignore list...${NC}"

DOC_DISCOVERY="$AGENT_OS_DIR/guidelines/documentation-discovery.md"
if [ -f "$DOC_DISCOVERY" ]; then
    if grep -q "## Ignore List" "$DOC_DISCOVERY"; then
        echo -e "   ${GREEN}✓${NC} Ignore list section exists"

        # Count entries (excluding header and empty rows)
        IGNORE_COUNT=$(grep -A 100 "## Ignore List" "$DOC_DISCOVERY" | \
            grep "^|" | grep -v "Pattern\|---" | wc -l | tr -d ' ')
        echo "   Ignore list entries: $IGNORE_COUNT"
    else
        WARNINGS=$((WARNINGS + 1))
        echo -e "   ${YELLOW}!${NC} Ignore list section not found"
    fi
else
    echo "   Skipped - documentation-discovery.md not found"
fi
echo ""

# Cleanup
rm -f "$HEADERS_FILE"

# Summary
echo -e "${BLUE}=== Validation Summary ===${NC}"
echo ""
echo -e "  Valid references:  ${GREEN}$VALID${NC}"
echo -e "  Errors:            ${RED}$ERRORS${NC}"
echo -e "  Warnings:          ${YELLOW}$WARNINGS${NC}"
echo ""

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}Validation failed with $ERRORS error(s)${NC}"
    echo ""
    echo "To get fix suggestions, run: $0 --fix"
    exit 1
elif [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}Validation passed with $WARNINGS warning(s)${NC}"
    exit 0
else
    echo -e "${GREEN}All validations passed!${NC}"
    exit 0
fi