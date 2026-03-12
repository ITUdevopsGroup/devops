import json
import base64
import requests

BASE_URL = 'http://localhost:5001'
#BASE_URL = 'http://ec2-13-51-198-31.eu-north-1.compute.amazonaws.com:5001'

USERNAME = 'simulator'
PWD = 'super_safe!'
CREDENTIALS = ':'.join([USERNAME, PWD]).encode('ascii')
ENCODED_CREDENTIALS = base64.b64encode(CREDENTIALS).decode()

HEADERS = {
    'Connection': 'close',
    'Content-Type': 'application/json',
    'Authorization': f'Basic {ENCODED_CREDENTIALS}'
}


def test_latest():
    url = f"{BASE_URL}/register"
    data = {'username': 'test', 'email': 'test@test', 'pwd': 'foo'}
    params = {'latest': 1337}
    response = requests.post(url, json=data, params=params, headers=HEADERS, timeout=60)
    assert response.ok

    url = f'{BASE_URL}/latest'
    response = requests.get(url, headers=HEADERS, timeout=60)
    assert response.ok
    assert response.json()['latest'] == 1337
    print("Passed!")


def test_register():
    username = 'a'
    email = 'a@a.a'
    pwd = 'a'
    data = {'username': username, 'email': email, 'pwd': pwd}
    params = {'latest': 1}
    response = requests.post(f'{BASE_URL}/register', json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    response = requests.get(f'{BASE_URL}/latest', headers=HEADERS, timeout=60)
    assert response.json()['latest'] == 1
    print("Passed!")


def test_create_msg():
    username = 'a'
    data = {'content': 'Blub!'}
    url = f'{BASE_URL}/msgs/{username}'
    params = {'latest': 2}
    response = requests.post(url, json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    response = requests.get(f'{BASE_URL}/latest', headers=HEADERS, timeout=60)
    assert response.json()['latest'] == 2
    print("Passed!")


def test_get_latest_user_msgs():
    username = 'a'
    query = {'no': 20, 'latest': 3}
    url = f'{BASE_URL}/msgs/{username}'
    response = requests.get(url, headers=HEADERS, params=query, timeout=60)
    assert response.status_code == 200

    got_it_earlier = False
    for msg in response.json():
        if msg['content'] == 'Blub!' and msg['user'] == username:
            got_it_earlier = True

    assert got_it_earlier

    response = requests.get(f'{BASE_URL}/latest', headers=HEADERS, timeout=60)
    assert response.json()['latest'] == 3
    print("Passed!")


def test_get_latest_msgs():
    username = 'a'
    query = {'no': 20, 'latest': 4}
    url = f'{BASE_URL}/msgs'
    response = requests.get(url, headers=HEADERS, params=query, timeout=60)
    assert response.status_code == 200

    got_it_earlier = False
    for msg in response.json():
        if msg['content'] == 'Blub!' and msg['user'] == username:
            got_it_earlier = True

    assert got_it_earlier

    response = requests.get(f'{BASE_URL}/latest', headers=HEADERS, timeout=60)
    assert response.json()['latest'] == 4
    print("Latest msgs!")


def test_register_b():
    username = 'b'
    email = 'b@b.b'
    pwd = 'b'
    data = {'username': username, 'email': email, 'pwd': pwd}
    params = {'latest': 5}
    response = requests.post(f'{BASE_URL}/register', json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    response = requests.get(f'{BASE_URL}/latest', headers=HEADERS, timeout=60)
    assert response.json()['latest'] == 5
    print("Register tests passed!")


def test_register_c():
    username = 'c'
    email = 'c@c.c'
    pwd = 'c'
    data = {'username': username, 'email': email, 'pwd': pwd}
    params = {'latest': 6}
    response = requests.post(f'{BASE_URL}/register', json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    response = requests.get(f'{BASE_URL}/latest', headers=HEADERS, timeout=60)
    assert response.json()['latest'] == 6
    print("Register tests passed!")


def test_follow_user():
    username = 'a'
    url = f'{BASE_URL}/fllws/{username}'

    data = {'follow': 'b'}
    params = {'latest': 7}
    response = requests.post(url, json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    data = {'follow': 'c'}
    params = {'latest': 8}
    response = requests.post(url, json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    query = {'no': 20, 'latest': 9}
    response = requests.get(url, headers=HEADERS, params=query, timeout=60)
    assert response.ok

    json_data = response.json()
    assert "b" in json_data["follows"]
    assert "c" in json_data["follows"]

    response = requests.get(f'{BASE_URL}/latest', headers=HEADERS, timeout=60)
    assert response.json()['latest'] == 9
    print("follow tests passed!")


def test_a_unfollows_b():
    username = 'a'
    url = f'{BASE_URL}/fllws/{username}'

    data = {'unfollow': 'b'}
    params = {'latest': 10}
    response = requests.post(url, json=data, headers=HEADERS, params=params, timeout=60)
    assert response.ok

    query = {'no': 20, 'latest': 11}
    response = requests.get(url, params=query, headers=HEADERS, timeout=60)
    assert response.ok
    assert 'b' not in response.json()['follows']

    response = requests.get(f'{BASE_URL}/latest', headers=HEADERS, timeout=60)
    assert response.json()['latest'] == 11
    print("Unfollow tests passed!")


test_latest()
test_register()
test_create_msg()
test_get_latest_user_msgs()
test_get_latest_msgs()
test_register_b()
test_register_c()
test_follow_user()
test_a_unfollows_b()
print("ALL TESTS PASSED")