package foodtruckfinder.site.endpoint;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foodtruckfinder.site.common.foodtruck.FoodTruckDto;
import foodtruckfinder.site.common.user.UserService;
import foodtruckfinder.site.common.user.UserService.RegistrationRequest;
import foodtruckfinder.site.common.user.UserDto;

/**
 * This is a controller endpoint which is intended to manage users and their association with food trucks. Because the class
 * is annotated with @RestController Spring will automatically take care of the creation of this class... we will never do
 * something like <code>UserEndpoint endpoint = new UserEndpoint()</code> in our code because this would break the Sping lifecycle.
 *
 * @RestController is like @Service, but implies a few additional attributes - it wires up with controller with Spring MVC so that
 * the @GetMapping and @PostMapping annotations work properly. If you had just used @Service here, those annotations would not have
 * any effect and our controller would not work like we anticipate. Additionally, @RestController differs from @Controller in that it
 * expects to receive and return JSON payloads at each of the endpoints. If we had just used @Controller here we would have to annotate our
 * function parameters and return types with @RequestBody and @ResponseBody respectively. @RestController annotation, then, is simply a convenience
 * which makes these additional annotations unnecessary.
 *
 * The typical Spring architecture is for Controllers to call down into Services, which then call into other Services or Daos. We'll talk more about
 * Services and Daos in other classes.
 */
@RestController
@RequestMapping(value = "/api/user")
public class UserEndpoint {
	/**
	 * @Autowired directs spring to set this field when creating the singleton instance of UserEndpoint. If you are familiar with Java but not Spring,
	 * you may wonder how this field is non-null because it doesn't get set equal to anything in the codebase. When Spring creates the singleton instance of
	 * UserEndpoint it looks in the Spring context for a singleton instance of UserService because of this @Autowired annotation and then sets that field appropriately.
	 * The UserService instances are created by the @Service annotation. Look inside of UserService for more information on how this works.
	 *
	 * The autowiring happens by type. If there are multiple of that type in the Spring context (this would be multiple instances of UserService in this case) then
	 * the application will fail to start - Spring will report that it cannot find a unique bean of type UserService. If you need to solve this, you can annotate one of those
	 * services with @Primary or you can @Autowire a list of elements.
	 */
	@Autowired
	private UserService userService;

	/**
	 * This @GetMapping annotation binds the below function to a GET HTTP request. Whenever the /api/user url is hit, the below function will be invoked. This is because
	 * the method level @GetMapping annotation inherits a root path from the class level @ReuestMapping annotation on UserEndpoint. The return type of UserDto will be
	 * serialized to a JSON representation via Jackson, a Java JSON marshalling library that Spring uses under the covers. You can hit this endpoint yourself in Postman, but
	 * will need to pass an authentication header as well. You can see how this is done by the application by opening up the network tab in the browser when you log in.
	 *
	 * Note: If you're wondering about what Optional is here, this is a Java utility for null saftey in that it forces the programmer to check the nullability of a value before
	 * accessing that value itself in an attempt to prevent occurence of NullPointerExceptions. In this case, it is possible that the user we want to get the details of does not exist.
	 * In this case, we would return Optional.empty() to signify that it is a null result. If you want to read more about Optional, see: https://www.baeldung.com/java-optional
	 */
	@GetMapping(value = "", produces = "application/json")
	public Optional<UserDto> getUserDetails() {
		// This line gets the "principal" of the currently logged in user - Spring sets this value for us based on the authentication header that is passed with the request
		// In this case "principal" refers to the email address of the user
		String principal = SecurityContextHolder.getContext().getAuthentication().getName();

		// Then, we simply look up that user by their email address in Elasticsearch
		return userService.findUserByPrincipal(principal);
	}

	/**
	 * The @PostMapping annotation is very similar to the @GetMapping annotation except that it expects HTTP POST requests instead of GET request. Because of this, a post can
	 * accept a payload of data in its post body. You can almost think of a GET call as a function which takes no parameters, while a POST call is a function that takes a paramter
	 * via the POST body. In this case, the body of the request is JSON that is serialized via Jackson into a RegistrationRequest Java object. The application calls this when you
	 * register as a new user.
	 *
	 * In this function, we take a RegistrationRequest which includes a username, password, and map of additional attributes and we persist a new user in ElasticSearch. Then, we
	 * return that new user back to the frontend as a JSON payload - represented by the UserDto class here as the return type.
	 */
	@PostMapping(value = "/register")
	public UserDto register(@RequestBody RegistrationRequest request) {
		return userService.register(request);
	}
}