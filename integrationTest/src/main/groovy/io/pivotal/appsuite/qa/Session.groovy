package io.pivotal.appsuite.qa

class Session {

    static final CookieManager COOKIE_MANAGER
    static {
        COOKIE_MANAGER = new CookieManager()
        CookieHandler.default = COOKIE_MANAGER
    }

    def id

    Session(String url) {
        HttpURLConnection conn = url.toURL().openConnection() as HttpURLConnection
        conn.requestMethod = 'GET'
        conn.connect()
        if (conn.responseCode == HttpURLConnection.HTTP_OK)
            id = COOKIE_MANAGER.cookieStore.cookies.find { it.name == 'JSESSIONID' }
    }

    @Override
    String toString() {
        "Session[id=${id}]"
    }

    @Override
    boolean equals(Object o) {
        if (o == null) {
            return false
        }
        if (!(o instanceof Session)) {
            return false
        }
        return this.id == o.id
    }

}
