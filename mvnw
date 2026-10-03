#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper script for UNIX
# ----------------------------------------------------------------------------
set -e

MAVEN_VERSION="3.9.9"
MAVEN_DIST_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip"
MAVEN_BASE="${HOME}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_HOME="${MAVEN_BASE}/apache-maven-${MAVEN_VERSION}"

if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
    echo "[INFO] Apache Maven ${MAVEN_VERSION} not found locally. Downloading..."
    mkdir -p "${MAVEN_BASE}"
    ZIP_FILE="${MAVEN_BASE}/apache-maven.zip"
    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "${MAVEN_DIST_URL}" -o "${ZIP_FILE}"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "${MAVEN_DIST_URL}" -O "${ZIP_FILE}"
    else
        echo "[ERROR] Neither curl nor wget found to download Maven."
        exit 1
    fi
    unzip -q "${ZIP_FILE}" -d "${MAVEN_BASE}"
    rm -f "${ZIP_FILE}"
    chmod +x "${MAVEN_HOME}/bin/mvn"
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"