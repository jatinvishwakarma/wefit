import urllib.request
import urllib.error
import ssl
import json

GATEWAY_URL = "https://localhost:8443"
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

def test_health():
    print("Testing API Gateway Health...")
    try:
        req = urllib.request.Request(f"{GATEWAY_URL}/actuator/health")
        with urllib.request.urlopen(req, context=ctx) as response:
            status = response.getcode()
            text = response.read().decode('utf-8')
            print(f"Status: {status}")
            print(f"Response: {text}")
            if status == 200:
                print("API Gateway is UP.")
                return True
            return False
    except urllib.error.HTTPError as e:
        print(f"Status: {e.code}")
        print(f"Response: {e.read().decode('utf-8')}")
        return False
    except Exception as e:
        print(f"Connection failed: {e}")
        return False

def test_service_routes():
    routes = {
        "UserService": "/api/v1/users/test",
        "ActivityService": "/api/v1/activities",
        "AiService": "/api/v1/ai/recommendations",
        "GamificationService": "/api/v1/gamification/challenges",
        "NotificationService": "/api/v1/notifications/test",
        "FeedService": "/api/v1/feed/test",
        "RelationshipService": "/api/v1/relationships/test",
        "MediaService": "/api/v1/media/test",
        "ModerationService": "/api/v1/moderation/test"
    }

    print("\nTesting Microservice Routing...")
    for name, path in routes.items():
        try:
            req = urllib.request.Request(f"{GATEWAY_URL}{path}")
            with urllib.request.urlopen(req, context=ctx) as response:
                print(f"{name} ({path}): Status {response.getcode()}")
        except urllib.error.HTTPError as e:
            print(f"{name} ({path}): Status {e.code}")
            if e.code in [502, 503, 500, 404]:
                print(f"  -> ISSUE DETECTED for {name}")
        except Exception as e:
            print(f"{name} ({path}): Connection failed - {e}")

if __name__ == "__main__":
    if test_health():
        test_service_routes()
    else:
        print("Cannot test downstream routes because API Gateway is down.")
