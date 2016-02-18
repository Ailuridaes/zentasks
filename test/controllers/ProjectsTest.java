package controllers;

import models.*;

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

public class ProjectsTest extends WithApplication {
    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
        Ebean.save((List) Yaml.load("test-data.yml"));
    }

  @Test
  public void newProject() {
    RequestBuilder request = new RequestBuilder()
      .session("email", "bob@example.com")
      .method(POST)
      .uri("/projects")
      .bodyForm(ImmutableMap.of(
        "group", "Some Group"));

    Result result = route(request);

    assertEquals(200, status(result));
    Project project = Project.find.where()
        .eq("folder", "Some Group").findUnique();
    assertNotNull(project);
    assertEquals("New project", project.name);
    assertEquals(1, project.members.size());
    assertEquals("bob@example.com", project.members.get(0).email);
  }

  @Test
  public void renameProject() {
    long id = Project.find.where()
        .eq("members.email", "bob@example.com")
        .eq("name", "Private").findUnique().id;

    RequestBuilder request = new RequestBuilder()
      .session("email", "bob@example.com")
      .method(PUT)
      .uri("/projects/"+id)
      .bodyForm(ImmutableMap.of(
        "name", "New name"));

    Result result = route(request);

    assertEquals(200, status(result));
    assertEquals("New name", Project.find.byId(id).name);
  }

  @Test
  public void renameProjectForbidden() {
    long id = Project.find.where()
        .eq("members.email", "jeff@example.com")
        .eq("name", "Private").findUnique().id;

    RequestBuilder request = new RequestBuilder()
      .session("email", "bob@example.com")
      .method(PUT)
      .uri("/projects/"+id)
      .bodyForm(ImmutableMap.of(
        "name", "New name"));

    Result result = route(request);

    assertEquals(403, status(result));
    assertEquals("Private", Project.find.byId(id).name);
  }

}
