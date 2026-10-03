import subprocess
import time
import os
import sys

def load_env():
    env_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")
    if os.path.exists(env_path):
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
                os.environ[key] = val

def start_service(name, cmd_str, log_file):
    print(f"Starting {name}...")
    log_fd = open(log_file, "w")
    
    # Fix the environment PATH
    env = os.environ.copy()
    env["PATH"] = r"C:\Windows\System32;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Program Files\Java\jdk-21\bin;C:\Zeotap_Assesment\apache-maven-3.9.9\bin;" + env.get("PATH", "")
    
    proc = subprocess.Popen(
        r'C:\Windows\System32\cmd.exe /c "' + cmd_str + '"',
        cwd=os.path.dirname(log_file),
        env=env,
        stdout=log_fd,
        stderr=subprocess.STDOUT
    )
    return proc

def main():
    load_env()
    log_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "scratch", "logs")
    os.makedirs(log_dir, exist_ok=True)
    
    procs = []
    
    # 1. Eureka
    procs.append(start_service("Eureka Server", r"cd c:\Wefit\eureka && mvnw.cmd spring-boot:run", os.path.join(log_dir, "eureka.log")))
    time.sleep(15)
    
    # 2. Config Server
    procs.append(start_service("Config Server", r"cd c:\Wefit\configServer && mvnw.cmd spring-boot:run", os.path.join(log_dir, "config.log")))
    time.sleep(15)
    
    # 3. User Service
    procs.append(start_service("UserService", r"cd c:\Wefit\userService && mvnw.cmd spring-boot:run", os.path.join(log_dir, "user.log")))
    
    # 4. Activity Service
    procs.append(start_service("ActivityService", r"cd c:\Wefit\activityService && mvnw.cmd spring-boot:run", os.path.join(log_dir, "activity.log")))
    
    # 5. AI Service
    procs.append(start_service("AiService", r"cd c:\Wefit\aiService && mvnw.cmd spring-boot:run", os.path.join(log_dir, "ai.log")))
    
    # 6. API Gateway
    procs.append(start_service("ApiGateway", r"cd c:\Wefit\apiGateway && mvnw.cmd spring-boot:run", os.path.join(log_dir, "gateway.log")))
    
    # 7. Relationship Service
    procs.append(start_service("RelationshipService", r"cd c:\Wefit\relationshipService && mvn spring-boot:run", os.path.join(log_dir, "relationship.log")))
    
    # 8. Moderation Service
    procs.append(start_service("ModerationService", r"cd c:\Wefit\moderationService && mvn spring-boot:run", os.path.join(log_dir, "moderation.log")))
    
    # 9. Media Service
    procs.append(start_service("MediaService", r"cd c:\Wefit\mediaService && mvn spring-boot:run", os.path.join(log_dir, "media.log")))
    
    print("\nAll services started in background.")
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        for p in procs:
            p.terminate()

if __name__ == "__main__":
    main()
