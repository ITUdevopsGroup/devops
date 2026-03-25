from playwright.sync_api import Page, expect

APP_URL = "http://localhost:3000"
API_URL = "http://localhost:5001"


def install_common_api_mocks(page: Page):
    page.route(
        f"{API_URL}/",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "data": [
                {
                  "messageId": 1,
                  "userId": 1,
                  "username": "alice",
                  "email": "alice@example.com",
                  "text": "Hello from Alice",
                  "pubDate": 1710000000
                }
              ]
            }
            """,
        ),
    )

    page.route(
        f"{API_URL}/stats",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body='{"totalMessages": 1}',
        ),
    )

    page.route(
        f"{API_URL}/user?*",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "data": [
                {
                  "messageId": 10,
                  "userId": 1,
                  "username": "alice",
                  "email": "alice@example.com",
                  "text": "My own post",
                  "pubDate": 1710001000
                }
              ],
              "followed": false
            }
            """,
        ),
    )


def test_timeline_page_loads(page: Page):
    install_common_api_mocks(page)

    page.goto(f"{APP_URL}/timeline")

    expect(page.get_by_role("heading", name="MiniTwit")).to_be_visible()
    expect(page.get_by_role("heading", name="Public timeline")).to_be_visible()
    expect(page.get_by_text("Hello from Alice")).to_be_visible()
    expect(page.get_by_text("1 Total tweets")).to_be_visible()


def test_logged_out_timeline_shows_auth_links(page: Page):
    install_common_api_mocks(page)

    page.goto(f"{APP_URL}/timeline")

    expect(page.get_by_text("Sign up", exact=True)).to_be_visible()
    expect(page.get_by_text("Sign in", exact=True)).to_be_visible()


def test_logged_in_timeline_shows_share_box(page: Page):
    install_common_api_mocks(page)

    page.goto(f"{APP_URL}/timeline?user=1&username=alice")

    expect(page.get_by_text("My timeline", exact=True)).to_be_visible()
    page.get_by_text("My timeline", exact=True).click()

    expect(page.locator('textarea[name="text"]')).to_be_visible()
    expect(page.locator('input[type="submit"][value="Share"]')).to_be_visible()
    expect(page.get_by_text("Sign out", exact=True)).to_be_visible()


def test_login_page_loads(page: Page):
    page.goto(f"{APP_URL}/login")

    expect(page.get_by_role("heading", name="MiniTwit")).to_be_visible()
    expect(page.get_by_role("heading", name="Sign in")).to_be_visible()


def test_login_navigation(page: Page):
    install_common_api_mocks(page)

    page.goto(f"{APP_URL}/login")

    page.get_by_text("Public timeline", exact=True).click()
    expect(page).to_have_url(f"{APP_URL}/timeline")

    page.goto(f"{APP_URL}/login")
    page.get_by_text("Sign up", exact=True).click()
    expect(page).to_have_url(f"{APP_URL}/register")


def test_login_empty_username(page: Page):
    page.goto(f"{APP_URL}/login")

    page.locator('input[name="password"]').fill("secret")
    page.locator('input[type="submit"][value="Sign In"]').click()

    expect(page.get_by_text("Please enter your username")).to_be_visible()


def test_login_empty_password(page: Page):
    page.goto(f"{APP_URL}/login")

    page.locator('input[name="username"]').fill("alice")
    page.locator('input[type="submit"][value="Sign In"]').click()

    expect(page.get_by_text("Please enter your password")).to_be_visible()


def test_login_success_redirects(page: Page):
    install_common_api_mocks(page)

    page.route(
        f"{API_URL}/spec_user?*",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "userData": {
                "username": "alice",
                "userId": 1,
                "pwOK": true
              }
            }
            """,
        ),
    )

    page.goto(f"{APP_URL}/login")

    page.locator('input[name="username"]').fill("alice")
    page.locator('input[name="password"]').fill("secret")
    page.locator('input[type="submit"][value="Sign In"]').click()

    expect(page).to_have_url(f"{APP_URL}/timeline?user=1&username=alice")


def test_login_failure_shows_error(page: Page):
    page.route(
        f"{API_URL}/spec_user?*",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "userData": {
                "username": "alice",
                "userId": 1,
                "pwOK": false
              }
            }
            """,
        ),
    )

    page.goto(f"{APP_URL}/login")

    page.locator('input[name="username"]').fill("alice")
    page.locator('input[name="password"]').fill("wrong")
    page.locator('input[type="submit"][value="Sign In"]').click()

    expect(page.get_by_text("Wrong user name or password")).to_be_visible()


def test_register_page_loads(page: Page):
    page.goto(f"{APP_URL}/register")

    expect(page.get_by_role("heading", name="MiniTwit")).to_be_visible()
    expect(page.get_by_text("Please fill out form to sign up")).to_be_visible()


def test_register_navigation(page: Page):
    install_common_api_mocks(page)

    page.goto(f"{APP_URL}/register")

    page.get_by_text("Public timeline", exact=True).click()
    expect(page).to_have_url(f"{APP_URL}/timeline")

    page.goto(f"{APP_URL}/register")
    page.get_by_text("Sign in", exact=True).click()
    expect(page).to_have_url(f"{APP_URL}/login")


def test_register_empty_username(page: Page):
    page.goto(f"{APP_URL}/register")

    page.locator('input[name="email"]').fill("alice@example.com")
    page.locator('input[name="password"]').fill("secret")
    page.locator('input[name="password2"]').fill("secret")
    page.locator('input[type="submit"][value="Sign Up"]').click()

    expect(page.get_by_text("You have to enter a username")).to_be_visible()


def test_register_invalid_email(page: Page):
    page.goto(f"{APP_URL}/register")

    page.locator('input[name="username"]').fill("alice")
    page.locator('input[name="email"]').fill("not-an-email")
    page.locator('input[name="password"]').fill("secret")
    page.locator('input[name="password2"]').fill("secret")
    page.locator('input[type="submit"][value="Sign Up"]').click()

    expect(page.get_by_text("You have to enter a valid email address")).to_be_visible()


def test_register_empty_password(page: Page):
    page.goto(f"{APP_URL}/register")

    page.locator('input[name="username"]').fill("alice")
    page.locator('input[name="email"]').fill("alice@example.com")
    page.locator('input[name="password2"]').fill("secret")
    page.locator('input[type="submit"][value="Sign Up"]').click()

    expect(page.get_by_text("You have to enter a password")).to_be_visible()


def test_register_password_mismatch(page: Page):
    page.goto(f"{APP_URL}/register")

    page.locator('input[name="username"]').fill("alice")
    page.locator('input[name="email"]').fill("alice@example.com")
    page.locator('input[name="password"]').fill("secret")
    page.locator('input[name="password2"]').fill("different")
    page.locator('input[type="submit"][value="Sign Up"]').click()

    expect(page.get_by_text("The two passwords do not match")).to_be_visible()


def test_register_success_alert(page: Page):
    page.route(
        f"{API_URL}/register?*",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "userData": {
                "result": true
              }
            }
            """,
        ),
    )

    page.goto(f"{APP_URL}/register")

    page.locator('input[name="username"]').fill("alice")
    page.locator('input[name="email"]').fill("alice@example.com")
    page.locator('input[name="password"]').fill("secret")
    page.locator('input[name="password2"]').fill("secret")

    with page.expect_event("dialog") as dialog_info:
        page.locator('input[type="submit"][value="Sign Up"]').click()

    dialog = dialog_info.value
    assert dialog.message == "Great! You can now sign in."
    dialog.accept()


def test_add_message_alert(page: Page):
    install_common_api_mocks(page)

    page.route(
        f"{API_URL}/add_message?*",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "userData": {
                "result": true
              }
            }
            """,
        ),
    )

    page.goto(f"{APP_URL}/timeline?user=1&username=alice")
    page.get_by_text("My timeline", exact=True).click()

    page.locator('textarea[name="text"]').fill("UI test post")

    with page.expect_event("dialog") as dialog_info:
        page.locator('input[type="submit"][value="Share"]').click()

    dialog = dialog_info.value
    assert dialog.message == "Your message was recorded"
    dialog.accept()


def test_follow_user_alert(page: Page):
    page.route(
        f"{API_URL}/",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "data": [
                {
                  "messageId": 1,
                  "userId": 2,
                  "username": "bob",
                  "email": "bob@example.com",
                  "text": "Bob post",
                  "pubDate": 1710000000
                }
              ]
            }
            """,
        ),
    )

    page.route(
        f"{API_URL}/stats",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body='{"totalMessages": 1}',
        ),
    )

    page.route(
        f"{API_URL}/user?*",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "data": [
                {
                  "messageId": 2,
                  "userId": 2,
                  "username": "bob",
                  "email": "bob@example.com",
                  "text": "Bob profile post",
                  "pubDate": 1710000100
                }
              ],
              "followed": false
            }
            """,
        ),
    )

    page.route(
        f"{API_URL}/follow?*",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "userData": {
                "result": true
              }
            }
            """,
        ),
    )

    page.goto(f"{APP_URL}/timeline?user=1&username=alice")
    page.get_by_text("bob", exact=True).click()

    expect(page.get_by_text("You are not yet following this user")).to_be_visible()

    with page.expect_event("dialog") as dialog_info:
        page.get_by_text("Follow user", exact=True).click()

    dialog = dialog_info.value
    assert dialog.message == "You are now following bob"
    dialog.accept()


def test_unfollow_user_alert(page: Page):
    page.route(
        f"{API_URL}/",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "data": [
                {
                  "messageId": 1,
                  "userId": 2,
                  "username": "bob",
                  "email": "bob@example.com",
                  "text": "Bob post",
                  "pubDate": 1710000000
                }
              ]
            }
            """,
        ),
    )

    page.route(
        f"{API_URL}/stats",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body='{"totalMessages": 1}',
        ),
    )

    page.route(
        f"{API_URL}/user?*",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "data": [
                {
                  "messageId": 2,
                  "userId": 2,
                  "username": "bob",
                  "email": "bob@example.com",
                  "text": "Bob profile post",
                  "pubDate": 1710000100
                }
              ],
              "followed": true
            }
            """,
        ),
    )

    page.route(
        f"{API_URL}/unfollow?*",
        lambda route: route.fulfill(
            status=200,
            content_type="application/json",
            body="""
            {
              "userData": {
                "result": true
              }
            }
            """,
        ),
    )

    page.goto(f"{APP_URL}/timeline?user=1&username=alice")
    page.get_by_text("bob", exact=True).click()

    expect(page.get_by_text("You are currently following this user")).to_be_visible()

    with page.expect_event("dialog") as dialog_info:
        page.get_by_text("Unfollow user", exact=True).click()

    dialog = dialog_info.value
    assert dialog.message == "You are no longer following bob"
    dialog.accept()