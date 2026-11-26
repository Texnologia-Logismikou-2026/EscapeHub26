# EscapeHub26
Running with Docker Compose

The backend can be started using a published Docker image from GitHub Container Registry (GHCR).

Prerequisites

Docker installed

Docker Compose v2

Access to the published image in GHCR

Select the image version

Different setts of commands:

Run:

docker compose up

Docker Compose will pull the selected image from GHCR and start the backend.

To run the application in the background:

docker compose up -d

Change the version

Update the IMAGE_TAG variable in version.env.

For example:

IMAGE_TAG=v0.3.0

Then recreate the container:

docker compose down
docker compose up -d

View logs

docker compose logs -f

Stop the application

docker compose down