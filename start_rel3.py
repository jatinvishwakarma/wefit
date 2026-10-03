import subprocess
import os

env_path = os.path.join(os.path.abspath("c:\\Wefit"), ".env")
env = os.environ.copy()
env["PATH"] = r"C:\Windows\System32;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Program Files\Java\jdk-21\bin;C:\Zeotap_Assesment\apache-maven-3.9.9\bin;" + env.get("PATH", "")

with open(env_path, "r", encoding="utf-8") as f:
    for line in f:
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, val = line.split("=", 1)
        key = key.strip()
        val = val.strip()
        if (val.startswith('"') and val.endswith('"')) or (val.startswith("'") and val.endswith("'")):
            val = val[1:-1]
        env[key] = val

with open("c:\\Wefit\\scratch\\logs\\relationship.log", "w") as f:
    subprocess.Popen(
        r'C:\Windows\System32\cmd.exe /c "mvn spring-boot:run"',
        cwd="c:\\Wefit\\relationshipService",
        env=env,
        stdout=f,
        stderr=subprocess.STDOUT
    )
