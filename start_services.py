import subprocess
import time
import sys
import os

def load_env():
    # Load environment variables from .env file if it exists
    env_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")
    if os.path.exists(env_path):
        print("Loading environment variables from .env...")
        with open(env_path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#") or "=" not in line:
                    continue
                key, val = line.split("=", 1)
                key = key.strip()
                val = val.strip()
                # Remove surrounding quotes if they exist
                if (val.startswith('"') and val.endswith('"')) or (val.startswith("'") and val.endswith("'")):
                    val = val[1:-1]
                os.environ[key] = val

def start_service(name, cmd_str, wait_sec):
    print(f"Starting {name}...")
    
    # On Windows, spawn a new PowerShell console that inherits env from this process
    if sys.platform == "win32":
        # Use PowerShell's Start-Process to open a new window that inherits environment
        ps_cmd = (
            f"Start-Process powershell -ArgumentList '-NoExit', '-Command', '{cmd_str}' "
            f"-WorkingDirectory '{os.getcwd()}'"
        )
        subprocess.Popen(
            ["powershell", "-Command", ps_cmd],
            env=os.environ  # <-- pass current env (with .env loaded) to the child
        )
    else:
        subprocess.Popen(cmd_str, shell=True, env=os.environ)
    
    if wait_sec > 0:
        print(f"Waiting for {name} to start ({wait_sec}s)...")
        time.sleep(wait_sec)

def main():
    load_env()
    # 1. Eureka
    start_service(
        name="Eureka Server",
        cmd_str="cd eureka && mvnw.cmd spring-boot:run",
        wait_sec=30
    )
    
    # 2. UserService
    start_service(
        name="UserService on port 8081",
        cmd_str="cd userService && mvnw.cmd spring-boot:run",
        wait_sec=15
    )
    
    # 3. ActivityService
    start_service(
        name="ActivityService on port 8082",
        cmd_str="cd activityService && mvnw.cmd spring-boot:run",
        wait_sec=15
    )
    
    # 4. AiService
    start_service(
        name="AiService on port 8083",
        cmd_str="cd aiService && mvnw.cmd spring-boot:run",
        wait_sec=0
    )
    
    print("\nAll services started!")
    print("Eureka: http://localhost:8761")
    print("UserService: http://localhost:8081")
    print("ActivityService: http://localhost:8082")
    print("AiService: http://localhost:8083")
    
    input("\nPress Enter to exit...")

if __name__ == "__main__":
    main()
