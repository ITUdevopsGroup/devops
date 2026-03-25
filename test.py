import base64
import requests

BASE_URL = "http://localhost:5001"
# BASE_URL = "http://ec2-13-51-198-31.eu-north-1.compute.amazonaws.com:5001"

USERNAME = "simulator"
PWD = "super_safe!"
CREDENTIALS = f"{USERNAME}:{PWD}".encode("ascii")
ENCODED_CREDENTIALS = base64.b64encode(CREDENTIALS).decode()

HEADERS = {
    "Connection": "close",
    "Content-Type": "application/json",
    "Authorization": f"Basic {ENCODED_CREDENTIALS}",
}


def get_latest_value():
    response = requests.get(f"{BASE_URL}/latest", headers=HEADERS, timeout=60)
    assert response.ok
    return response.json()["latest"]


def test_latest():
    before_latest = get_latest_value()

    url = f"{BASE_URL}/register"
    data = {"username": "test", "email": "test@test", "pwd": "foo"}
    params = {"latest": before_latest + 1}

    response = requests.post(url, json=data, params=params, headers=HEADERS, timeout=60)
    assert response.ok

    after_latest = get_latest_value()
    assert after_latest >= before_latest


def test_register():
    before_latest = get_latest_value()

    username = "a"
    email = "a@a.a"
    pwd = "a"
    data = {"username": username, "email": email, "pwd": pwd}
    params = {"latest": before_latest + 1}

    response = requests.post(
        f"{BASE_URL}/register",
        json=data,
        headers=HEADERS,
        params=params,
        timeout=60,
    )
    assert response.ok

    after_latest = get_latest_value()
    assert after_latest >= before_latest


def test_create_msg():
    before_latest = get_latest_value()

    username = "a"
    data = {"content": "Blub!"}
    url = f"{BASE_URL}/msgs/{username}"
    params = {"latest": before_latest + 1}

    response = requests.post(url, json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    response = requests.get(
        f"{BASE_URL}/msgs/{username}",
        headers=HEADERS,
        params={"no": 50},
        timeout=60,
    )
    assert response.ok

    messages = response.json()
    assert any(msg["content"] == "Blub!" and msg["user"] == username for msg in messages)

    after_latest = get_latest_value()
    assert after_latest >= before_latest


def test_get_latest_user_msgs():
    before_latest = get_latest_value()

    username = "a"
    query = {"no": 20, "latest": before_latest + 1}
    url = f"{BASE_URL}/msgs/{username}"

    response = requests.get(url, headers=HEADERS, params=query, timeout=60)
    assert response.status_code == 200

    got_it_earlier = False
    for msg in response.json():
        if msg["content"] == "Blub!" and msg["user"] == username:
            got_it_earlier = True

    assert got_it_earlier

    after_latest = get_latest_value()
    assert after_latest >= before_latest


def test_get_latest_msgs():
    before_latest = get_latest_value()

    username = "a"
    query = {"no": 20, "latest": before_latest + 1}
    url = f"{BASE_URL}/msgs"

    response = requests.get(url, headers=HEADERS, params=query, timeout=60)
    assert response.status_code == 200

    got_it_earlier = False
    for msg in response.json():
        if msg["content"] == "Blub!" and msg["user"] == username:
            got_it_earlier = True

    assert got_it_earlier

    after_latest = get_latest_value()
    assert after_latest >= before_latest


def test_register_b():
    before_latest = get_latest_value()

    username = "b"
    email = "b@b.b"
    pwd = "b"
    data = {"username": username, "email": email, "pwd": pwd}
    params = {"latest": before_latest + 1}

    response = requests.post(
        f"{BASE_URL}/register",
        json=data,
        headers=HEADERS,
        params=params,
        timeout=60,
    )
    assert response.ok

    after_latest = get_latest_value()
    assert after_latest >= before_latest


def test_register_c():
    before_latest = get_latest_value()

    username = "c"
    email = "c@c.c"
    pwd = "c"
    data = {"username": username, "email": email, "pwd": pwd}
    params = {"latest": before_latest + 1}

    response = requests.post(
        f"{BASE_URL}/register",
        json=data,
        headers=HEADERS,
        params=params,
        timeout=60,
    )
    assert response.ok

    after_latest = get_latest_value()
    assert after_latest >= before_latest


def test_follow_user():
    before_latest = get_latest_value()

    username = "a"
    url = f"{BASE_URL}/fllws/{username}"

    data = {"follow": "b"}
    params = {"latest": before_latest + 1}
    response = requests.post(url, json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    current_latest = get_latest_value()

    data = {"follow": "c"}
    params = {"latest": current_latest + 1}
    response = requests.post(url, json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    current_latest = get_latest_value()

    query = {"no": 20, "latest": current_latest + 1}
    response = requests.get(url, headers=HEADERS, params=query, timeout=60)
    assert response.ok

    json_data = response.json()
    assert "b" in json_data["follows"]
    assert "c" in json_data["follows"]

    after_latest = get_latest_value()
    assert after_latest >= before_latest


def test_a_unfollows_b():
    before_latest = get_latest_value()

    username = "a"
    url = f"{BASE_URL}/fllws/{username}"

    data = {"unfollow": "b"}
    params = {"latest": before_latest + 1}
    response = requests.post(url, json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    current_latest = get_latest_value()

    query = {"no": 20, "latest": current_latest + 1}
    response = requests.get(url, params=query, headers=HEADERS, timeout=60)
    assert response.ok
    assert "b" not in response.json()["follows"]

    after_latest = get_latest_value()
    assert after_latest >= before_latest