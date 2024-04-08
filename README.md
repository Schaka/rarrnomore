# Rarrnomore - prevent automatic grabs of rar'd scene releases

Disclaimer: I am not responsible for you deleting any torrents. Please test your setup before running it unmonitored.
**You need to set your torrent clients in Radarr and Sonarr to start new torrents in the paused state.**

### Notes
- currently, only qBittorrent and Transmission are supported

### Planned
- some unit tests
- (optional) other file types can be added to the exclusion list

This application works by monitoring Radarr and Sonarr `Grab` requests through web hooks that you need to set up. 
Once it receives a notification, it instantly connects to your torrent client, finds the torrent that was just added and checks its contents.
If it finds a .rar or partial rar (.r01) file, it sends a request to your servarr application to delete this item from the queue and blocklist that torrent.
If no rar is found, it sends a request to your torrent client to resume the torrent, starting the download.

## Setup
Currently, the code is only published as a docker image to GitHub.
If you cannot use Docker, you're out of luck for now.

### Setting up Docker
- map /config from within the container to a host folder of your choice
- within that host folder, put a copy of [application.yml](https://github.com/Schaka/rarrnomore/blob/main/src/main/resources/application.yml) from this repository
- choose either `QBITTORRENT` or `TRANSMISSION` (credentials not required for Transmission, if disabled)
- adjust said copy with your own info like *arr API keys and your preferred port
- forward the port you've chosen from your container to the host system

**Important**: The `clients.torrent.name` property needs to exactly match the name you gave your client in Sonarr/Radarr, this is validated against web hook requests at runtime.

A docker run command may look like this:
```
docker run
-d
--name='rarrnomore'
-e HOST_CONTAINERNAME="rarrnomore"
-p 8978:8978
-v '/mnt/user/appdata/rarrnomore/config':'/config':'rw' 'ghcr.io/schaka/rarrnomore'
```

An example of a `docker-compose.yml` may look like this:

```yml
services:
  janitorr:
    container_name: rarrnomore
    image: ghcr.io/schaka/rarrnomore:latest
    volumes:
      - /appdata/janitorr/config:/config
```

A native image is also published for every tagged release. It keeps a much lower memory and CPU footprint and doesn't require longer runtimes to achieve optimal performance (JIT).
If you restart more often than once a week or have a very low powered server, this is now recommended.
That image is always tagged `:native`. To get a specific version, use `:native-v1.x.x`.
It also requires you to map application.yml slightly differently - see below:

```yml
services:
  janitorr:
    container_name: janitorr
    image: ghcr.io/schaka/rarrnomore:native
    volumes:
      - /appdata/rarrnomore/config/application.yml:/workspace/application.yml
```

To get the latest build as found in the development branch, grab the following image: `ghcr.io/schaka/rarrnomore:develop`.

### Setting up Unraid
- Go to Docker, click "Add Container" at the bottom
- enter image name 'schaka/rarrnomore'
- Click "Add another Path, Port, Variable, Label or Device", choose Path
- map Container Path `/config` to host path `/mnt/user/appdata/rarrnomore/config`
- for native, map Container Path `/app/application.yml` to host path `/mnt/user/appdata/rarrnomore/config/application.yml`
- map Container Port `8978` to host port `8978`

It should look like this:

![unraid](docs/img/unraid.png)

## Configuring your web hook
- open Sonarr/Radarr, go to Settings => Connect
- click '+', choose Webhook, choose a name
- only enable Notification trigger 'Grab'
- enter `http://rarrnomore:8978/hook/sonarr`, where IP and port need to match your Docker container
- replace `sonarr` with `radarr` if applicable
- choose method POST, save the settings

It should look like this:

![webhook](docs/img/webhook.png)
