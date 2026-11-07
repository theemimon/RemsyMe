# Run `just --list` to see available commands.

image_name := "remsyme"
commit := `git rev-parse --short HEAD`
image := image_name + ":" + commit

# Build the docker image, tagged with the current commit hash.
docker-build:
    docker build -t {{image}} .

# Run the unit test suite inside the docker container.
test: docker-build
    docker run --rm {{image}} ./gradlew test --console=plain --no-daemon

# Build the app (debug APK) inside the docker container.
build: docker-build
    docker run --rm {{image}} ./gradlew assembleDebug --console=plain --no-daemon
