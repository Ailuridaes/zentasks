package controllers;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import play.mvc.*;
import play.mvc.Http.*;
import play.libs.*;
import play.test.*;
import static play.test.Helpers.*;
import com.avaje.ebean.Ebean;
import com.google.common.collect.ImmutableMap;

public class LoginTest extends WithApplication {
    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
        Ebean.save((List) Yaml.load("test-data.yml"));
    }

	@Test
	public void authenticateSuccess() {
		// Deprecated test methods from tutorial
		/* Result result = callAction(
			controllers.routes.ref.Application.authenticate(),
			fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
				"email", "bob@example.com",
				"password", "secret"))
		); */

		RequestBuilder request = new RequestBuilder()
			.method(POST)
			.uri("/login")
			.bodyForm(ImmutableMap.of(
				"email", "bob@example.com",
				"password", "secret"));

		Result result = route(request);

		// Getting 303 right now because it redirects to a uri accessible with a GET method
		// Should be 302 once authentication is fully implemented
		assertEquals(303, status(result));
		assertEquals("bob@example.com", session(result).get("email"));
	}

	@Test
	public void authenticateFailure() {
		RequestBuilder request = new RequestBuilder()
			.method(POST)
			.uri("/login")
			.bodyForm(ImmutableMap.of(
				"email", "bob@example.com",
				"password", "badpassword"));

		Result result = route(request);

		assertEquals(400, status(result));
		assertNull(session(result).get("email"));
	}

	@Test
	public void authenticated() {
		RequestBuilder request = new RequestBuilder()
			.method(GET)
			.uri("/")
			.session("email", "bob@example.com");

		Result result = route(request);
		assertEquals(200, status(result));
	}

	@Test
	public void notAuthenticated() {
		RequestBuilder request = new RequestBuilder()
			.method(GET)
			.uri("/");

		Result result = route(request);

		assertEquals(303, status(result));
		assertEquals("/login", header("Location", result));
	}
}
