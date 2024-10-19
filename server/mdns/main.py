import asyncio
import socket

from zeroconf import ServiceBrowser, ServiceInfo, ServiceListener, Zeroconf


class mDNSEventListener(ServiceListener):
    def update_service(self, zc: Zeroconf, type_: str, name: str) -> None:
        print(f"Service {name} updated")

    def remove_service(self, zc: Zeroconf, type_: str, name: str) -> None:
        print(f"Service {name} removed")

    def add_service(self, zc: Zeroconf, type_: str, name: str) -> None:
        info = zc.get_service_info(type_, name)
        print(f"Service {name} added, service info: {info}")


async def register_service(service: ServiceInfo) -> None:
    zeroconf = Zeroconf()
    browser = ServiceBrowser(zeroconf, "_http._tcp.local.", mDNSEventListener())
    try:
        await zeroconf.async_register_service(service)
        await asyncio.Event().wait()
    finally:
        browser.cancel()
        zeroconf.close()


if __name__ == "__main__":
    import argparse
    import os.path

    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--local_domain",
        dest="local_domain",
        type=str,
        help="Add local_domain",
    )
    parser.add_argument(
        "--addresses",
        dest="addresses",
        type=str,
        help="Add addresses (comma separated)",
    )
    args = parser.parse_args()

    if args.local_domain is None or args.addresses is None:
        # Try to read root .env file if it exists
        env_vars = {}
        if os.path.isfile("../.env"):
            with open("../.env", "r") as f:
                env_vars = dict(
                    tuple(line.replace("\n", "").split("="))
                    for line in f.readlines()
                    if not line.startswith("#")
                )

        args.local_domain = env_vars.get("LOCAL_DOMAIN") or "service"
        if "INTERNAL_IP" in env_vars:
            args.addresses = f"{env_vars.get('INTERNAL_IP')}"
        else:
            args.addresses = "127.0.0.1"

    print(args.local_domain, " -> ", args.addresses)

    local_domain = args.local_domain
    addresses = args.addresses
    addresses = list(map(socket.inet_aton, addresses.split(",")))

    asyncio.run(
        register_service(
            ServiceInfo(
                type_="_http._tcp.local.",
                name=f"{local_domain}._http._tcp.local.",
                addresses=addresses,
                port=80,
                server=f"{local_domain}.local.",
            )
        )
    )
