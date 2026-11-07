# Builds the RemsyMe Android app and runs its JVM unit tests (./gradlew test).
#
# Usage:
#   docker build -t remsyme-build .          # builds the app and runs unit tests
#   docker run --rm remsyme-build            # re-runs the unit tests on demand
#
# The debug APK produced by the build is left under /workspace/app/build/outputs
# inside the image if you need to copy it out, e.g.:
#   docker create --name remsyme-tmp remsyme-build
#   docker cp remsyme-tmp:/workspace/app/build/outputs/apk ./apk
#   docker rm remsyme-tmp

FROM eclipse-temurin:25-jdk-jammy@sha256:e4f029273ed9e4444697df30bb6098dbe51477291598aebc54dd0b48d95d6ae7

# Pinned command-line tools build + SDK package versions. Bump these (and the
# checksum below) if Google retires the cmdline-tools download or ships a
# newer compileSdk.
ARG CMDLINE_TOOLS_URL=https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip
ARG CMDLINE_TOOLS_SHA256=7ec965280a073311c339e571cd5de778b9975026cfcbe79f2b1cdcb1e15317ee
ARG ANDROID_PLATFORM=android-37.0
ARG ANDROID_BUILD_TOOLS=37.0.0

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=${ANDROID_HOME}
ENV PATH=${ANDROID_HOME}/cmdline-tools/latest/bin:${PATH}
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false"

# --- Android SDK command-line tools -----------------------------------------
# Exact versions pinned (including Ubuntu's apt point-releases) for reproducible rebuilds.
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
         curl=7.81.0-1ubuntu1.30 \
         unzip=6.0-26ubuntu3.2 \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && curl -fsSL -o /tmp/cmdline-tools.zip "${CMDLINE_TOOLS_URL}" \
    && echo "${CMDLINE_TOOLS_SHA256}  /tmp/cmdline-tools.zip" | sha256sum -c - \
    && unzip -q /tmp/cmdline-tools.zip -d "${ANDROID_HOME}/cmdline-tools" \
    && mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm /tmp/cmdline-tools.zip

# platform-tools is deliberately omitted: the Android package repo only ever
# serves the latest build under that package id (no pinnable version), and
# neither assembleDebug nor the unit tests need adb/fastboot.
RUN yes | sdkmanager --licenses --sdk_root="${ANDROID_HOME}" >/dev/null \
    && sdkmanager --sdk_root="${ANDROID_HOME}" \
         "platforms;${ANDROID_PLATFORM}" \
         "build-tools;${ANDROID_BUILD_TOOLS}" >/dev/null

# --- Project sources ----------------------------------------------------------
WORKDIR /workspace

# Copy the Gradle wrapper and build scripts first so dependency/toolchain
# resolution can be cached in its own layer independently of app source edits.
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN chmod +x gradlew

COPY app ./app

# Builds the debug APK and runs the full JVM unit test suite; fails the image
# build if either step fails.
RUN ./gradlew assembleDebug test --console=plain --no-daemon

CMD ["./gradlew", "test", "--console=plain", "--no-daemon"]
