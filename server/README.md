## Sample user notificaions API 

## Built with

- [Caddy](https://caddyserver.com/) - Reverse proxy with autoSSL
- [FastAPI](https://fastapi.tiangolo.com/) - Web framework for building APIs with Python
- [uvicorn](https://www.uvicorn.org/) - ASGI web server for Python

## How to run

### Build & start via [docker compose](https://docs.docker.com/compose/install/)
```bash
docker compose up -d --build
```

The API will be available locally at http://localhost:8000, https://localhost or https://notifications.localhost (if it can be resolved to 127.0.0.1)

> [!NOTE]
>
> API docs will be available at http://localhost:8000/docs or https://localhost/docs

## <a id='going-live'>Going live (access to API from the device)</a>

> [!WARNING]
>
> Android client will throw an exception on self-signed certificate. [See more on developer.android.com](https://developer.android.com/privacy-and-security/security-config#CustomTrust)
>
> To ship the caddy root certificate to Android app as trusted, run the command: 
>
> ```bash
> pip install requests
> python update_caddy_client_root_ca.py
> ```

> [!TIP]
>
> If nothing works for you (ssl, domain, port discovery or other issues), try tunneling to 8000 port, i.e. [tuna.am](https://tuna.am/).

Here are several options:

- Using [adb reverse](https://fig.io/manual/adb/reverse):

    `adb root`

    `adb reverse tcp:80 tcp:80`

    `adb reverse tcp:443 tcp:443`

    `adb reverse tcp:8000 tcp:8000`

    https://localhost and http://localhost:8000 should be accesible from device now

- Exposing local port (8000) via tunneling services like [tuna.am](https://tuna.am/)

- <a id='internal-ip-address'>Connecting to API using the internal IP address</a>

    Get the internal address of the host machine on the private network:

    #### Linux
    ```bash
    ifconfig
    # Most likely desired ip starts with 192.168.
    ifconfig | grep "192.168."
    ```

    #### Windows
    ```bash
    ipconfig
    # Most likely desired ip starts with 192.168.
    ipconfig | find "192.168."
    ```

    **FOR EXAMPLE**, let's say this internal host IP = `192.168.100.12`

    Set **INTERNAL_IP**=`192.168.100.12` in the `.env` file
    
    You will have to *restart docker containers* to update the ip address

    https://192.168.100.12 and http://192.168.100.12:8000 should be accesible, but you should check the availability of ports (80, 443, 8000) on the host

> [!IMPORTANT]
> 
> Accessing **https**://192.168.100.12 from the device may result in *SSLPeerUnverifiedException: Hostname 192.168.100.12 is not verified* due to docker using **userland-proxy** and then the original source IP will be lost behind the proxy
>
> [See more on caddy.community](https://caddy.community/t/how-to-get-a-true-remote-ip-behind-caddy-reverse-proxy/22348)

### Using mDNS service to broadcast `notifications.local` domain in your private network

> [!NOTE]
>
> mDNS should be supported on Android 12 and above

Make `notifications.local` point to local ip (`127.0.0.1`) and [our internal ip](#internal-ip-address) in the private network (ex, `192.168.100.12`):

#### Windows
```bash
# implicitly read parameters from .env
.\mdns\start.bat
# or
# explicitly pass parameters
.\mdns\start.bat --local_domain notifications --addresses 192.168.100.12,127.0.0.1
```
#### Linux
```bash
# implicitly read parameters from .env
./mdns/start.sh
# or
# explicitly pass parameters
./mdns/start.sh --local_domain notifications --addresses 192.168.100.12,127.0.0.1
```

It should be accessible at https://notifications.local