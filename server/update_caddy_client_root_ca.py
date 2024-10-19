"""
Ships Caddy's self-signed root certificate to the client.
"""

import datetime
import os

import requests

CADDY_ADMIN_BASE_URL = "http://127.0.0.1:2019/{path}"
CADDY_CERTS_PATH = "pki/ca/local"
CADDY_HOSTS_PATH = "config/apps/http/servers/srv0/routes/0/match/0/host"

CWD = os.path.dirname(os.path.realpath(__file__))
OUTPUT_BASE_FOLDER = os.path.join(CWD, "..", "client", "app", "src", "main", "res")
OUTPUT_NETWORK_SECURITY_CONFIG_PATH = os.path.join(
    OUTPUT_BASE_FOLDER, "xml", "network_security_config.xml"
)
OUTPUT_CERT_PATH = os.path.join(OUTPUT_BASE_FOLDER, "raw", "caddy_root_ca")


NETWORK_SECURITY_CONFIG_ITEM_TEMPLATE = (
    '\t\t<domain includeSubdomains="true">{domain}</domain>'
)
NETWORK_SECURITY_CONFIG_TEMPLATE = """\
<!-- Auto-generated at {date} -->
<network-security-config>
    <domain-config>
{domains}
        <trust-anchors>
            <certificates src="@raw/caddy_root_ca"/>
        </trust-anchors>
    </domain-config>
</network-security-config>
"""


if __name__ == "__main__":
    r = requests.get(CADDY_ADMIN_BASE_URL.format(path=CADDY_HOSTS_PATH))
    r.raise_for_status()
    caddy_hosts = r.json()

    if caddy_hosts is None:
        raise Exception("Caddy hosts not found. Are your sure that caddy is running?")

    print(
        "Writing caddy hosts to network security config: {}".format(
            OUTPUT_NETWORK_SECURITY_CONFIG_PATH
        )
    )

    with open(OUTPUT_NETWORK_SECURITY_CONFIG_PATH, "w") as f:
        f.write(
            NETWORK_SECURITY_CONFIG_TEMPLATE.format(
                date=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                domains="\n".join(
                    [
                        NETWORK_SECURITY_CONFIG_ITEM_TEMPLATE.format(domain=host)
                        for host in caddy_hosts
                    ]
                ),
            )
        )

    r = requests.get(CADDY_ADMIN_BASE_URL.format(path=CADDY_CERTS_PATH))
    r.raise_for_status()
    caddy_root_ca = r.json()["root_certificate"]

    if caddy_root_ca is None:
        raise Exception(
            "Caddy root certificate not found. Are you sure that caddy is running?"
        )

    print("Writing caddy root certificate to {}".format(OUTPUT_CERT_PATH))

    with open(OUTPUT_CERT_PATH, "w") as f:
        f.write(caddy_root_ca)
