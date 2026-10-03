import subprocess
import os

with open("c:\\Wefit\\scratch\\logs\\relationship.log", "w") as f:
    subprocess.Popen(
        "mvn.cmd spring-boot:run",
        cwd="c:\\Wefit\\relationshipService",
        shell=True,
        stdout=f,
        stderr=subprocess.STDOUT
    )
