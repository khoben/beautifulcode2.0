## [mDNS](https://en.wikipedia.org/wiki/Multicast_DNS#:~:text=Multicast%20DNS%20(mDNS)%20is%20a,Domain%20Name%20System%20(DNS).) service broadcaster

Offers `[local_domain].local` --> `[ip1,ip2]` mapping via [mDNS](https://en.wikipedia.org/wiki/Multicast_DNS#:~:text=Multicast%20DNS%20(mDNS)%20is%20a,Domain%20Name%20System%20(DNS).) broadcast

## Should be natively supported with

- Windows 10 (1511) and above

- [Android 11+](https://source.android.com/docs/core/ota/modular-system/dns-resolver#mdns-local-resolution)

- iOS, OSX (modern versions)

- Linux distros may require related packages and services installation

## How to run

> [!NOTE]
>
> [mDNS will not work](https://forums.docker.com/t/multicast-not-working-even-with-net-host/134035) in docker on Windows even with `network_mode=host`

Tested with `python 3.11` on `Windows 11` (23H2) as host and `Android 14` as client on the same WI-FI network.

Also make sure that the required service ports are open and reachable in the firewall settings on your host OS.

### (Optional) Activate virtual environment

```
python -m venv venv
# linux
source venv/bin/activate
# windows
.\venv\Scripts\activate
```

### Install dependencies

```sh
pip install -r requirements.txt
```

### Start mDNS offerring

```sh
python main.py --local_domain notifications --addresses 192.168.100.12,127.0.0.1
```