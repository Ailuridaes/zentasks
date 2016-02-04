package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import play.data.*;

import models.*;
import views.html.*;

public class Application extends Controller {

    public Result index() {
        return ok(index.render(
			Project.find.all(),
            Task.find.all()
		));
    }
	
	public Result login() {
        return ok(
            login.render(form(Login.class))
        );
    }
	
	public Result authenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			return badRequest(login.render(loginForm));
		} else {
			session().clear();
			session("email", loginForm.get().email);
			return redirect(
				routes.Application.index()
			);
		}
	}
	
	
	public static class Login {
		public String email;
		public String password;
		
		public String validate() {
			if (User.authenticate(email, password) == null) {
			  return "Invalid username or password";
			}
			return null;
		}
	}

}
