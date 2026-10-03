import os
import glob

services = [
    "userService", "activityService", "aiService", "gamificationService",
    "notificationService", "feedService", "relationshipService",
    "mediaService", "moderationService", "apiGateway"
]

for service in services:
    path = os.path.join(r"c:\Wefit", service, "src", "main", "resources", "application.yml")
    if os.path.exists(path):
        with open(path, "r") as f:
            content = f.read()
        
        content = content.replace("http://localhost:8888", "https://localhost:8888")
        
        with open(path, "w") as f:
            f.write(content)
        print(f"Updated {path}")
    else:
        print(f"File not found: {path}")
